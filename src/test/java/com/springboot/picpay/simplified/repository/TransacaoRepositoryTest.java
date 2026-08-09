package com.springboot.picpay.simplified.repository;

import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.model.Transacao;
import com.springboot.picpay.simplified.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransacaoRepositoryTest {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuarioPesquisado;
    private Usuario segundoUsuario;
    private Usuario terceiroUsuario;


    @BeforeEach
    void setUp() {
        usuarioPesquisado = entityManager.persist(criarUsuario("Gabriel", "12345678901"));
        segundoUsuario = entityManager.persist(criarUsuario("João", "98765432100"));
        terceiroUsuario = entityManager.persist(criarUsuario("Maria", "11122233344"));
    }

    @Test
    @DisplayName("Deve retornar as transações enviadas e recebidas pelo usuário")
    void deveRetornarTransacaoEnviadaERecebidaPeloUsuario() {
        Transacao transacaoEnviada = entityManager.persist(criarTransacao(usuarioPesquisado, segundoUsuario, "10.00", LocalDateTime.now()));
        Transacao transacaoRecebida = entityManager.persist(criarTransacao(terceiroUsuario, usuarioPesquisado, "20.00", LocalDateTime.now()));
        entityManager.persist(criarTransacao(segundoUsuario, terceiroUsuario, "50.00", LocalDateTime.now()));

        List<Transacao> resultado = transacaoRepository.findByRemetenteIdOrDestinatarioId(usuarioPesquisado.getId(), usuarioPesquisado.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Transacao::getId)
                .containsExactlyInAnyOrder(transacaoEnviada.getId(), transacaoRecebida.getId());
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

    private Transacao criarTransacao(
            Usuario remetente,
            Usuario destinatario,
            String valor,
            LocalDateTime dataHora
    ) {
        Transacao transacao = new Transacao();

        transacao.setRemetente(remetente);
        transacao.setDestinatario(destinatario);
        transacao.setValor(new BigDecimal(valor));
        transacao.setHora(dataHora);

        return transacao;
    }
}
