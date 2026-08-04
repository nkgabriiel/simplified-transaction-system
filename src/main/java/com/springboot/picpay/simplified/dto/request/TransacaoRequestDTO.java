package com.springboot.picpay.simplified.dto.request;

import java.math.BigDecimal;

public record TransacaoRequestDTO(
        Long remetenteId,
        Long destinatarioId,
        BigDecimal valor
) {
}
