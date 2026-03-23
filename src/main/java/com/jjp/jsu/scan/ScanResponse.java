package com.jjp.jsu.scan;

import java.util.List;

/**
 * 스캔 결과 응답.
 * {@link #of(List)}로 생성하면 severity별 카운트를 자동 집계합니다.
 */
public record ScanResponse(
        List<ScanItem> items,
        int totalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        int infoCount
) {
    /** items 목록으로부터 severity별 카운트를 집계해 ScanResponse를 생성합니다. */
    public static ScanResponse of(List<ScanItem> items) {
        int high   = (int) items.stream().filter(i -> "HIGH".equals(i.severity())).count();
        int medium = (int) items.stream().filter(i -> "MEDIUM".equals(i.severity())).count();
        int low    = (int) items.stream().filter(i -> "LOW".equals(i.severity())).count();
        int info   = (int) items.stream().filter(i -> "INFO".equals(i.severity())).count();
        return new ScanResponse(items, items.size(), high, medium, low, info);
    }
}
