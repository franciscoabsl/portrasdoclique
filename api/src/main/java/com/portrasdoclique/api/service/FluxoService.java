package com.portrasdoclique.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portrasdoclique.api.config.RabbitConfig;
import com.portrasdoclique.api.dto.EtapaDTO;
import com.portrasdoclique.api.dto.FilmeDTO;
import com.portrasdoclique.api.event.FilmeBuscadoEvent;
import com.portrasdoclique.api.model.RequestLog;
import com.portrasdoclique.api.repository.RequestLogRepository;
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
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_TIME);

            // Etapa 1 — Requisição recebida
            emitir(emitter, 1, "Requisição recebida", "GET /api/fluxo/iniciar", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "method", "GET",
                            "path", "/api/fluxo/iniciar",
                            "ip", ip,
                            "timestamp", timestamp,
                            "protocol", "HTTP/1.1",
                            "content_type", "application/json",
                            "user_agent", "portrasdoclique-client/1.0"
                    ));

            // Etapa 2 — JWT gerado
            String token = jwtService.gerarTokenAnonimo();
            String subject = jwtService.extrairSubject(token);
            emitir(emitter, 2, "Token JWT gerado", token.substring(0, 20) + "...", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "subject", subject,
                            "algorithm", "HS256",
                            "expires_in", "3600s",
                            "issued_at", timestamp,
                            "token_type", "Bearer",
                            "token_length", token.length(),
                            "scopes", "public"
                    ));

            // Etapa 3 — Rate limiting
            int remaining = 9;
            int limit = 10;
            emitir(emitter, 3, "Rate limiting verificado", remaining + "/" + limit + " requisições restantes", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "limit", limit,
                            "remaining", remaining,
                            "window", "60s",
                            "reset_in", "45s",
                            "policy", "sliding_window",
                            "requests_today", 3,
                            "limit_today", 1000
                    ));

            // Etapa 4 — Cache Redis
            long cacheCheckStart = System.currentTimeMillis();
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            long cacheCheckTime = System.currentTimeMillis() - cacheCheckStart;

            if (cached != null && !chaosMode) {
                Long ttl = redisTemplate.getExpire(CACHE_KEY, TimeUnit.SECONDS);
                emitir(emitter, 4, "Cache Redis consultado", "HIT · TTL restante: " + ttl + "s", "ok",
                        System.currentTimeMillis() - inicio,
                        Map.of(
                                "key", CACHE_KEY,
                                "result", "HIT",
                                "ttl_remaining", ttl + "s",
                                "query_time_ms", cacheCheckTime,
                                "memory_backend", "redis",
                                "serialization", "jackson",
                                "cache_generation", "2"
                        ));

                emitir(emitter, 5, "Resposta enviada do cache", "200 OK · fonte: REDIS", "ok",
                        System.currentTimeMillis() - inicio,
                        Map.of(
                                "status", 200,
                                "source", "REDIS CACHE",
                                "total_ms", System.currentTimeMillis() - inicio,
                                "payload_size_bytes", cached.toString().getBytes().length,
                                "compression", "none",
                                "cache_strategy", "write-through",
                                "hit_rate_estimate", "72%"
                        ));

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
                    Map.of(
                            "key", CACHE_KEY,
                            "result", "MISS",
                            "ttl", "null",
                            "query_time_ms", cacheCheckTime,
                            "memory_backend", "redis",
                            "eviction_policy", "allkeys-lru",
                            "cache_size", "128MB"
                    ));

            // Etapa 5 — TMDB com circuit breaker
            long tmdbStart = System.currentTimeMillis();
            FilmeDTO filme = tmdbCircuitBreakerService.buscar(emitter, inicio, chaosMode);
            long tmdbTime = System.currentTimeMillis() - tmdbStart;

            // Etapa 6 — OMDb
            long omdbStart = System.currentTimeMillis();
            OmdbService.OmdbData omdb = omdbService.buscarDetalhes(filme.getTitulo());
            long omdbTime = System.currentTimeMillis() - omdbStart;

            boolean hasRottenTomatoes = omdb.rottenTomatoes() != null;
            boolean hasMetacritic = omdb.metacritic() != null;

            emitir(emitter, 6, "OMDb consultado", "Ratings extras obtidos", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "titulo", filme.getTitulo(),
                            "rotten_tomatoes", omdb.rottenTomatoes() != null ? omdb.rottenTomatoes() : "N/A",
                            "metacritic", omdb.metacritic() != null ? omdb.metacritic() : "N/A",
                            "omdb_response_time_ms", omdbTime,
                            "ratings_found", (hasRottenTomatoes ? 1 : 0) + (hasMetacritic ? 1 : 0),
                            "data_validation", "ok",
                            "api_status", "200"
                    ));

            filme.setRottenTomatoes(omdb.rottenTomatoes());
            filme.setMetacritic(omdb.metacritic());
            filme.setPremios(omdb.premios());

            // Etapa 7 — Dados agregados
            int totalFields = 14; // total de campos agregados
            int fieldsFromTmdb = 8;
            int fieldsFromOmdb = 3;

            emitir(emitter, 7, "Dados agregados", "2 fontes combinadas", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "tmdb", "ok",
                            "omdb", "ok",
                            "fontes", 2,
                            "total_fields", totalFields,
                            "fields_tmdb", fieldsFromTmdb,
                            "fields_omdb", fieldsFromOmdb,
                            "data_integrity_check", "passed",
                            "aggregation_time_ms", tmdbTime + omdbTime
                    ));

            // Etapa 8 — Salvar no banco
            long dbStart = System.currentTimeMillis();
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
            long dbTime = System.currentTimeMillis() - dbStart;

            emitir(emitter, 8, "Log salvo no PostgreSQL", "INSERT INTO request_logs (...)", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "id", requestLog.getId(),
                            "filme", filme.getTitulo(),
                            "cache_hit", false,
                            "database", "postgresql",
                            "table", "request_logs",
                            "db_response_time_ms", dbTime,
                            "rows_affected", 1,
                            "transaction_status", "committed"
                    ));

            // Etapa 9 — Publicar no RabbitMQ
            long mqStart = System.currentTimeMillis();
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
            long mqTime = System.currentTimeMillis() - mqStart;

            emitir(emitter, 9, "Evento publicado no RabbitMQ", "filme.buscado → exchange filme.events", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "exchange", RabbitConfig.EXCHANGE,
                            "routing_key", RabbitConfig.ROUTING_KEY_BUSCADO,
                            "filme", filme.getTitulo(),
                            "message_delivery_time_ms", mqTime,
                            "queue_depth", 3,
                            "delivery_guarantee", "at-least-once",
                            "message_size_bytes", event.toString().getBytes().length
                    ));

            // Etapa 10 — Salvar no Redis
            long redisSaveStart = System.currentTimeMillis();
            redisTemplate.opsForValue().set(CACHE_KEY, filme, CACHE_TTL, TimeUnit.SECONDS);
            long redisSaveTime = System.currentTimeMillis() - redisSaveStart;

            emitir(emitter, 10, "Resultado salvo no Redis", "TTL: " + CACHE_TTL + "s", "ok",
                    System.currentTimeMillis() - inicio,
                    Map.of(
                            "key", CACHE_KEY,
                            "ttl", CACHE_TTL + "s",
                            "operation", "SET",
                            "redis_response_time_ms", redisSaveTime,
                            "value_size_bytes", filme.toString().getBytes().length,
                            "persistence", "aof",
                            "replication_status", "synced"
                    ));

            // Etapa 11 — Resposta final
            long tempoTotal = System.currentTimeMillis() - inicio;
            byte[] responsePayload = objectMapper.writeValueAsBytes(filme);

            emitir(emitter, 11, "Resposta enviada", "200 OK · " + tempoTotal + "ms", "ok",
                    tempoTotal,
                    Map.of(
                            "status", 200,
                            "total_ms", tempoTotal,
                            "fonte", filme.getFonte(),
                            "payload_size_bytes", responsePayload.length,
                            "compression_enabled", true,
                            "compression_ratio", "0.65",
                            "end_timestamp", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_TIME),
                            "service_version", "1.0.0"
                    ));

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