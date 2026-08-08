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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario primeiroUsuarioComum;

    private Usuario usuarioLojista;

    @BeforeEach
    void setUp() {
        primeiroUsuarioComum = new Usuario();
        primeiroUsuarioComum.setId(1L);
        primeiroUsuarioComum.setNome("Gabriel");
        primeiroUsuarioComum.setDocumento("12345678910");
        primeiroUsuarioComum.setEmail("Gabriel@email.com");
        primeiroUsuarioComum.setSaldo(new BigDecimal("100.00"));
        primeiroUsuarioComum.setTipo(TipoUsuario.COMUM);
        primeiroUsuarioComum.setAtivo(true);

        usuarioLojista = new Usuario();
        usuarioLojista.setId(3L);
        usuarioLojista.setNome("Lorenzo");
        usuarioLojista.setDocumento("12345678000198");
        usuarioLojista.setEmail("Lorenzo@email.com");
        usuarioLojista.setSaldo(new BigDecimal("10.00"));
        usuarioLojista.setTipo(TipoUsuario.LOJISTA);
        usuarioLojista.setAtivo(true);
    }

    @Test
    @DisplayName("Deve salvar usuario e retornar response com o ID gerado")
    void deveSalvarUsuarioComSucesso() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioRecebido = invocation.getArgument(0);
            usuarioRecebido.setId(1L);
            return usuarioRecebido;
        });

        UsuarioResponseDTO response = usuarioService.criarUsuario(dto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Gabriel");
        assertThat(response.documento()).isEqualTo("12345678910");
        assertThat(response.tipoUsuario()).isEqualTo(TipoUsuario.COMUM);
        assertThat(response.email()).isEqualTo("Gabriel@email.com");
        assertThat(response.saldo()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exception ao tentar realizar transação sendo lojista")
    void deveLancarQuandoExceptionRemetenteLojista() {
        BigDecimal valorTransferencia = new BigDecimal("100.00");

        assertThatThrownBy(() -> usuarioService.validarElegibilidadeDoRemetente(usuarioLojista, valorTransferencia))
                .isInstanceOf(IneligibleSenderException.class)
                .hasMessage("Lojistas não podem realizar transações, apenas receber.");
    }

    @Test
    @DisplayName("Deve lançar exception ao tentar realizar transação com saldo insuficiente")
    void deveLancarExceptionQuandoSaldoInsuficiente() {
        BigDecimal valorTransferencia = new BigDecimal("120.00");

        assertThatThrownBy(() -> usuarioService.validarElegibilidadeDoRemetente(primeiroUsuarioComum, valorTransferencia))
                .isInstanceOf(IneligibleSenderException.class)
                .hasMessage("Saldo do usuário insuficiente para realizar transação.");
    }

    @Test
    @DisplayName("Deve lançar sucesso quando remetente for elegível para transação (boundary case)")
    void deveLancarSucessoParaRemetenteElegivelBoundaryCase() {
        BigDecimal valorTransferencia = new BigDecimal("100.00");

        assertThatCode(() -> usuarioService.validarElegibilidadeDoRemetente(primeiroUsuarioComum, valorTransferencia))
                .doesNotThrowAnyException();

    }

    @Test
    @DisplayName("Deve lançar sucesso quando remetente for elegível para transação (normal case)")
    void deveLancarSucessoParaRemetenteElegivel() {
        BigDecimal valorTransferencia = new BigDecimal("50.00");

        assertThatCode(() -> usuarioService.validarElegibilidadeDoRemetente(primeiroUsuarioComum, valorTransferencia))
                .doesNotThrowAnyException();

    }

    @Test
    @DisplayName("(CPF) - Deve lançar exception quando tipo do documento não corresponder ao tipo do usuário")
    void deveLancarExceptionQuandoTamanhoDoCpfErrado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678000198", TipoUsuario.COMUM);

        assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("CPF deve conter 11 dígitos.");
    }

    @Test
    @DisplayName("(CNPJ) - Deve lançar exception quando tipo do documento não corresponder ao tipo do usuário")
    void deveLancarExceptionQuandoTamanhoDoCnpjErrado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.LOJISTA);

        assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("CNPJ deve conter 14 dígitos.");
    }

    @Test
    @DisplayName("(CNPJ) - Deve lançar sucesso quando tipo do documento correto")
    void deveLancarSucessoQuandoCnpjCorreto() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678000198", TipoUsuario.LOJISTA);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioRecebido = invocation.getArgument(0);
            usuarioRecebido.setId(1L);
            return usuarioRecebido;
        });

        UsuarioResponseDTO response = usuarioService.criarUsuario(dto);

        assertThat(response.documento()).isEqualTo("12345678000198");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("(CPF) - Deve lançar sucesso quando tipo do documento correto")
    void deveLancarSucessoQuandoCpfCorreto() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioRecebido = invocation.getArgument(0);
            usuarioRecebido.setId(1L);
            return usuarioRecebido;
        });

        UsuarioResponseDTO response = usuarioService.criarUsuario(dto);

        assertThat(response.documento()).isEqualTo("12345678910");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exception se o email já estiver cadastrado")
    void deveLancarExceptionQuandoEmailCadastrado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
                .isInstanceOf(InvalidUserRequestDataException.class)
                .hasMessage("Já existe um usuário com esse email");
    }

    @Test
    @DisplayName("Deve lançar exception quando documento já estiver cadastrado")
    void deveLancarExceptionQuandoDocumentoCadastrado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioRepository.existsByDocumento(dto.documento())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
                .isInstanceOf(InvalidUserRequestDataException.class)
                .hasMessage("Esse documento já está cadastrado");
    }

    @Test
    @DisplayName("Deve lançar exception ao tentar deletar usuário que não existe")
    void deveLancarExceptionAoDeletarUsuarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.desativarUsuario(999L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessage("Usuário não foi encontrado com o id: 999");
    }

    @Test
    @DisplayName("Deve lançar sucesso quando deletando usuário existente")
    void deveLancarSucessoQuandoDeletarUsuarioExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(primeiroUsuarioComum));

        assertThatCode(() -> usuarioService.desativarUsuario(1L))
                .doesNotThrowAnyException();

        assertThat(primeiroUsuarioComum.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("(Response) - Deve lançar exception quando buscar por usuário que não existe")
    void deveLancarExceptionQuandoBuscarUsuarioResponseInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findUsuarioResponseById(999L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessage("Usuário não foi encontrado com o id: 999");
    }

    @Test
    @DisplayName("(Objeto) - Deve lançar exception quando buscar entidade usuário que não existe")
    void deveLancarExceptionQuandoBuscarUsuarioObjetoInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> usuarioService.findUsuarioById(999L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessage("Usuário não foi encontrado com o id: 999");
    }

    @Test
    @DisplayName("(Response) - Deve retornar sucesso quando buscar por usuário que existe")
    void deveLancarSucessoQuandoBuscarUsuarioResponse() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(primeiroUsuarioComum));

        UsuarioResponseDTO response = usuarioService.findUsuarioResponseById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Gabriel");

    }

    @Test
    @DisplayName("(Objeto) - Deve retornar sucesso quando buscar por usuário")
    void deveLancarSucessoQuandoBuscarUsuarioObjeto() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(primeiroUsuarioComum));

        Usuario resultado = usuarioService.findUsuarioById(1L);

        assertThat(resultado).isSameAs(primeiroUsuarioComum);
    }
}
