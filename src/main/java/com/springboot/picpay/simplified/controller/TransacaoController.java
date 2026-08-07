package com.springboot.picpay.simplified.controller;

import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping("/transfer")
    public ResponseEntity<TransacaoResponseDTO> criar(
            @RequestBody @Valid TransacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.gerarTransacao(dto));
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listarTodasTransacoesPorUsuario(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarTodasPorUsuario(usuarioId));
    }
}
