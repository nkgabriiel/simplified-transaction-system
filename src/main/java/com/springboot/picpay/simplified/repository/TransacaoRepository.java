package com.springboot.picpay.simplified.repository;

import com.springboot.picpay.simplified.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByRemetenteIdOrDestinatarioId(Long remetenteId, Long DestinatarioId);
}
