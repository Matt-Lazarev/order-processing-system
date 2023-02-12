package com.lazarev.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleProductNotFoundException(RuntimeException exception){
        Map<String, Object> errors = getDefaultErrorMap(exception.getMessage(), HttpStatus.NOT_FOUND);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errors);
    }

    private Map<String, Object> getDefaultErrorMap(String message, HttpStatus status){
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("timestamp", ZonedDateTime.now(ZoneId.systemDefault()));
        errors.put("status", status.name());
        errors.put("message", message);
        return errors;
    }
}
