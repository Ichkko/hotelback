package com.example.hotelback.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(ErrorCode code, String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }
}
