package br.com.sentinela.repository;

import br.com.sentinela.domain.model.LeituraClimatica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeituraClimaticaRepository extends JpaRepository<LeituraClimatica, Long> {
    List<LeituraClimatica> findByRegiaoIdOrderByTimestampDesc(Long regiaoId);
    Optional<LeituraClimatica> findTopByRegiaoIdOrderByTimestampDesc(Long regiaoId);
}