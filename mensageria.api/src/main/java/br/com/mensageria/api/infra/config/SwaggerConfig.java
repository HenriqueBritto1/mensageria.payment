package br.com.mensageria.api.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Mensageria payment - API Gateway")
                .version("1.0")
                .description("Documentação dos endpoints da aplicação")
                .contact(new Contact().name("Suporte").email("henrique.britto1711@outlook.com")));
    }
}
