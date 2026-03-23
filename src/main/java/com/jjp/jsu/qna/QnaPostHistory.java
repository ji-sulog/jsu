package com.jjp.jsu.qna;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_post_history")
@Getter
@NoArgsConstructor
public class QnaPostHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private QnaPost post;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public QnaPostHistory(QnaPost post, String action, String title, String content) {
        this.post = post;
        this.action = action;
        this.title = title;
        this.content = content;
        this.changedAt = LocalDateTime.now();
    }
}
