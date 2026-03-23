package com.jjp.jsu.qna;

import java.util.List;

public record QnaPostDetailResponse(
        Long id,
        String title,
        String content,
        String status,
        boolean isPublic,
        String date,
        String updatedAt,
        boolean deleted,
        boolean editable,
        List<QnaReplyResponse> replies,
        List<QnaPostHistoryResponse> histories
) {
}
