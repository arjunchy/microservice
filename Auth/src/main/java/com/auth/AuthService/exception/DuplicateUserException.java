package com.auth.AuthService.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends RuntimeException {

    private static final HttpStatus status = HttpStatus.CONFLICT;

    public DuplicateUserException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }

}