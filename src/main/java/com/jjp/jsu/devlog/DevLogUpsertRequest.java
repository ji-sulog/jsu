package com.jjp.jsu.devlog;

public record DevLogUpsertRequest(
        String title,
        String content,
        String tags
) {
}
