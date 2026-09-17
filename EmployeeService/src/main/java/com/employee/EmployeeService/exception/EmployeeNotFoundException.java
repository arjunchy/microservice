package com.employee.EmployeeService.exception;


import org.springframework.http.HttpStatus;

public class EmployeeNotFoundException extends RuntimeException{

    private String message;
    private HttpStatus status;

    public EmployeeNotFoundException(String message) {
        this.message = message;
        this.status = HttpStatus.NOT_FOUND;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}