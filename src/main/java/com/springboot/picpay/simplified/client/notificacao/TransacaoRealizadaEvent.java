package com.springboot.picpay.simplified.client.notificacao;

import com.springboot.picpay.simplified.dto.request.NotificacaoRequestDTO;

public record TransacaoRealizadaEvent(NotificacaoRequestDTO notificacao) {
}
