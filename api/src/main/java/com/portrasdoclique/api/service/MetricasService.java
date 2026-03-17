package com.portrasdoclique.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portrasdoclique.api.dto.MetricasDTO;
import com.portrasdoclique.api.event.FilmeBuscadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricasService {

    private static final String KEY_TOTAL = "metricas:total";
    private static final String KEY_CACHE_HITS = "metricas:cache_hits";
    private static final String KEY_TEMPO_TOTAL = "metricas:tempo_total";
    private static final String KEY_TEMPO_COUNT = "metricas:tempo_count";
    private static final String KEY_TOP_FILMES = "metricas:top_filmes";
    private static final String KEY_RECENTES = "metricas:recentes";
    private static final int MAX_RECENTES = 10;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void processar(FilmeBuscadoEvent event) {
        redisTemplate.opsForValue().increment(KEY_TOTAL);

        if (Boolean.TRUE.equals(event.getCacheHit())) {
            redisTemplate.opsForValue().increment(KEY_CACHE_HITS);
        }

        if (event.getTempoMs() != null) {
            redisTemplate.opsForValue().increment(KEY_TEMPO_TOTAL, event.getTempoMs());
            redisTemplate.opsForValue().increment(KEY_TEMPO_COUNT);
        }

        if (event.getFilmeTitulo() != null) {
            redisTemplate.opsForZSet().incrementScore(KEY_TOP_FILMES, event.getFilmeTitulo(), 1);
        }

        try {
            MetricasDTO.BuscaRecenteDTO recente = MetricasDTO.BuscaRecenteDTO.builder()
                    .titulo(event.getFilmeTitulo())
                    .tempoMs(event.getTempoMs())
                    .cacheHit(event.getCacheHit())
                    .fallbackUsed(event.getFallbackUsed())
                    .timestamp(event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

            String json = objectMapper.writeValueAsString(recente);
            redisTemplate.opsForList().leftPush(KEY_RECENTES, json);
            redisTemplate.opsForList().trim(KEY_RECENTES, 0, MAX_RECENTES - 1);
        } catch (Exception e) {
            log.error("Erro ao salvar busca recente no Redis", e);
        }
    }

    public MetricasDTO getMetricas() {
        Long total = toLong(redisTemplate.opsForValue().get(KEY_TOTAL));
        Long cacheHits = toLong(redisTemplate.opsForValue().get(KEY_CACHE_HITS));
        Long tempoTotal = toLong(redisTemplate.opsForValue().get(KEY_TEMPO_TOTAL));
        Long tempoCount = toLong(redisTemplate.opsForValue().get(KEY_TEMPO_COUNT));

        double cacheHitRate = total != null && total > 0
                ? (cacheHits != null ? cacheHits * 100.0 / total : 0)
                : 0;

        double tempoMedio = tempoCount != null && tempoCount > 0
                ? (tempoTotal != null ? tempoTotal * 1.0 / tempoCount : 0)
                : 0;

        List<MetricasDTO.TopFilmeDTO> topFilmes = new ArrayList<>();
        Set<Object> top = redisTemplate.opsForZSet().reverseRange(KEY_TOP_FILMES, 0, 4);
        if (top != null) {
            top.forEach(titulo -> {
                Double score = redisTemplate.opsForZSet().score(KEY_TOP_FILMES, titulo);
                topFilmes.add(MetricasDTO.TopFilmeDTO.builder()
                        .titulo(titulo.toString())
                        .total(score != null ? score.longValue() : 0)
                        .build());
            });
        }

        List<MetricasDTO.BuscaRecenteDTO> recentes = new ArrayList<>();
        List<Object> recentesRaw = redisTemplate.opsForList().range(KEY_RECENTES, 0, MAX_RECENTES - 1);
        if (recentesRaw != null) {
            recentesRaw.forEach(item -> {
                try {
                    recentes.add(objectMapper.readValue(item.toString(), MetricasDTO.BuscaRecenteDTO.class));
                } catch (Exception e) {
                    log.error("Erro ao deserializar busca recente", e);
                }
            });
        }

        return MetricasDTO.builder()
                .totalCliques(total != null ? total : 0L)
                .cacheHitRate(Math.round(cacheHitRate * 10.0) / 10.0)
                .tempoMedioMs(Math.round(tempoMedio * 10.0) / 10.0)
                .topFilmes(topFilmes)
                .buscasRecentes(recentes)
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        return Long.parseLong(value.toString());
    }
}