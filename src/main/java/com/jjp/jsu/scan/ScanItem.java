package com.jjp.jsu.scan;

/**
 * 탐지된 하드코딩 / 특화 로직 항목 1건.
 *
 * @param ruleId         규칙 ID (HC-001, CL-002 …)
 * @param ruleName       규칙명
 * @param category       분류 (HARDCODING / CUSTOMIZATION / REFACTORING)
 * @param lineNumber     탐지된 라인 번호
 * @param lineContent    탐지된 라인 원문
 * @param matchedText    탐지 근거 텍스트 (매칭된 부분)
 * @param reason         탐지 이유 (왜 문제인가)
 * @param recommendation 권장 조치
 * @param score          최종 점수 (가중치 적용 후)
 * @param severity       등급 (HIGH / MEDIUM / LOW / INFO)
 */
public record ScanItem(
        String ruleId,
        String ruleName,
        String category,
        int lineNumber,
        String lineContent,
        String matchedText,
        String reason,
        String recommendation,
        int score,
        String severity
) {}
