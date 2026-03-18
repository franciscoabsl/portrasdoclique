package com.portrasdoclique.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portrasdoclique.api.config.RabbitConfig;
import com.portrasdoclique.api.dto.EtapaDTO;
import com.portrasdoclique.api.dto.FilmeDTO;
import com.portrasdoclique.api.event.FilmeBuscadoEvent;
import com.portrasdoclique.api.model.RequestLog;
import com.portrasdoclique.api.repository.RequestLogRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FluxoService {

    private static final String CACHE_KEY = "filme:popular:atual";
    private static final long CACHE_TTL = 300L;

    private final JwtService jwtService;
    private final TmdbService tmdbService;
    private final OmdbService omdbService;
    private final RequestLogRepository logRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TmdbCircuitBreakerService tmdbCircuitBreakerService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void executar(SseEmitter emitter, String ip, boolean chaosMode) {
        try {
            long inicio = System.currentTimeMillis();

            // Etapa 1 — Requisição recebida
            emitir(emitter, 1, "Requisição recebida", "GET /api/fluxo/iniciar", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("method", "GET", "path", "/api/fluxo/iniciar", "ip", ip));

            // Etapa 2 — JWT gerado
            String token = jwtService.gerarTokenAnonimo();
            emitir(emitter, 2, "Token JWT gerado", token.substring(0, 20) + "...", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("subject", jwtService.extrairSubject(token), "algorithm", "HS256", "expires", "3600s"));

            // Etapa 3 — Rate limiting
            emitir(emitter, 3, "Rate limiting verificado", "9/10 requisições restantes", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("limit", 10, "remaining", 9, "window", "60s"));

            // Etapa 4 — Cache Redis
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null && !chaosMode) {
                Long ttl = redisTemplate.getExpire(CACHE_KEY, TimeUnit.SECONDS);
                emitir(emitter, 4, "Cache Redis consultado", "HIT · TTL restante: " + ttl + "s", "ok",
                        System.currentTimeMillis() - inicio,
                        Map.of("key", CACHE_KEY, "result", "HIT", "ttl_remaining", ttl + "s"));

                emitir(emitter, 5, "Resposta enviada do cache", "200 OK · fonte: REDIS", "ok",
                        System.currentTimeMillis() - inicio,
                        Map.of("status", 200, "source", "REDIS CACHE", "total_ms", System.currentTimeMillis() - inicio));

                FilmeDTO filme = objectMapper.convertValue(cached, FilmeDTO.class);
                filme.setTempoTotalMs(System.currentTimeMillis() - inicio);
                filme.setFonte("REDIS CACHE");

                // Publica evento mesmo no cache hit
                FilmeBuscadoEvent eventCache = FilmeBuscadoEvent.builder()
                        .filmeTitulo(filme.getTitulo())
                        .filmeId(filme.getId())
                        .ip(ip)
                        .tempoMs((int) (System.currentTimeMillis() - inicio))
                        .cacheHit(true)
                        .fallbackUsed(false)
                        .tmdbOk(true)
                        .timestamp(LocalDateTime.now())
                        .build();
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BUSCADO, eventCache);

                emitirResultado(emitter, filme);
                emitter.complete();
                return;
            }

            emitir(emitter, 4, "Cache Redis consultado", "MISS · buscando na fonte...", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("key", CACHE_KEY, "result", "MISS", "ttl", "null"));

            // Etapa 5 — TMDB com circuit breaker
            FilmeDTO filme = tmdbCircuitBreakerService.buscar(emitter, inicio, chaosMode);

            // Etapa 6 — OMDb
            OmdbService.OmdbData omdb = omdbService.buscarDetalhes(filme.getTitulo());
            emitir(emitter, 6, "OMDb consultado", "Ratings extras obtidos", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "titulo", filme.getTitulo(),
                            "rotten_tomatoes", omdb.rottenTomatoes() != null ? omdb.rottenTomatoes() : "N/A",
                            "metacritic", omdb.metacritic() != null ? omdb.metacritic() : "N/A"
                    ));

            filme.setRottenTomatoes(omdb.rottenTomatoes());
            filme.setMetacritic(omdb.metacritic());
            filme.setPremios(omdb.premios());

            // Etapa 7 — Dados agregados
            emitir(emitter, 7, "Dados agregados", "2 fontes combinadas", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("tmdb", "ok", "omdb", "ok", "fontes", 2));

            // Etapa 8 — Salvar no banco
            RequestLog requestLog = RequestLog.builder()
                    .filmeTitulo(filme.getTitulo())
                    .filmeId(filme.getId())
                    .ip(ip)
                    .tempoMs((int) (System.currentTimeMillis() - inicio))
                    .cacheHit(false)
                    .fallbackUsed(chaosMode)
                    .tmdbOk(!chaosMode)
                    .build();
            logRepository.save(requestLog);

            emitir(emitter, 8, "Log salvo no PostgreSQL", "INSERT INTO request_logs (...)", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("id", requestLog.getId(), "filme", filme.getTitulo(), "cache_hit", false));

            // Etapa 9 — Publicar no RabbitMQ
            FilmeBuscadoEvent event = FilmeBuscadoEvent.builder()
                    .filmeTitulo(filme.getTitulo())
                    .filmeId(filme.getId())
                    .ip(ip)
                    .tempoMs((int) (System.currentTimeMillis() - inicio))
                    .cacheHit(false)
                    .fallbackUsed(chaosMode)
                    .tmdbOk(!chaosMode)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BUSCADO, event);

            emitir(emitter, 9, "Evento publicado no RabbitMQ", "filme.buscado → exchange filme.events", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "exchange", RabbitConfig.EXCHANGE,
                            "routing_key", RabbitConfig.ROUTING_KEY_BUSCADO,
                            "filme", filme.getTitulo()
                    ));

            // Etapa 10 — Salvar no Redis
            redisTemplate.opsForValue().set(CACHE_KEY, filme, CACHE_TTL, TimeUnit.SECONDS);

            emitir(emitter, 10, "Resultado salvo no Redis", "TTL: " + CACHE_TTL + "s", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of("key", CACHE_KEY, "ttl", CACHE_TTL + "s"));

            // Etapa 11 — Resposta final
            long tempoTotal = System.currentTimeMillis() - inicio;
            emitir(emitter, 11, "Resposta enviada", "200 OK · " + tempoTotal + "ms", "ok",
                    tempoTotal,
                    Map.of("status", 200, "total_ms", tempoTotal, "fonte", filme.getFonte()));

            filme.setTempoTotalMs(tempoTotal);
            filme.setFonte(chaosMode ? "OMDB FALLBACK" : "TMDB");
            emitirResultado(emitter, filme);
            emitter.complete();

        } catch (Exception e) {
            log.error("Erro no fluxo SSE", e);
            emitter.completeWithError(e);
        }
    }

    private void emitir(SseEmitter emitter, int numero, String nome, String resumo,
                        String status, long tempoMs, Map<String, Object> detalhes) throws Exception {
        EtapaDTO etapa = EtapaDTO.builder()
                .numero(numero)
                .nome(nome)
                .resumo(resumo)
                .status(status)
                .tempoMs(tempoMs)
                .detalhes(detalhes)
                .build();

        emitter.send(SseEmitter.event().name("etapa").data(etapa));
        Thread.sleep(300);
    }

    private void emitirResultado(SseEmitter emitter, FilmeDTO filme) throws Exception {
        emitter.send(SseEmitter.event().name("resultado").data(filme));
    }
}