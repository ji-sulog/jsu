package com.jjp.jsu.compare;

/**
 * 비교 요청 DTO.
 *
 * @param oldText 이전 요구사항 문서 전체 텍스트
 * @param newText 최신 요구사항 문서 전체 텍스트
 */
public record CompareRequest(String oldText, String newText) {}
