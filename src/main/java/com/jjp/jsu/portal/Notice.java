package com.jjp.jsu.portal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 공지 및 업데이트 게시판 엔티티.
 * type: UPDATE(업데이트) / GUIDE(가이드) / PLAN(예정)
 * pinned=true 인 항목은 상단 배너로 표시
 */
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UPDATE / GUIDE / PLAN */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    /** 배너 표시용 본문 (pinned 항목에만 사용) */
    @Column(columnDefinition = "TEXT")
    private String body;

    /** 표시 날짜 문자열 (예: "2026.03", "2026.Q2 예정") */
    @Column(name = "display_date")
    private String displayDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** true 면 대시보드 상단 파란 배너로 표시 */
    private boolean pinned;

    /** 타입에 따른 한글 레이블 */
    public String getTypeLabel() {
        return switch (type) {
            case "UPDATE" -> "업데이트";
            case "GUIDE"  -> "가이드";
            case "PLAN"   -> "예정";
            default       -> type;
        };
    }

    /** 타입에 따른 CSS 클래스 */
    public String getTypeClass() {
        return switch (type) {
            case "UPDATE" -> "update";
            case "GUIDE"  -> "guide";
            case "PLAN"   -> "plan";
            default       -> "update";
        };
    }
}
