package com.oficina.oficina_api.repository;

import com.oficina.oficina_api.model.Funcao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncaoRepository extends JpaRepository<Funcao, Long> {
    Optional<Funcao> findByNome(String nome);
}
