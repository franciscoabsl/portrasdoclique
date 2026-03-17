package com.portrasdoclique.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filme_titulo")
    private String filmeTitulo;

    @Column(name = "filme_id")
    private Integer filmeId;

    @Column(name = "ip")
    private String ip;

    @Column(name = "tempo_ms")
    private Integer tempoMs;

    @Column(name = "cache_hit")
    private Boolean cacheHit;

    @Column(name = "fallback_used")
    private Boolean fallbackUsed;

    @Column(name = "tmdb_ok")
    private Boolean tmdbOk;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}