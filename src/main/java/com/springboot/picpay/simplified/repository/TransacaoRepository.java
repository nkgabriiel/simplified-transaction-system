package com.springboot.picpay.simplified.repository;

import com.springboot.picpay.simplified.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    Optional<Transacao> findById (Long id);
}
