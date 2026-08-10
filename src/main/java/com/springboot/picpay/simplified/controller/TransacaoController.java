package com.springboot.picpay.simplified.controller;

import com.springboot.picpay.simplified.dto.request.TransacaoRequestDTO;
import com.springboot.picpay.simplified.dto.response.TransacaoResponseDTO;
import com.springboot.picpay.simplified.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Gerenciamento das transações")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @Operation(summary = "Gerar uma nova transferência", description = "Cria uma nova transferência envolvendo dois usuários")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem autorização"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Chave de idempotência reutilizada com dados diferentes " +
                    "ou concorrência na atualização do saldo"),
            @ApiResponse(responseCode = "503", description = "Autorizador indisponível")
    })
    @PostMapping("/transferir")
    public ResponseEntity<TransacaoResponseDTO> criar(
            @RequestBody @Valid TransacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.gerarTransacao(dto));
    }

    @Operation(summary = "Lista todas as transferências de um usuário", description = "Lista todas as transações em que um usuário participa" +
            "como remetente ou destinatário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
    })
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listarTodasTransacoesPorUsuario(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarTodasPorUsuario(usuarioId));
    }
}
