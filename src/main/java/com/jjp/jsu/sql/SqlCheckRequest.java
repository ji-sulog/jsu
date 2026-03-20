package com.jjp.jsu.sql;

public record SqlCheckRequest(
        String sql,
        String customPatterns  // 쉼표 구분 사용자 금지 패턴 (nullable)
) {}
