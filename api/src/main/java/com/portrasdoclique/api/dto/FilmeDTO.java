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
    // Identificação
    private Integer id;
    private String titulo;
    private String tituloOriginal;

    // Conteúdo principal
    private String sinopse;
    private String posterPath;
    private String posterUrl;
    private String backdropPath;
    private String backdropUrl;

    // Informações adicionais
    private String idioma;
    private String generos;
    private Integer duracao;
    private String pais;

    // Avaliações
    private Double nota;
    private Integer votos;
    private String rottenTomatoes;
    private String metacritic;
    private String premios;

    // Datas
    private String dataLancamento;
    private String dataAtualizacao;

    // Mídia
    private String youtubeVideoId;

    // Popularidade
    private Double popularidade;
    private Double votacaoMedia;
    private Boolean emAlta;

    // Dados de Execução
    private long tempoTotalMs;
    private String fonte;
    private String statusFonte;
    private Integer tentativasRequisicao;
    private String fallbackUtilizado;

    // Rastreamento
    private String requestId;
    private String ipOrigem;
    private String timestamp;
    private Boolean cacheHit;
}