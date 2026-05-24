package com.example.hotelback.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends DomainException {

    public BadRequestException(ErrorCode code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
