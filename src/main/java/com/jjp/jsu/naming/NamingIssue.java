package com.jjp.jsu.naming;

/**
 * 탐지된 네이밍 위반 항목 1건.
 *
 * @param ruleId         규칙 ID (NM-001 ~ NM-008)
 * @param ruleName       규칙명
 * @param category       분류 (TYPE / METHOD / FIELD / MEANING)
 * @param severity       등급 (HIGH / MEDIUM / LOW)
 * @param lineNumber     탐지된 라인 번호
 * @param lineContent    탐지된 라인 원문
 * @param matchedText    탐지 근거 식별자 (문제가 되는 이름)
 * @param message        탐지 이유
 * @param recommendation 권장 조치
 * @param suggestedName  개선된 이름 제안 (null 가능)
 */
public record NamingIssue(
        String ruleId,
        String ruleName,
        String category,
        String severity,
        int lineNumber,
        String lineContent,
        String matchedText,
        String message,
        String recommendation,
        String suggestedName
) {}
