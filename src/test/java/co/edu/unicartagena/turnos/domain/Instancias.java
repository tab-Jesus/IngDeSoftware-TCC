package co.edu.unicartagena.turnos.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instancias de prueba. La instancia base es la del TCC de Matemáticas Discretas:
 * 6 usuarios (1..6), 7 sesiones A..G (1..7), 3 períodos (1..3) y 3 equipos (1..3).
 */
final class Instancias {

    static final long A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7;

    private Instancias() {
    }

    static ProblemaPlanificacion base() {
        Map<Long, Set<Long>> participantes = new LinkedHashMap<>();
        participantes.put(A, Set.of(1L, 2L));
        participantes.put(B, Set.of(2L, 3L));
        participantes.put(C, Set.of(1L, 3L));
        participantes.put(D, Set.of(3L, 4L));
        participantes.put(E, Set.of(4L, 5L));
        participantes.put(F, Set.of(5L, 6L));
        participantes.put(G, Set.of(1L, 6L));
        return construir(participantes, List.of(1L, 2L, 3L), List.of(1L, 2L, 3L), Map.of());
    }

    /**
     * Construye un problema con disponibilidad completa salvo las restricciones dadas
     * (usuario → períodos disponibles) y con todos los equipos compatibles con todas las sesiones.
     */
    static ProblemaPlanificacion construir(Map<Long, Set<Long>> participantes, List<Long> periodos,
                                           List<Long> equipos, Map<Long, Set<Long>> disponibilidadEspecial) {
        List<Long> sesiones = new ArrayList<>(participantes.keySet());
        Map<Long, Set<Long>> disponibilidad = new HashMap<>();
        for (Set<Long> usuarios : participantes.values()) {
            for (Long usuario : usuarios) {
                disponibilidad.put(usuario, Set.copyOf(periodos));
            }
        }
        disponibilidad.putAll(disponibilidadEspecial);
        Map<Long, Set<Long>> compatibilidad = new HashMap<>();
        for (Long sesion : sesiones) {
            compatibilidad.put(sesion, Set.copyOf(equipos));
        }
        return new ProblemaPlanificacion(sesiones, participantes, periodos, equipos, disponibilidad, compatibilidad);
    }

    static List<ValidadorAgenda.ReservaPropuesta> agendaDelTcc() {
        return List.of(
                new ValidadorAgenda.ReservaPropuesta(A, 1, 1),
                new ValidadorAgenda.ReservaPropuesta(B, 2, 1),
                new ValidadorAgenda.ReservaPropuesta(C, 3, 1),
                new ValidadorAgenda.ReservaPropuesta(D, 1, 2),
                new ValidadorAgenda.ReservaPropuesta(E, 2, 2),
                new ValidadorAgenda.ReservaPropuesta(F, 1, 3),
                new ValidadorAgenda.ReservaPropuesta(G, 2, 3));
    }

    static List<ValidadorAgenda.ReservaPropuesta> sin(List<ValidadorAgenda.ReservaPropuesta> agenda, long sesion) {
        List<ValidadorAgenda.ReservaPropuesta> copia = new ArrayList<>(agenda);
        copia.removeIf(r -> r.sesionId() == sesion);
        return copia;
    }
}
