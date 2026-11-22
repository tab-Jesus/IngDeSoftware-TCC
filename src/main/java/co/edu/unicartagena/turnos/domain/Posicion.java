package co.edu.unicartagena.turnos.domain;

/**
 * Posición del calendario: par (período, equipo). Corresponde a f(s) = (τ(s), κ(s)).
 */
public record Posicion(long periodoId, long equipoId) {
}
