package com.portrasdoclique.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmeDTO {
    private Integer id;
    private String titulo;
    private String tituloOriginal;
    private String sinopse;
    private String posterPath;
    private String posterUrl;
    private Double nota;
    private String dataLancamento;
    private String youtubeVideoId;
    private String rottenTomatoes;
    private String metacritic;
    private String premios;
    private long tempoTotalMs;
    private String fonte;
}