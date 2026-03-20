package com.jjp.jsu.sql;

/**
 * SQL 품질 검사 결과 단건.
 */
public record SqlIssue(
        String ruleId,       // 예: SQ01
        String severity,     // HIGH / MEDIUM / LOW
        int    line,         // 탐지된 라인 번호 (0 = 전체 쿼리)
        String lineText,     // 해당 라인 원문 (최대 120자)
        String title,        // 규칙 제목
        String description   // 상세 설명
) {}
