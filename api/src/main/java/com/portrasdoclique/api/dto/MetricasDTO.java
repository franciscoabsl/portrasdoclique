package com.portrasdoclique.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasDTO {
    private Long totalCliques;
    private Double cacheHitRate;
    private Double tempoMedioMs;
    private List<TopFilmeDTO> topFilmes;
    private List<BuscaRecenteDTO> buscasRecentes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFilmeDTO {
        private String titulo;
        private Long total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuscaRecenteDTO {
        private String titulo;
        private Integer tempoMs;
        private Boolean cacheHit;
        private Boolean fallbackUsed;
        private String timestamp;
    }
}