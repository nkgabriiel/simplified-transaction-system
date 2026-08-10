package com.springboot.picpay.simplified.client.notificacao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificacaoEventListener {

    private final NotificacaoClient notificacaoClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRealizarTransacao(TransacaoRealizadaEvent event) {
        notificacaoClient.notificar(event.notificacao());
    }
}
