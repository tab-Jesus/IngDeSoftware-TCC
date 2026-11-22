package co.edu.unicartagena.turnos.domain;

import java.util.List;
import java.util.Map;

/**
 * Resultado del solucionador.
 *
 * @param factible        true si existe una agenda que cumple todas las restricciones
 * @param asignaciones    sesión → posición (vacío si no es factible)
 * @param periodosUsados  cantidad de períodos distintos utilizados (objetivo minimizado)
 * @param nodosExplorados asignaciones tentativas realizadas durante la búsqueda
 * @param optimo          true si se probó que no existe una agenda con menos períodos
 * @param mensajes        explicación en lenguaje natural (p. ej. motivo de infactibilidad)
 */
public record ResultadoPlanificacion(
        boolean factible,
        Map<Long, Posicion> asignaciones,
        int periodosUsados,
        long nodosExplorados,
        boolean optimo,
        List<String> mensajes) {
}
