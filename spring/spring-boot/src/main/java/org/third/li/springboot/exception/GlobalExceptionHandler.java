package org.third.li.springboot.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // 直接返回 JSON 响应，避免转发到 /error
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }

}