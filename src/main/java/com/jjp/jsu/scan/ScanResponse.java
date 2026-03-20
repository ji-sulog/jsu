package com.jjp.jsu.scan;

import java.util.List;

/**
 * 스캔 결과 응답.
 */
public record ScanResponse(
        List<ScanItem> items,
        int totalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        int infoCount
) {}
