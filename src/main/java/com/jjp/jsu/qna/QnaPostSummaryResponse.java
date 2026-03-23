package com.jjp.jsu.qna;

public record QnaPostSummaryResponse(
        Long id,
        String title,
        String status,
        boolean isPublic,
        String date,
        boolean deleted
) {
}
