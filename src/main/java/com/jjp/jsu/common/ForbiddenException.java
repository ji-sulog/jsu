package com.jjp.jsu.common;

/**
 * 접근 거부 (403 Forbidden) 공통 예외.
 * 각 모듈의 AccessDeniedException은 이 클래스를 상속합니다.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
