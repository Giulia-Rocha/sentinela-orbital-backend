package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.response.AlertaResponse;
import br.com.sentinela.service.AlertaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listarAtivos() {
        return ResponseEntity.ok(alertaService.listarAtivos());
    }

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<List<AlertaResponse>> listarPorRegiao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(alertaService.listarPorRegiao(regiaoId));
    }
}