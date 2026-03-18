package com.portrasdoclique.api.service;

import com.portrasdoclique.api.dto.EtapaDTO;
import com.portrasdoclique.api.dto.FilmeDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbCircuitBreakerService {

    private final TmdbService tmdbService;

    @CircuitBreaker(name = "tmdb", fallbackMethod = "tmdbFallback")
    public FilmeDTO buscar(SseEmitter emitter, long inicio, boolean chaosMode) throws Exception {
        if (chaosMode) throw new RuntimeException("Modo caos ativado");

        FilmeDTO filme = tmdbService.buscarFilmeAleatorio();
        long tempoMs = System.currentTimeMillis() - inicio;

        emitir(emitter, 5, "TMDB API consultada", "GET /movie/popular · filme selecionado", "ok",
                tempoMs,
                Map.of(
                        "url", "api.themoviedb.org/3/movie/popular",
                        "filme", filme.getTitulo(),
                        "nota", filme.getNota()
                ));

        filme.setFonte("TMDB");
        return filme;
    }

    public FilmeDTO tmdbFallback(SseEmitter emitter, long inicio, boolean chaosMode, Exception e) throws Exception {
        long tempoMs = System.currentTimeMillis() - inicio;

        emitir(emitter, 5, "TMDB indisponível", "Circuit Breaker aberto · acionando fallback", "error",
                tempoMs,
                Map.of("error", e.getMessage(), "state", "OPEN", "fallback", "omdb"));

        emitir(emitter, 5, "Fallback ativado", "Buscando via OMDb...", "ok",
                System.currentTimeMillis() - inicio,
                Map.of("source", "fallback", "reason", "tmdb_unavailable"));

        return FilmeDTO.builder()
                .id(0)
                .titulo("Duna: Parte Dois")
                .tituloOriginal("Dune: Part Two")
                .sinopse("Paul Atreides se une aos Fremen enquanto busca vingança contra os conspiradores que destruíram sua família.")
                .posterPath("/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg")
                .posterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg")
                .nota(8.4)
                .dataLancamento("2024-02-27")
                .fonte("OMDB FALLBACK")
                .build();
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
}