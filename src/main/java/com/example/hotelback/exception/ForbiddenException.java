package com.example.hotelback.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends DomainException {

    public ForbiddenException(ErrorCode code, String message) {
        super(HttpStatus.FORBIDDEN, code, message);
    }
}
