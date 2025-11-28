package com.example.getmeholidays.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String error;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;

    public ErrorResponse(String error, String message, String path, int status, LocalDateTime timestamp) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

