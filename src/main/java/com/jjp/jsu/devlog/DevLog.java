package com.jjp.jsu.devlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "devlog")
@Getter
@NoArgsConstructor
public class DevLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 쉼표 구분 태그, 예: "Spring,Flyway,DB" */
    @Column(length = 500)
    private String tags;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DevLog(String title, String content, String tags) {
        this.title     = title;
        this.content   = content;
        this.tags      = tags;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, String tags) {
        this.title     = title;
        this.content   = content;
        this.tags      = tags;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getTagList() {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
