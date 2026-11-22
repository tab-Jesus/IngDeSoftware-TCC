package co.edu.unicartagena.turnos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI turnosOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de turnos y recursos de una sala comunitaria")
                .version("1.0.0")
                .description("Asigna sesiones a períodos y equipos mediante coloración de grafos y "
                        + "verifica las reglas de consistencia del modelo."));
    }
}
