package com.address.AddressService.exception;

import org.springframework.http.HttpStatus;

public class AddressNotFoundException extends RuntimeException{

    private String message;
    private HttpStatus status;

    public AddressNotFoundException(String message){
        this.message = message;
        this.status = HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
