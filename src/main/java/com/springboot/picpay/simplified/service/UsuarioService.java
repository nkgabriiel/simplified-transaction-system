package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.dto.response.UsuarioResponseDTO;
import com.springboot.picpay.simplified.exception.UsuarioNotFoundException;
import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.model.Usuario;
import com.springboot.picpay.simplified.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;

    public void validarTransferencia(Usuario remetente, BigDecimal saldo) throws Exception {
        if(remetente.getTipo() == TipoUsuario.LOJISTA) {
            throw new Exception("Lojistas não podem realizar transações, apenas receber.");
        }

        if(remetente.getSaldo().compareTo(saldo) < 0) {
            throw new Exception("Usuário deve ter saldo válido para realizar transações.");
        }
    }

    public UsuarioResponseDTO findUsuarioById (Long id) {
        Usuario usuarioEncontrado = repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        return toResponse(usuarioEncontrado);

    }

    private UsuarioResponseDTO toResponse (Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getDocumento(),
                usuario.getEmail(),
                usuario.getSaldo(),
                String.valueOf(usuario.getTipo())
        );
    }
}
