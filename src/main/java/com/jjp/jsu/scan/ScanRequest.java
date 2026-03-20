package com.jjp.jsu.scan;

import java.util.List;

/**
 * 스캔 요청.
 *
 * @param code            분석할 소스코드/설정 텍스트
 * @param language        언어 힌트 (java / javascript / sql / config / text)
 * @param fileName        파일명 (선택 — WT 규칙 적용에 활용)
 * @param customKeywords  사용자 정의 탐지 키워드 목록 (고객사 코드, 조직 코드 등)
 * @param excludeKeywords 제외 키워드 목록 — matchedText 에 포함된 경우 해당 항목을 결과에서 제외
 */
public record ScanRequest(
        String code,
        String language,
        String fileName,
        List<String> customKeywords,
        List<String> excludeKeywords
) {}
