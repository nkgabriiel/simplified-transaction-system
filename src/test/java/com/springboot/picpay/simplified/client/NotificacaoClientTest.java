package com.springboot.picpay.simplified.client;


import com.springboot.picpay.simplified.client.notificacao.NotificacaoClient;
import com.springboot.picpay.simplified.dto.request.NotificacaoRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class NotificacaoClientTest {

    private MockRestServiceServer mockServer;
    private NotificacaoClient notificacaoClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://util.devi.tools");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        notificacaoClient = new NotificacaoClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @Test
    @DisplayName("Não deve lançar exception quando serviço estiver indisponível")
    void naoDeveLancarExceptionServicoIndisponivel() {
        NotificacaoRequestDTO dto = new NotificacaoRequestDTO("remetente", "destinatario", new BigDecimal("10.00"));
        mockServer.expect(requestTo("https://util.devi.tools/api/v1/notify"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatCode(() -> notificacaoClient.notificar(dto))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar notificação quando serviço estiver disponível")
    void deveLancarSucessoQuandoServicoDisponivel() {
        NotificacaoRequestDTO dto = new NotificacaoRequestDTO("Kaiser", "Lorenzo", new BigDecimal("10.00"));
        mockServer.expect(requestTo("https://util.devi.tools/api/v1/notify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(
                        """
                                {
                                "remetente": "Kaiser",
                                "destinatario": "Lorenzo",
                                "valor": 10.00
                                }
                                """
                ))
                .andRespond(withSuccess());

        notificacaoClient.notificar(dto);
    }

}
