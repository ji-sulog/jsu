package com.jjp.jsu.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(ForbiddenException e) {
        return ResponseEntity.status(403).body(new ApiErrorResponse(e.getMessage()));
    }
}
