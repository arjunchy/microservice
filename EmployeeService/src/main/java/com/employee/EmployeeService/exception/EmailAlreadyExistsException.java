package com.employee.EmployeeService.exception;


import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends RuntimeException{

    private String message;
    private HttpStatus status;

    public EmailAlreadyExistsException(String message) {
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST;

    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}