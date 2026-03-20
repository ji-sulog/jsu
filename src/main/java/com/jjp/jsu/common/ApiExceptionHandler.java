package com.jjp.jsu.common;

import com.jjp.jsu.chat.QnaAccessDeniedException;
import com.jjp.jsu.chat.QnaBadRequestException;
import com.jjp.jsu.devlog.DevLogAccessDeniedException;
import com.jjp.jsu.devlog.DevLogBadRequestException;
import com.jjp.jsu.devlog.DevLogController;
import com.jjp.jsu.chat.QnaController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {QnaController.class, DevLogController.class})
public class ApiExceptionHandler {

    @ExceptionHandler({QnaBadRequestException.class, DevLogBadRequestException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({QnaAccessDeniedException.class, DevLogAccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(RuntimeException e) {
        return ResponseEntity.status(403).body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
