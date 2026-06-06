package br.com.sentinelasoap.repository;

import br.com.sentinelasoap.domain.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByRegiaoIdAndAtivoTrue(Long regiaoId);
}
