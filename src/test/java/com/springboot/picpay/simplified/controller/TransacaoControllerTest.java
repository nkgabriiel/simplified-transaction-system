package com.springboot.picpay.simplified.controller;

import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.exception.*;
import com.springboot.picpay.simplified.service.TransacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
public class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Deve gerar uma transação com sucesso")
            void deveGerarTransacaoComSucesso() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"));
        TransacaoResponseDTO response = new TransacaoResponseDTO(1L, 1L, 2L, new BigDecimal("10.00"), LocalDateTime.now());

        when(transacaoService.gerarTransacao(any())).thenReturn(response);

        mockMvc.perform(post("/api/transacoes/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.remetenteId").value(1))
                .andExpect(jsonPath("$.destinatarioId").value(2));

        verify(transacaoService).gerarTransacao(dto);
    }

    @Test
    @DisplayName("Deve receber uma lista de transações de um usuário")
    void deveReceberListaTransacoesDeUsuario() throws Exception {
        TransacaoResponseDTO primeiraTransacao = new TransacaoResponseDTO(10L, 1L, 2L, new BigDecimal("10.00"), LocalDateTime.now());
        TransacaoResponseDTO segundaTransacao = new TransacaoResponseDTO(11L, 2L, 1L, new BigDecimal("20.00"), LocalDateTime.now());

        List<TransacaoResponseDTO> transacoes = List.of(primeiraTransacao, segundaTransacao);
        given(transacaoService.listarTodasPorUsuario(1L)).willReturn(transacoes);

        mockMvc.perform(get("/api/transacoes")
                .param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].remetenteId").value(1))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].remetenteId").value(2));

        verify(transacaoService).listarTodasPorUsuario(1L);
    }

    @Test
    @DisplayName("Deve retornar erro quando valores preenchidos forem errados")
    void deveRetornarErroQuandoValoresErrados() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/transacoes/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transacaoService);

    }

   @Test
   @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExceptionQuandoUsuarioNaoEncontrado() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"));

       when(transacaoService.gerarTransacao(dto)).thenThrow(new UsuarioNotFoundException("Usuário não foi encontrado com o id: 1"));

        mockMvc.perform(post("/api/transacoes/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não foi encontrado com o id: 1"));

       verify(transacaoService).gerarTransacao(dto);
   }

    @Test
    @DisplayName("Deve lançar exception quando usuário não autorizado tentar realizar transação")
    void deveLancarExceptionQuandoUsuarioNaoAutorizadoTransacao() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"));

        when(transacaoService.gerarTransacao(dto)).thenThrow(new UnauthorizedTransactionException("Transação não autorizada."));

        mockMvc.perform(post("/api/transacoes/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Transação não autorizada."));

        verify(transacaoService).gerarTransacao(dto);
    }

    @Test
    @DisplayName("Deve lançar exception quando autorizador não estiver disponível")
    void deveLancarExceptionQuandoAutorizadorIndisponivel() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"));

        when(transacaoService.gerarTransacao(dto)).thenThrow(new UnavailableAuthorizerException("Não foi possível consultar o serviço de autorização", null));

        mockMvc.perform(post("/api/transacoes/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Não foi possível consultar o serviço de autorização"));

        verify(transacaoService).gerarTransacao(dto);
    }

    @Test
    @DisplayName("Deve lançar exception quando remetente inelegivel")
    void deveLancarExceptionQuandoRemetenteInelegivel() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"));

        when(transacaoService.gerarTransacao(dto)).thenThrow(new IneligibleSenderException("Lojistas não podem realizar transações, apenas receber."));

        mockMvc.perform(post("/api/transacoes/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lojistas não podem realizar transações, apenas receber."));

        verify(transacaoService).gerarTransacao(dto);
    }

    @Test
    @DisplayName("Deve lançar exception quando remetente for igual ao destinatário")
    void deveLancarExceptionQuandoRemetenteForIgualDestinatario() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 1L, new BigDecimal("10.00"));

        when(transacaoService.gerarTransacao(dto)).thenThrow(new SelfTransferNotAllowedException("Um usuário não pode realizar transferências para si mesmo."));

        mockMvc.perform(post("/api/transacoes/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Um usuário não pode realizar transferências para si mesmo."));

        verify(transacaoService).gerarTransacao(dto);

    }

}
