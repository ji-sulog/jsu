-- ── tool 테이블 ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tool (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    href         VARCHAR(255),
    status       VARCHAR(255) NOT NULL,
    sort_order   INTEGER      NOT NULL,
    icon         VARCHAR(255) NOT NULL,
    description  TEXT,
    tags         VARCHAR(255),
    since        VARCHAR(255),
    status_label VARCHAR(255),
    button_label VARCHAR(255),
    status_class VARCHAR(255)
);

-- ── notice 테이블 ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notice (
    id           BIGSERIAL    PRIMARY KEY,
    type         VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    body         TEXT,
    display_date VARCHAR(255),
    sort_order   INTEGER      NOT NULL,
    pinned       BOOLEAN      NOT NULL DEFAULT FALSE
);
