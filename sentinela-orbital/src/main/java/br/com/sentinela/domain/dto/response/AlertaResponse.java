package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AlertaResponse {
    private Long id;
    private String nivel;
    private String mensagem;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private String nomeRegiao;
}