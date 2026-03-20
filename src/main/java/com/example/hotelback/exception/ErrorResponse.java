package com.example.hotelback.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId,
        List<ValidationError> validationErrors
) {
    @Builder
    public record ValidationError(
            String field,
            String message,
            Object rejectedValue
    ) {
    }
}
