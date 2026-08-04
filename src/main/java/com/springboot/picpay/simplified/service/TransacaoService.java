package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.model.Transacao;
import com.springboot.picpay.simplified.repository.TransacaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;

    public TransacaoResponseDTO gerarTransacao(TransacaoRequestDTO dto) {

    }

    public boolean transacaoAutorizada() {

    }

}
