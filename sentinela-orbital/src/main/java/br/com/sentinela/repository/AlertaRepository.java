package br.com.sentinela.repository;

import br.com.sentinela.domain.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByRegiaoIdAndAtivoTrue(Long regiaoId);
    List<Alerta> findAllByAtivoTrueOrderByCreatedAtDesc();
}