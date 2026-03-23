package com.jjp.jsu.qna;

public record QnaCreatePostRequest(
        String title,
        String content,
        boolean isPublic,
        String password
) {
}
