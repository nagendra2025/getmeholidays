package com.example.getmeholidays.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex,
            HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                "INVALID_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamService(
            UpstreamServiceException ex,
            HttpServletRequest request) {

        // Map upstream status to something meaningful; 4xx -> 502 or 400 depending on your choice
        HttpStatus status = (ex.getStatusCode() >= 500)
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.BAD_REQUEST;

        ErrorResponse body = new ErrorResponse(
                "UPSTREAM_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                status.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Unexpected error occurred",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        // In a real project, log full stacktrace here
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
