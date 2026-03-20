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

-- ── Q&A 답변 (관리자 전용) ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS qna_reply (
    id         BIGSERIAL    PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES qna_post(id) ON DELETE CASCADE,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qna_reply_post_id ON qna_reply(post_id);
