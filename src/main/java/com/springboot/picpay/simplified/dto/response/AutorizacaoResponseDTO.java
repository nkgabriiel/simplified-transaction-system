package com.springboot.picpay.simplified.dto.response;

public record AutorizacaoResponseDTO(String status, AuthorizationData data) {
    public record AuthorizationData(Boolean authorization) {

    }
}
