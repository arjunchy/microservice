package com.employee.EmployeeService.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, HttpStatus status){
        this.message = message;
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}