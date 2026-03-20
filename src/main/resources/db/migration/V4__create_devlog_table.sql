-- ── 개발자 로그 ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS devlog (
    id         BIGSERIAL    PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    tags       VARCHAR(500) NULL,          -- 쉼표 구분, 예: "Spring,Flyway,DB"
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
