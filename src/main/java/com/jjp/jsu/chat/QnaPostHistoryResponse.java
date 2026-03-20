package com.jjp.jsu.chat;

public record QnaPostHistoryResponse(
        Long id,
        String action,
        String title,
        String content,
        String changedAt
) {
}
