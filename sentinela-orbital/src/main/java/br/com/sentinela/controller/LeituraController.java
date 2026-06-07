package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.request.LeituraRequest;
import br.com.sentinela.domain.dto.response.LeituraResponse;
import br.com.sentinela.service.LeituraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leituras")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LeituraController {

    private final LeituraService leituraService;

    @PostMapping
    public ResponseEntity<LeituraResponse> processar(@Valid @RequestBody LeituraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leituraService.processarLeitura(request));
    }

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<List<LeituraResponse>> listarPorRegiao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(leituraService.listarPorRegiao(regiaoId));
    }
}
