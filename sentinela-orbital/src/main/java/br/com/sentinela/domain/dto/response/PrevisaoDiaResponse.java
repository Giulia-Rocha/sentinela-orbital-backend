package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrevisaoDiaResponse {
    private String data;
    private Double tempMax;
    private Double tempMin;
    private Double iuv;
}
