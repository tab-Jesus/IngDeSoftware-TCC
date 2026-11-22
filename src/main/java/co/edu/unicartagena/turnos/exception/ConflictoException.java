package co.edu.unicartagena.turnos.exception;

/** Se intenta crear un duplicado o eliminar un recurso que aún está referenciado (HTTP 409). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
