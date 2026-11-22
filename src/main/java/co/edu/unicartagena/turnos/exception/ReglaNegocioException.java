package co.edu.unicartagena.turnos.exception;

import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import java.util.List;

/** La operación viola una o más reglas de negocio del modelo (HTTP 422). */
public class ReglaNegocioException extends RuntimeException {

    private final transient List<ViolacionResponse> violaciones;

    public ReglaNegocioException(String mensaje, List<ViolacionResponse> violaciones) {
        super(mensaje);
        this.violaciones = List.copyOf(violaciones);
    }

    public List<ViolacionResponse> getViolaciones() {
        return violaciones;
    }
}
