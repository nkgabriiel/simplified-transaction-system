package com.springboot.picpay.simplified.dto.response;

public record ErroResponseDTO(
        String timestamp,
        int status,
        String error,
        String message,
        String path
){
}
