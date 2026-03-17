package com.portrasdoclique.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmeBuscadoEvent implements Serializable {

    private String filmeTitulo;
    private Integer filmeId;
    private String ip;
    private Integer tempoMs;
    private Boolean cacheHit;
    private Boolean fallbackUsed;
    private Boolean tmdbOk;
    private LocalDateTime timestamp;
}