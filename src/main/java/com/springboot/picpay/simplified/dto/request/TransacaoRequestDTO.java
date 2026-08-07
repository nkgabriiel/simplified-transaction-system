package com.springboot.picpay.simplified.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransacaoRequestDTO(
        @NotNull(message = "Remetente é obrigatório")
        Long remetenteId,

        @NotNull(message = "Destinatário é obrigatório")
        Long destinatarioId,

        @NotNull
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor
) {
}
