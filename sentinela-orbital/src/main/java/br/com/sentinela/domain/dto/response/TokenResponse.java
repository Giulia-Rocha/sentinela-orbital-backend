package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String tipo = "Bearer";
    private String email;
    private String name;
}