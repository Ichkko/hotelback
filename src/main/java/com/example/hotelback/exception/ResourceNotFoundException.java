package com.example.hotelback.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
