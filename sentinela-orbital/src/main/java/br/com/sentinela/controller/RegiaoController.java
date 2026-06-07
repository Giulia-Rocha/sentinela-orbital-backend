package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.request.RegiaoRequest;
import br.com.sentinela.domain.dto.response.RegiaoResponse;
import br.com.sentinela.service.RegiaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regioes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RegiaoController {

    private final RegiaoService regiaoService;

    @GetMapping
    public ResponseEntity<List<RegiaoResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(regiaoService.listar(auth.getName()));
    }

    @GetMapping("/proxima")
    public ResponseEntity<RegiaoResponse> buscarProxima(@RequestParam Double lat,
                                                        @RequestParam Double lng,
                                                        Authentication auth) {
        return ResponseEntity.ok(regiaoService.buscarMaisProxima(lat, lng, auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegiaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(regiaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RegiaoResponse> criar(@Valid @RequestBody RegiaoRequest request,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(regiaoService.criar(request, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegiaoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody RegiaoRequest request) {
        return ResponseEntity.ok(regiaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        regiaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}