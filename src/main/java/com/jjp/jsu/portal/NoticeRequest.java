package com.jjp.jsu.portal;

/**
 * 공지 등록/수정 요청 DTO.
 * type: UPDATE / GUIDE / PLAN
 */
public record NoticeRequest(
        String type,
        String title,
        String body,
        String displayDate,
        Integer sortOrder,
        boolean pinned
) {}
