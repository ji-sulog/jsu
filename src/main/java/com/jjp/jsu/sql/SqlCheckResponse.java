package com.jjp.jsu.sql;

import java.util.List;

public record SqlCheckResponse(
        List<SqlIssue> issues,
        int total,
        int highCount,
        int mediumCount,
        int lowCount
) {}
