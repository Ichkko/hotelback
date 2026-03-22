package com.example.hotelback.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends DomainException {

    public TooManyRequestsException(ErrorCode code, String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, code, message);
    }
}
