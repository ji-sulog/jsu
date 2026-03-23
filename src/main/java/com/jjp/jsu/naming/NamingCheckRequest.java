package com.jjp.jsu.naming;

/**
 * 네이밍 검사 요청 DTO.
 *
 * @param code                검사할 소스코드
 * @param fileName            파일명 (선택)
 * @param language            언어 (JAVA — 현재 1차 지원)
 * @param strictMode          엄격 모드 (true이면 LOW 규칙도 강하게 적용)
 * @param checkMeaningQuality 의미 품질 규칙(NM-005~NM-008) 사용 여부
 * @param checkBooleanNaming  boolean 이름 규칙(NM-005) 사용 여부
 */
public record NamingCheckRequest(
        String code,
        String fileName,
        String language,
        boolean strictMode,
        boolean checkMeaningQuality,
        boolean checkBooleanNaming
) {}
