CREATE TABLE request_logs (
    id            BIGSERIAL PRIMARY KEY,
    filme_titulo  VARCHAR(255),
    filme_id      INTEGER,
    ip            VARCHAR(45),
    tempo_ms      INTEGER,
    cache_hit     BOOLEAN DEFAULT FALSE,
    fallback_used BOOLEAN DEFAULT FALSE,
    tmdb_ok       BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_request_logs_created_at ON request_logs(created_at DESC);
CREATE INDEX idx_request_logs_filme_titulo ON request_logs(filme_titulo);
