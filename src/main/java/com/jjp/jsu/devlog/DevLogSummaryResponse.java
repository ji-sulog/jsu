package com.jjp.jsu.devlog;

import java.util.List;

public record DevLogSummaryResponse(
        Long id,
        String title,
        List<String> tags,
        String date
) {
}
