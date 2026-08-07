package com.springboot.picpay.simplified.dto.request;

import java.math.BigDecimal;

public record UsuarioRequestDTO(
        String nome,
        String email,
        String documento,
        BigDecimal saldo
) {
}
