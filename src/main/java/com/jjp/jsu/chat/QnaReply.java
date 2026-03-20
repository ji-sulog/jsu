package com.jjp.jsu.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_reply")
@Getter
@NoArgsConstructor
public class QnaReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private QnaPost post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public QnaReply(QnaPost post, String content) {
        this.post      = post;
        this.content   = content;
        this.createdAt = LocalDateTime.now();
    }
}
