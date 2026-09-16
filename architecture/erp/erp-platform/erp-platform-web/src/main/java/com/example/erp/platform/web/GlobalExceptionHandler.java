package com.example.erp.platform.web;

import com.example.erp.platform.kernel.BizException;
import com.example.erp.platform.kernel.ErrorCode;
import com.example.erp.platform.kernel.Result;
import com.example.erp.shared.BusinessRuleViolationException;
import com.example.erp.shared.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ErrorCode.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({BusinessRuleViolationException.class, BizException.class})
    public ResponseEntity<Result<Void>> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Result.fail(ErrorCode.BUSINESS_RULE, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(Result.fail(ErrorCode.BAD_REQUEST, "invalid request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.INTERNAL, ex.getMessage()));
    }
}