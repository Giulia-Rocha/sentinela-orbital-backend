package br.com.sentinela.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeituraRequest {

    @NotNull
    private Long regiaoId;

    @NotNull
    private Double temperatura;

    @NotNull
    private Double umidade;

    @NotNull
    private Double iuv;
}
