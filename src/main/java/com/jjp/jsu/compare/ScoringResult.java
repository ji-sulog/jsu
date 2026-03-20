package com.jjp.jsu.compare;

import java.util.List;

/**
 * 규칙 엔진이 변경 항목 하나에 대해 평가한 결과.
 *
 * @param score    합산 점수 (R01~R10 규칙 적용 결과)
 * @param priority 중요도 레이블: HIGH(7+) / MEDIUM(4-6) / LOW(1-3) / GENERAL(0)
 * @param reasons  적용된 규칙 이유 목록 (예: ["주체 또는 권한 관련 변경", "숫자 또는 제한값 변경"])
 */
public record ScoringResult(int score, String priority, List<String> reasons) {}
