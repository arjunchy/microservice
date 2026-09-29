package com.employee.EmployeeService.exception;

import org.springframework.http.HttpStatus;

public class AddressServiceUnavailableException extends RuntimeException {

    private final String message;
    private final HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

    public AddressServiceUnavailableException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}