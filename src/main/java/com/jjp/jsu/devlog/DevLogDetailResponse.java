package com.jjp.jsu.devlog;

import java.util.List;

public record DevLogDetailResponse(
        Long id,
        String title,
        String content,
        List<String> tags,
        String createdAt,
        String updatedAt
) {
}
