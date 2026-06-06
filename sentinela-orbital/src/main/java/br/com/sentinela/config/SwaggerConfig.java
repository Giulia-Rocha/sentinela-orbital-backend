package br.com.sentinela.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI sentinelaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sentinela Orbital API")
                        .description("API REST para monitoramento de condições climáticas espaciais e alertas de ondas de calor.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Space Connect")
                                .email("suporte@sentinela.com.br")));
    }
}
