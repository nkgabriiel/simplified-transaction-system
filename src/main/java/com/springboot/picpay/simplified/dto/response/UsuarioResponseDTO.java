package com.springboot.picpay.simplified.dto.response;

import java.math.BigDecimal;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String documento,
        String email,
        BigDecimal saldo,
        String tipoUsuario
) {
}
