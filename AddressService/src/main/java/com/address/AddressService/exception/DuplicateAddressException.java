package com.address.AddressService.exception;

import org.springframework.http.HttpStatus;

public class DuplicateAddressException extends RuntimeException{

    private String message;
    private HttpStatus status;

    public DuplicateAddressException(String message){
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
