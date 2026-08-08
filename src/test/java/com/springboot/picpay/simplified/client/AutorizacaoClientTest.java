package com.springboot.picpay.simplified.client;

import com.springboot.picpay.simplified.exception.UnavailableAuthorizerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


public class AutorizacaoClientTest {

    private MockRestServiceServer mockServer;
    private AutorizacaoClient autorizacaoClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://util.devi.tools");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        autorizacaoClient = new AutorizacaoClient(builder.build());
    }


    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve retornar false quando autorizador recusar")
    void deveRetornarFalseQuandoRecusado() {
        mockServer.expect(requestTo("https://util.devi.tools/api/v2/authorize"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                        "status":"success",
                        "data":{"authorization":false}
                        }
                        """, MediaType.APPLICATION_JSON));

        boolean resultado = autorizacaoClient.autorizar();
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exception quando serviço estiver indisponível")
    void deveLancarExceptionQuandoServicoIndisponivel() {
        mockServer.expect(requestTo("https://util.devi.tools/api/v2/authorize"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> autorizacaoClient.autorizar())
                .isInstanceOf(UnavailableAuthorizerException.class);
    }

    @Test
    @DisplayName("Deve retornar false quando \"data\" for null")
    void deveLancarFalseQuandoDataForNull() {
        mockServer.expect(requestTo("https://util.devi.tools/api/v2/authorize"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                        "status":"success",
                        "data":null
                        }
                        """, MediaType.APPLICATION_JSON));

        boolean resultado = autorizacaoClient.autorizar();
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando autorizador aprovar")
    void deveLancarTrueQuandoAutorizadorAprovar() {
        mockServer.expect(requestTo("https://util.devi.tools/api/v2/authorize"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                        "status":"success",
                        "data":{"authorization":true}
                        }
                        """, MediaType.APPLICATION_JSON));

        boolean resultado = autorizacaoClient.autorizar();
        assertThat(resultado).isTrue();
    }

}
