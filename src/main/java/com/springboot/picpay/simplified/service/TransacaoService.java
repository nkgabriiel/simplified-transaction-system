package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.client.AutorizacaoClient;
import com.springboot.picpay.simplified.client.NotificacaoClient;
import com.springboot.picpay.simplified.dto.request.NotificacaoRequestDTO;
import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.exception.SelfTransferNotAllowedException;
import com.springboot.picpay.simplified.exception.InvalidTransactionValueException;
import com.springboot.picpay.simplified.exception.UnauthorizedTransactionException;
import com.springboot.picpay.simplified.model.Transacao;
import com.springboot.picpay.simplified.model.Usuario;
import com.springboot.picpay.simplified.repository.TransacaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final AutorizacaoClient autorizacaoClient;
    private final NotificacaoClient notificacaoClient;

    @Transactional
    public TransacaoResponseDTO gerarTransacao(TransacaoRequestDTO dto) {
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

        Transacao transacaoSalva = salvarTransacao(remetente, destinatario, dto.valor());

        notificacaoClient.notificar(toNotificacaoRequest(destinatario, dto.valor(), remetente));

        return toResponse(transacaoSalva);

    }

    public List<TransacaoResponseDTO> listarTodasPorUsuario (Long id) {
        return transacaoRepository.findByRemetenteIdOrDestinatarioId(id, id)
                .stream()
                .map(this::toResponse)
                .toList();
    }





    private Transacao salvarTransacao(Usuario remetente, Usuario destinatario, BigDecimal valor) {
            Transacao transacao = new Transacao();
            transacao.setRemetente(remetente);
            transacao.setDestinatario(destinatario);
            transacao.setValor(valor);
            transacao.setHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
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

    private NotificacaoRequestDTO toNotificacaoRequest(Usuario destinatario, BigDecimal valor, Usuario remetente) {
        return new NotificacaoRequestDTO(
                destinatario.getNome(),
                valor,
                remetente.getNome()
        );
    }

}
