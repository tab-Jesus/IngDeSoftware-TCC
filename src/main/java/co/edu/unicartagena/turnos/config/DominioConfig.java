package co.edu.unicartagena.turnos.config;

import co.edu.unicartagena.turnos.domain.PlanificadorColoracion;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expone como beans las clases del dominio, que no dependen de Spring. */
@Configuration
public class DominioConfig {

    @Bean
    public PlanificadorColoracion planificadorColoracion() {
        return new PlanificadorColoracion();
    }

    @Bean
    public ValidadorAgenda validadorAgenda() {
        return new ValidadorAgenda();
    }
}
