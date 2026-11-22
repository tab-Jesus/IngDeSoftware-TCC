package co.edu.unicartagena.turnos.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instancia del problema de asignación, independiente de la persistencia.
 *
 * @param sesiones       conjunto S (ordenado, sin duplicados)
 * @param participantes  relación P: sesión → usuarios inscritos
 * @param periodos       conjunto T (ordenado cronológicamente)
 * @param equipos        conjunto C (equipos activos)
 * @param disponibilidad relación D: usuario → períodos en los que puede asistir
 * @param compatibilidad relación K: sesión → equipos compatibles
 */
public record ProblemaPlanificacion(
        List<Long> sesiones,
        Map<Long, Set<Long>> participantes,
        List<Long> periodos,
        List<Long> equipos,
        Map<Long, Set<Long>> disponibilidad,
        Map<Long, Set<Long>> compatibilidad) {

    public Set<Long> participantesDe(long sesionId) {
        return participantes.getOrDefault(sesionId, Set.of());
    }

    public boolean usuarioDisponible(long usuarioId, long periodoId) {
        return disponibilidad.getOrDefault(usuarioId, Set.of()).contains(periodoId);
    }

    public boolean equipoCompatible(long sesionId, long equipoId) {
        return compatibilidad.getOrDefault(sesionId, Set.of()).contains(equipoId);
    }

    /** Un período es admisible para una sesión si todos sus participantes están disponibles. */
    public boolean periodoAdmisible(long sesionId, long periodoId) {
        for (long usuario : participantesDe(sesionId)) {
            if (!usuarioDisponible(usuario, periodoId)) {
                return false;
            }
        }
        return true;
    }
}
