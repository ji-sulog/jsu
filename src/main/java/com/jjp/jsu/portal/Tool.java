package com.jjp.jsu.portal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 포털 사이드바 및 대시보드 도구 카드에 노출되는 도구 목록 엔티티.
 * status: ACTIVE(운영 중) / COMING_SOON(준비 중)
 */
@Entity
@Table(name = "tool")
@Getter
@NoArgsConstructor
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** 페이지 경로 — COMING_SOON 도구는 null */
    private String href;

    /** ACTIVE / COMING_SOON */
    @Column(nullable = false)
    private String status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 사이드바에 표시할 이모지 아이콘 */
    @Column(nullable = false)
    private String icon;

    /** 도구 카드 설명 텍스트 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 태그 목록 (쉼표 구분, 예: "LCS Diff,규칙 엔진,파일 업로드") */
    private String tags;

    /** 오픈 시기 표시 (예: "2026.03") */
    private String since;

    /** 상태 배지 레이블 (예: "● 운영 중", "준비 중", "AI 연동 예정") */
    @Column(name = "status_label")
    private String statusLabel;

    /** 버튼 레이블 (예: "도구 열기", "개발 예정", "AI 연동 후 오픈") */
    @Column(name = "button_label")
    private String buttonLabel;

    /** CSS 클래스 구분 (live / soon / planned) */
    @Column(name = "status_class")
    private String statusClass;

    /** SVG 아이콘 식별자 — DB ID에 의존하지 않는 고정 키 (compare / scan / sql / naming / log-analyzer) */
    @Column(name = "icon_type")
    private String iconType;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /** 태그 문자열을 리스트로 변환 */
    public List<String> getTagList() {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.stream(tags.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .toList();
    }
}
