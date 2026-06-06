package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegiaoResponse {
    private Long id;
    private String nome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}