package com.jjp.jsu.qna;

public record QnaPostHistoryResponse(
        Long id,
        String action,
        String title,
        String content,
        String changedAt
) {
}
