package co.edu.unicartagena.turnos.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe.");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
