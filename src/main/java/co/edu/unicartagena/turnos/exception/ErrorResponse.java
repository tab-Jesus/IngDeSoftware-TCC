package co.edu.unicartagena.turnos.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, int status, String error, String mensaje, List<String> detalles) {
}
