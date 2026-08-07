package com.springboot.picpay.simplified.dto.response;

import com.springboot.picpay.simplified.model.TipoUsuario;

import java.math.BigDecimal;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String documento,
        String email,
        BigDecimal saldo,
        TipoUsuario tipoUsuario
) {
}
