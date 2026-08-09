package com.springboot.picpay.simplified.repository;

import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuarioBuscado;

    @BeforeEach
    void setUp() {
        usuarioBuscado = entityManager.persist(criarUsuario("Gabriel", "12345678910"));
    }

    @Test
    @DisplayName("Deve retornar true quando buscando usuário correto por documento")
    void deveRetornarTrueQuandoBuscandoUsuarioCorretoPorDocumento() {

        boolean resultado = usuarioRepository.existsByDocumento(usuarioBuscado.getDocumento());

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando buscando usuário correto por email")
    void deveRetornarTrueQuandoBuscandoUsuarioCorretoPorEMAIL() {

        boolean resultado = usuarioRepository.existsByEmail(usuarioBuscado.getEmail());

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando buscando usuário correto por documento")
    void deveRetornarFalseQuandoBuscandoUsuarioCorretoPorDocumento() {

        boolean resultado = usuarioRepository.existsByDocumento("00000000000");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando buscando usuário correto por email")
    void deveRetornarFalseQuandoBuscandoUsuarioCorretoPorEMAIL() {

        boolean resultado = usuarioRepository.existsByEmail("teste123@email.com");

        assertThat(resultado).isFalse();
    }

    private Usuario criarUsuario(String nome, String documento) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(nome.toLowerCase() + "@email.com");
        usuario.setDocumento(documento);
        usuario.setSaldo(BigDecimal.ZERO);
        usuario.setTipo(TipoUsuario.COMUM);
        return usuario;
    }
}
