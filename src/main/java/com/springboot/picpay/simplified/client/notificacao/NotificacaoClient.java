package com.springboot.picpay.simplified.client.notificacao;

import com.springboot.picpay.simplified.dto.request.NotificacaoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class NotificacaoClient {

    private final RestClient restClient;

    public void notificar(NotificacaoRequestDTO dto) {
        try {
            restClient.post()
                    .uri("/api/v1/notify")
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignore) {

        }
    }
}
