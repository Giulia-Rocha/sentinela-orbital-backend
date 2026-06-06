package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.response.PrevisaoResponse;
import br.com.sentinela.service.CptecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/previsao")
@RequiredArgsConstructor
public class PrevisaoController {

    private final CptecService cptecService;

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<PrevisaoResponse> buscarPrevisao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(cptecService.buscarPrevisaoPorRegiao(regiaoId));
    }
}
