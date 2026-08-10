package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.client.autorizacao.AutorizacaoClient;
import com.springboot.picpay.simplified.client.notificacao.TransacaoRealizadaEvent;
import com.springboot.picpay.simplified.dto.request.NotificacaoRequestDTO;
import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.exception.*;
import com.springboot.picpay.simplified.model.Transacao;
import com.springboot.picpay.simplified.model.Usuario;
import com.springboot.picpay.simplified.repository.TransacaoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final AutorizacaoClient autorizacaoClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransacaoResponseDTO gerarTransacao(TransacaoRequestDTO dto) {
        log.info("Gerando transação: remetente={}, destinatario={}, valor={}", dto.remetenteId(), dto.destinatarioId(), dto.valor());
        Optional<Transacao> transacaoExistente = verificarIdempotencyKey(dto.idempotencyKey());

        if(transacaoExistente.isPresent()) {
            log.warn("Chave de idempotência reutilizada: {}. devolvendo resultado..", dto.idempotencyKey());
            Transacao transacao = transacaoExistente.get();

            validarMesmaRequisicao(transacao, dto);
            return toResponse(transacaoExistente.get());
        }
        Usuario remetente = usuarioService.findUsuarioById(dto.remetenteId());
        Usuario destinatario = usuarioService.findUsuarioById(dto.destinatarioId());

        if(dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionValueException("Insira um valor válido para a transferência: " + dto.valor());
        }

        if(remetente.getId().equals(destinatario.getId())) {
            throw new SelfTransferNotAllowedException("Um usuário não pode realizar transferências para si mesmo.");
        }


        usuarioService.validarElegibilidadeDoRemetente(remetente, dto.valor());

        if(!autorizacaoClient.autorizar()) {
            throw new UnauthorizedTransactionException("Transação não autorizada.");
        }

        remetente.setSaldo(remetente.getSaldo().subtract(dto.valor()));
        destinatario.setSaldo(destinatario.getSaldo().add(dto.valor()));

        Transacao transacaoSalva = salvarTransacao(remetente, destinatario, dto.valor(), dto.idempotencyKey());
        log.info("Transação concluída: transacaoId={}", transacaoSalva.getId());

        eventPublisher.publishEvent(new TransacaoRealizadaEvent(toNotificacaoRequest(remetente, destinatario, dto.valor())));

        return toResponse(transacaoSalva);

    }

    public List<TransacaoResponseDTO> listarTodasPorUsuario (Long id) {

        if(!usuarioService.verificaExistenciaUsuario(id)) {
            throw new UsuarioNotFoundException("Usuário não foi encontrado com o id: " + id);
        }

        return transacaoRepository.findByRemetenteIdOrDestinatarioId(id, id)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Transacao salvarTransacao(Usuario remetente, Usuario destinatario, BigDecimal valor, UUID idempotencyKey) {
            Transacao transacao = new Transacao();
            transacao.setRemetente(remetente);
            transacao.setDestinatario(destinatario);
            transacao.setValor(valor);
            transacao.setIdempotencyKey(idempotencyKey);
            transacao.setHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
    }

    private Optional<Transacao> verificarIdempotencyKey(UUID idempotencyKey) {
        return transacaoRepository.findByIdempotencyKey(idempotencyKey);
    }

    private void validarMesmaRequisicao(Transacao transacao, TransacaoRequestDTO dto) {
        boolean mesmaRequisicao =
                transacao.getRemetente().getId().equals(dto.remetenteId())
                && transacao.getDestinatario().getId().equals(dto.destinatarioId())
                && transacao.getValor().compareTo(dto.valor()) == 0;

        if(!mesmaRequisicao) {
            throw new ReutilizedIdempotencyKeyException("A chave de idempotência já foi utilizada.");
        }
    }

    private TransacaoResponseDTO toResponse(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getRemetente().getId(),
                transacao.getDestinatario().getId(),
                transacao.getValor(),
                transacao.getHora()
        );
    }

    private NotificacaoRequestDTO toNotificacaoRequest(Usuario remetente, Usuario destinatario, BigDecimal valor) {
        return new NotificacaoRequestDTO(
                remetente.getNome(),
                destinatario.getNome(),
                valor
        );
    }

}
