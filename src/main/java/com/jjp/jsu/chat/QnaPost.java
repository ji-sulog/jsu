package com.jjp.jsu.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_post")
@Getter
@NoArgsConstructor
public class QnaPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_public", nullable = false)
    private boolean publicPost;   // DB 컬럼명: is_public

    /** 비공개 글의 SHA-256(비밀번호) — 공개글은 null */
    @Column(name = "password_hash", length = 64)
    private String passwordHash;

    /** WAITING | ANSWERED */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public QnaPost(String title, String content, boolean publicPost, String passwordHash) {
        this.title        = title;
        this.content      = content;
        this.publicPost   = publicPost;
        this.passwordHash = passwordHash;
        this.status       = "WAITING";
        this.createdAt    = LocalDateTime.now();
    }

    public void markAnswered() {
        this.status = "ANSWERED";
    }
}
