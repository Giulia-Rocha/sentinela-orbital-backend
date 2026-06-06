package br.com.sentinela.repository;

import br.com.sentinela.domain.model.Regiao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegiaoRepository extends JpaRepository<Regiao, Long> {
    List<Regiao> findByUsuarioId(Long usuarioId);
}