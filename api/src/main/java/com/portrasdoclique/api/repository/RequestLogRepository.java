package com.portrasdoclique.api.repository;

import com.portrasdoclique.api.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    @Query("""
        SELECT r.filmeTitulo, COUNT(r) as total
        FROM RequestLog r
        WHERE r.filmeTitulo IS NOT NULL
        GROUP BY r.filmeTitulo
        ORDER BY total DESC
        LIMIT 5
    """)
    List<Object[]> findTopFilmes();

    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.cacheHit = true")
    Long countCacheHits();

    @Query("SELECT AVG(r.tempoMs) FROM RequestLog r WHERE r.tempoMs IS NOT NULL")
    Double findAvgTempoMs();
}