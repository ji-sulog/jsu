package com.jjp.jsu.naming;

import java.util.List;

/**
 * 네이밍 검사 응답 DTO.
 */
public record NamingCheckResponse(
        List<NamingIssue> items,
        int totalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        int typeCount,
        int methodCount,
        int fieldCount,
        int meaningCount
) {}
