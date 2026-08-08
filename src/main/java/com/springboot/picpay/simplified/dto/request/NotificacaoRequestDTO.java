package com.springboot.picpay.simplified.dto.request;

import java.math.BigDecimal;

public record NotificacaoRequestDTO(
        String remetente,
        String destinatario,
        BigDecimal valor

) {
}
