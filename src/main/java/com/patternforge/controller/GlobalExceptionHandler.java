package com.patternforge.controller;

import com.patternforge.pattern.composite.CircularReferenceException;
import com.patternforge.pattern.composite.NodeNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CircularReferenceException.class)
    public ResponseEntity<ErrorResponse> handleCircularRef(CircularReferenceException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("CIRCULAR_REFERENCE", e.getMessage()));
    }

    @ExceptionHandler(NodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NodeNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupported(UnsupportedOperationException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("UNSUPPORTED_OPERATION", e.getMessage()));
    }

    record ErrorResponse(String code, String message) {}
}
