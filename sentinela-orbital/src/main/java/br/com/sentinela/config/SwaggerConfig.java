package br.com.sentinela.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Sentinela Orbital API",
        description = "API REST para monitoramento de condições climáticas espaciais e alertas de ondas de calor.",
        version = "1.0.0",
        contact = @Contact(name = "Equipe Space Connect", email = "suporte@sentinela.com.br")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Insira o token JWT gerado no login"
)
public class SwaggerConfig {
}
