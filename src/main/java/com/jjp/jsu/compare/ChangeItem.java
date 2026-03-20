package com.jjp.jsu.compare;

import java.util.List;

/**
 * 요구사항 변경 항목 하나를 나타내는 DTO.
 *
 * @param type       변경 유형: ADDED / REMOVED / MODIFIED
 * @param score      규칙 엔진 합산 점수
 * @param priority   중요도: HIGH / MEDIUM / LOW / GENERAL
 * @param oldContent 이전 내용 (ADDED 일 때는 빈 문자열)
 * @param newContent 변경된 내용 (REMOVED 일 때는 빈 문자열)
 * @param reasons    적용된 규칙 이유 목록
 */
public record ChangeItem(
        String type,
        int score,
        String priority,
        String oldContent,
        String newContent,
        List<String> reasons
) {}
