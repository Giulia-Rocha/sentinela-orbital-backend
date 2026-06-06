package br.com.sentinela.service;

import br.com.sentinela.domain.dto.response.AlertaResponse;
import br.com.sentinela.domain.model.Alerta;
import br.com.sentinela.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public List<AlertaResponse> listarAtivos() {
        return alertaRepository.findAllByAtivoTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<AlertaResponse> listarPorRegiao(Long regiaoId) {
        return alertaRepository.findByRegiaoIdAndAtivoTrue(regiaoId)
                .stream().map(this::toResponse).toList();
    }

    private AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(
                a.getId(), a.getNivel(), a.getMensagem(),
                a.getAtivo(), a.getCreatedAt(), a.getRegiao().getNome());
    }
}