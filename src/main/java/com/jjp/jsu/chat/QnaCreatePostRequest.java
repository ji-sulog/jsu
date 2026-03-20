package com.jjp.jsu.chat;

public record QnaCreatePostRequest(
        String title,
        String content,
        boolean isPublic,
        String password
) {
}
