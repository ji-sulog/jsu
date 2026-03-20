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

-- ── Q&A 게시글 ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS qna_post (
    id           BIGSERIAL    PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    is_public    BOOLEAN      NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(64) NULL,         -- 비공개 글의 SHA-256(비밀번호)
    status       VARCHAR(20)  NOT NULL DEFAULT 'WAITING',  -- WAITING | ANSWERED
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);


ALTER TABLE qna_post
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

UPDATE qna_post
SET updated_at = created_at
WHERE updated_at IS NULL;

CREATE TABLE IF NOT EXISTS qna_post_history (
    id         BIGSERIAL    PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES qna_post(id),
    action     VARCHAR(20)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    changed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qna_post_history_post_id ON qna_post_history(post_id);


-- ── Q&A 답변 (관리자 전용) ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS qna_reply (
    id         BIGSERIAL    PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES qna_post(id) ON DELETE CASCADE,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qna_reply_post_id ON qna_reply(post_id);

-- ── 개발자 로그 ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS devlog (
    id         BIGSERIAL    PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    tags       VARCHAR(500) NULL,          -- 쉼표 구분, 예: "Spring,Flyway,DB"
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
