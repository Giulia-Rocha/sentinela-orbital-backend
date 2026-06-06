package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeituraResponse {
    private Long id;
    private Long regiaoId;
    private String nomeRegiao;
    private Double temperatura;
    private Double umidade;
    private Double iuv;
    private Double hri;
    private String fonte;
    private LocalDateTime timestamp;
    private String nivelAlerta;
    private String mensagemAlerta;
}
