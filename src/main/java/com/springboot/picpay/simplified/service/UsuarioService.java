package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.dto.response.UsuarioResponseDTO;
import com.springboot.picpay.simplified.exception.IneligibleSenderException;
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

    public void validarElegibilidadeDoRemetente(Usuario remetente, BigDecimal valorTransferencia) {
        if(remetente.getTipo() == TipoUsuario.LOJISTA) {
            throw new IneligibleSenderException("Lojistas não podem realizar transações, apenas receber.");
        }

        if(remetente.getSaldo().compareTo(valorTransferencia) < 0) {
            throw new IneligibleSenderException("Saldo do usuário insuficiente para realizar transação.");
        }
    }

    public UsuarioResponseDTO findUsuarioResponseById (Long id) {
        Usuario usuarioEncontrado = repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        return toResponse(usuarioEncontrado);

    }

    public Usuario findUsuarioById (Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
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
