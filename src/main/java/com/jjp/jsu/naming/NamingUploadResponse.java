package com.jjp.jsu.naming;

/**
 * 파일 업로드 응답 DTO.
 */
public record NamingUploadResponse(
        String text,
        String filename,
        String error
) {}
