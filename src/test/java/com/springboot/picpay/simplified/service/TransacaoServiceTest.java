package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.client.autorizacao.AutorizacaoClient;
import com.springboot.picpay.simplified.client.notificacao.TransacaoRealizadaEvent;
import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.exception.*;
import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.model.Transacao;
import com.springboot.picpay.simplified.model.Usuario;
import com.springboot.picpay.simplified.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    AutorizacaoClient autorizacaoClient;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario remetente;
    private Usuario destinatario;

    @BeforeEach
    void setUp() {
        remetente = new Usuario();
        remetente.setId(1L);
        remetente.setSaldo(new BigDecimal("100.00"));
        remetente.setTipo(TipoUsuario.COMUM);

        destinatario = new Usuario();
        destinatario.setId(2L);
        destinatario.setSaldo(new BigDecimal("0.00"));
        destinatario.setTipo(TipoUsuario.COMUM);
    }

    @Test
    @DisplayName("Deve lançar exception quando remetente não existir")
    void deveLancarExceptionQuandoRemetenteNaoExistir() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("100.00"), UUID.randomUUID());
        when(usuarioService.findUsuarioById(dto.remetenteId())).thenThrow(new UsuarioNotFoundException("Usuário não foi encontrado com o id: 1"));

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(UsuarioNotFoundException.class);

        verify(usuarioService, never()).validarElegibilidadeDoRemetente(any(), any());
        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exception quando valor inválido para transação")
    void deveLancarExceptionQuandoValorInvalido() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(remetente.getId(), destinatario.getId(), new BigDecimal("0.00"), UUID.randomUUID());

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(InvalidTransactionValueException.class)
                .hasMessage("Insira um valor válido para a transferência: 0.00");

        verify(usuarioService, never()).validarElegibilidadeDoRemetente(any(), any());
        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exception quando usuário tentar transferir para si mesmo")
    void deveLancarExceptionQuandoAutoTransferencia() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 1L, new BigDecimal("10.00"), UUID.randomUUID());

        when(usuarioService.findUsuarioById(1L)).thenReturn(remetente);

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(SelfTransferNotAllowedException.class)
                .hasMessage("Um usuário não pode realizar transferências para si mesmo.");

        verify(usuarioService, never()).validarElegibilidadeDoRemetente(any(), any());
        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exception quando usuário for inelegível")
    void deveLancarExceptionQuandoUsuarioInelegivel() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"), UUID.randomUUID());

        when(usuarioService.findUsuarioById(1L)).thenReturn(remetente);
        when(usuarioService.findUsuarioById(2L)).thenReturn(destinatario);

        doThrow(new IneligibleSenderException("Lojistas não podem realizar transações, apenas receber."))
                .when(usuarioService).validarElegibilidadeDoRemetente(any(Usuario.class), (any(BigDecimal.class)));

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(IneligibleSenderException.class);

        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exception quando transação não autorizada")
    void deveLancarExceptionQuandoTransacaoNaoAutorizada() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"), UUID.randomUUID());

        when(usuarioService.findUsuarioById(1L)).thenReturn(remetente);
        when(usuarioService.findUsuarioById(2L)).thenReturn(destinatario);

        when(autorizacaoClient.autorizar()).thenReturn(false);

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(UnauthorizedTransactionException.class);

        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exception quando autorizador não disponível")
    void deveLancarExceptionQuandoAutorizadorNaoDisponivel() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(1L, 2L, new BigDecimal("10.00"), UUID.randomUUID());

        when(usuarioService.findUsuarioById(1L)).thenReturn(remetente);
        when(usuarioService.findUsuarioById(2L)).thenReturn(destinatario);

        when(autorizacaoClient.autorizar()).thenThrow(new UnavailableAuthorizerException("Não foi possível consultar o serviço de autorização", null));

        assertThatThrownBy(() -> transacaoService.gerarTransacao(dto))
                .isInstanceOf(UnavailableAuthorizerException.class);

        verify(transacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar sucesso quando transação corretamente realizada")
    void deveLancarTransacaoCorretaComSucesso() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(remetente.getId(), destinatario.getId(), new BigDecimal("10.00"), UUID.randomUUID());

        when(usuarioService.findUsuarioById(1L)).thenReturn(remetente);
        when(usuarioService.findUsuarioById(2L)).thenReturn(destinatario);
        when(autorizacaoClient.autorizar()).thenReturn(true);
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> {
            Transacao transacaoRecebida = invocation.getArgument(0);
            transacaoRecebida.setId(1L);
            return transacaoRecebida;
        });

        TransacaoResponseDTO response = transacaoService.gerarTransacao(dto);

        assertThat(response.remetenteId()).isEqualTo(1L);
        assertThat(response.destinatarioId()).isEqualTo(2L);
        assertThat(remetente.getSaldo()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(destinatario.getSaldo()).isEqualByComparingTo(new BigDecimal("10.00"));

        verify(eventPublisher, times(1)).publishEvent(any(TransacaoRealizadaEvent.class));
    }

    @Test
    @DisplayName("Deve listar todas as transações de um usuário")
    void deveListarTransacoesUsuario() {
        Transacao transacao = new Transacao();
        transacao.setId(1L);
        transacao.setRemetente(remetente);
        transacao.setDestinatario(destinatario);
        transacao.setValor(new BigDecimal("10.00"));
        transacao.setHora(LocalDateTime.now());

        when(usuarioService.verificaExistenciaUsuario(1L)).thenReturn(true);
        when(transacaoRepository.findByRemetenteIdOrDestinatarioId(1L, 1L))
                .thenReturn(List.of(transacao));

        List<TransacaoResponseDTO> resultado = transacaoService.listarTodasPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().remetenteId()).isEqualTo(1L);
        assertThat(resultado.getFirst().destinatarioId()).isEqualTo(2L);
        assertThat(resultado.getFirst().valor()).isEqualByComparingTo(new BigDecimal("10.00"));
    }
}

