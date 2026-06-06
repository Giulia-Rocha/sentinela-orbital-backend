package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrevisaoResponse {
    private String nomeRegiao;
    private String fonte;
    private String riscoCalor;
    private List<PrevisaoDiaResponse> previsoes;

}
