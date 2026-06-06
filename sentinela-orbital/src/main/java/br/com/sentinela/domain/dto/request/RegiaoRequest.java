package br.com.sentinela.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegiaoRequest {
    @NotBlank private String nome;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private String descricao;
}