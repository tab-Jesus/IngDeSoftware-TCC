package co.edu.unicartagena.turnos.exception;

import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del dominio de la aplicación a respuestas JSON uniformes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de la base de datos.",
                List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaDeNegocio(ReglaNegocioException ex) {
        List<String> detalles = ex.getViolaciones().stream()
                .map(v -> v.tipo() + ": " + v.mensaje()).toList();
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), detalles);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return construir(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos.", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido.", List.of());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, List<String> detalles) {
        ErrorResponse cuerpo = new ErrorResponse(Instant.now(), estado.value(), estado.getReasonPhrase(),
                mensaje, detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
