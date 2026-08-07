package com.springboot.picpay.simplified.client;

import com.springboot.picpay.simplified.dto.response.AutorizacaoResponseDTO;
import com.springboot.picpay.simplified.exception.UnavailableAuthorizerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class AutorizacaoClient {

    private final RestClient restClient;

    public boolean autorizar() {
        try{
            AutorizacaoResponseDTO response = restClient.get()
                    .uri("/api/v2/authorize")
                    .retrieve()
                    .body(AutorizacaoResponseDTO.class);

            return response != null
                    && response.data() != null
                    && Boolean.TRUE.equals(response.data().authorization());

        } catch(RestClientException e) {
            throw new UnavailableAuthorizerException("Não foi possível consultar o serviço de autorização", e);
        }
    }
}
