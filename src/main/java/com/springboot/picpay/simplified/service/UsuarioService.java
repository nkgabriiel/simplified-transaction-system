package com.springboot.picpay.simplified.service;

import com.springboot.picpay.simplified.dto.request.UsuarioRequestDTO;
import com.springboot.picpay.simplified.dto.response.UsuarioResponseDTO;
import com.springboot.picpay.simplified.exception.IneligibleSenderException;
import com.springboot.picpay.simplified.exception.InvalidDocumentException;
import com.springboot.picpay.simplified.exception.InvalidUserRequestDataException;
import com.springboot.picpay.simplified.exception.UsuarioNotFoundException;
import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.model.Usuario;
import com.springboot.picpay.simplified.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public void validarElegibilidadeDoRemetente(Usuario remetente, BigDecimal valorTransferencia) {
        if(remetente.getTipo() == TipoUsuario.LOJISTA) {
            throw new IneligibleSenderException("Lojistas não podem realizar transações, apenas receber.");
        }

        if(remetente.getSaldo().compareTo(valorTransferencia) < 0) {
            throw new IneligibleSenderException("Saldo do usuário insuficiente para realizar transação.");
        }
    }

    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO dto) {
        validarDocumento(dto.tipo(), dto.documento());

        if(usuarioRepository.existsByEmail(dto.email())) {
            throw new InvalidUserRequestDataException("Já existe um usuário com esse email");
        }

        if(usuarioRepository.existsByDocumento(dto.documento())) {
            throw new InvalidUserRequestDataException("Esse documento já está cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setDocumento(dto.documento());
        usuario.setTipo(dto.tipo());
        usuario.setSaldo(BigDecimal.ZERO);

        Usuario salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    @Transactional
    public void desativarUsuario(Long id) {
        Usuario usuario = findUsuarioById(id);
        usuario.setAtivo(false);
    }

    public UsuarioResponseDTO findUsuarioResponseById (Long id) {
        Usuario usuarioEncontrado = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não foi encontrado com o id: " + id));
        return toResponse(usuarioEncontrado);
    }

    public Usuario findUsuarioById (Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não foi encontrado com o id: " + id));
    }

    public boolean verificaExistenciaUsuario(Long id) {
        return usuarioRepository.existsById(id);
    }

    private void validarDocumento(TipoUsuario tipo, String documento) {
        int tamanhoEsperado = switch (tipo) {
            case COMUM -> 11;
            case LOJISTA -> 14;
        };

        if(documento.length() != tamanhoEsperado) {
            String tipoDocumento = tipo == TipoUsuario.COMUM ? "CPF" : "CNPJ";
            throw new InvalidDocumentException(tipoDocumento + " deve conter " + tamanhoEsperado + " dígitos.");
        }
    }

    private UsuarioResponseDTO toResponse (Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getDocumento(),
                usuario.getEmail(),
                usuario.getSaldo(),
                usuario.getTipo()
        );
    }
}
