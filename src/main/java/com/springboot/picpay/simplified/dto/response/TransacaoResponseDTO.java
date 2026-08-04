package com.springboot.picpay.simplified.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponseDTO(
        Long id,
        Long remetenteId,
        Long destinatarioId,
        BigDecimal valor,
        LocalDateTime hora

) {
}
