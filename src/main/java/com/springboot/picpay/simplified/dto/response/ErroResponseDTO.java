package com.springboot.picpay.simplified.dto.response;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErroResponseDTO(
        String timestamp,
        int status,
        String error,
        String message,
        String path
){
    public static ErroResponseDTO of(HttpStatus status, String message, String path) {
        return new ErroResponseDTO(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
