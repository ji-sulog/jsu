package com.jjp.jsu.common;

/**
 * 잘못된 요청 (400 Bad Request) 공통 예외.
 * 각 모듈의 BadRequestException은 이 클래스를 상속합니다.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
