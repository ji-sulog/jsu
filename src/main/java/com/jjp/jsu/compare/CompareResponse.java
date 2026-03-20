package com.jjp.jsu.compare;

import java.util.List;

/**
 * 비교 결과 DTO.
 * 변경 항목은 점수(높은 순)로 정렬되어 반환됩니다.
 */
public record CompareResponse(
        List<ChangeItem> changes,
        int totalChanges,
        int highCount,
        int mediumCount,
        int lowCount,
        int generalCount
) {}
