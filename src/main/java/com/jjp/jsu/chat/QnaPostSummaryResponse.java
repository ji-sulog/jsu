package com.jjp.jsu.chat;

public record QnaPostSummaryResponse(
        Long id,
        String title,
        String status,
        boolean isPublic,
        String date,
        boolean deleted
) {
}
