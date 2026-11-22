package co.edu.unicartagena.turnos.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verificador independiente del solucionador. Implementa las seis reglas de lógica de
 * predicados del modelo y reporta todas las violaciones encontradas, no solo la primera.
 */
public final class ValidadorAgenda {

    public enum Tipo {
        ENTIDAD_INEXISTENTE,
        SESION_SIN_RESERVA,
        RESERVA_DUPLICADA_SESION,
        EQUIPO_DOBLE_RESERVA,
        USUARIO_DOBLE_ACTIVIDAD,
        USUARIO_NO_DISPONIBLE,
        EQUIPO_INCOMPATIBLE
    }

    public record Violacion(Tipo tipo, String mensaje) {
    }

    /** Reserva propuesta: R(s, t, c). */
    public record ReservaPropuesta(long sesionId, long periodoId, long equipoId) {
    }

    public List<Violacion> validar(ProblemaPlanificacion problema, List<ReservaPropuesta> reservas,
                                   boolean exigirCobertura) {
        List<Violacion> violaciones = new ArrayList<>();
        verificarExistencia(problema, reservas, violaciones);
        verificarUnicidad(reservas, violaciones);
        if (exigirCobertura) {
            verificarCobertura(problema, reservas, violaciones);
        }
        verificarEquipos(reservas, violaciones);
        verificarUsuarios(problema, reservas, violaciones);
        verificarDisponibilidadYCompatibilidad(problema, reservas, violaciones);
        return violaciones;
    }

    /** Regla 0: toda reserva se refiere a elementos de S, T y C. */
    private void verificarExistencia(ProblemaPlanificacion p, List<ReservaPropuesta> reservas, List<Violacion> out) {
        for (ReservaPropuesta r : reservas) {
            if (!p.sesiones().contains(r.sesionId())) {
                out.add(new Violacion(Tipo.ENTIDAD_INEXISTENTE, "La sesión " + r.sesionId() + " no existe."));
            }
            if (!p.periodos().contains(r.periodoId())) {
                out.add(new Violacion(Tipo.ENTIDAD_INEXISTENTE, "El período " + r.periodoId() + " no existe."));
            }
            if (!p.equipos().contains(r.equipoId())) {
                out.add(new Violacion(Tipo.ENTIDAD_INEXISTENTE,
                        "El equipo " + r.equipoId() + " no existe o no está activo."));
            }
        }
    }

    /** Regla 2: si una sesión tiene dos reservas, deben coincidir en período y equipo. */
    private void verificarUnicidad(List<ReservaPropuesta> reservas, List<Violacion> out) {
        Map<Long, ReservaPropuesta> primera = new HashMap<>();
        for (ReservaPropuesta r : reservas) {
            ReservaPropuesta previa = primera.putIfAbsent(r.sesionId(), r);
            if (previa != null && (previa.periodoId() != r.periodoId() || previa.equipoId() != r.equipoId())) {
                out.add(new Violacion(Tipo.RESERVA_DUPLICADA_SESION,
                        "La sesión " + r.sesionId() + " tiene más de una reserva distinta."));
            }
        }
    }

    /** Regla 1: para toda sesión existe una reserva. */
    private void verificarCobertura(ProblemaPlanificacion p, List<ReservaPropuesta> reservas, List<Violacion> out) {
        Set<Long> cubiertas = new java.util.HashSet<>();
        for (ReservaPropuesta r : reservas) {
            cubiertas.add(r.sesionId());
        }
        for (Long sesion : p.sesiones()) {
            if (!cubiertas.contains(sesion)) {
                out.add(new Violacion(Tipo.SESION_SIN_RESERVA, "La sesión " + sesion + " no tiene reserva."));
            }
        }
    }

    /** Regla 3: dos reservas con el mismo (período, equipo) deben ser de la misma sesión. */
    private void verificarEquipos(List<ReservaPropuesta> reservas, List<Violacion> out) {
        Map<Posicion, Long> ocupante = new LinkedHashMap<>();
        for (ReservaPropuesta r : reservas) {
            Posicion posicion = new Posicion(r.periodoId(), r.equipoId());
            Long otra = ocupante.putIfAbsent(posicion, r.sesionId());
            if (otra != null && otra != r.sesionId()) {
                out.add(new Violacion(Tipo.EQUIPO_DOBLE_RESERVA,
                        "El equipo " + r.equipoId() + " está reservado en el período " + r.periodoId()
                                + " por las sesiones " + otra + " y " + r.sesionId() + "."));
            }
        }
    }

    /** Regla 4: un usuario no participa en dos sesiones distintas del mismo período. */
    private void verificarUsuarios(ProblemaPlanificacion p, List<ReservaPropuesta> reservas, List<Violacion> out) {
        Map<String, Long> actividad = new LinkedHashMap<>();
        for (ReservaPropuesta r : reservas) {
            for (Long usuario : p.participantesDe(r.sesionId())) {
                String clave = usuario + "@" + r.periodoId();
                Long otra = actividad.putIfAbsent(clave, r.sesionId());
                if (otra != null && otra != r.sesionId()) {
                    out.add(new Violacion(Tipo.USUARIO_DOBLE_ACTIVIDAD,
                            "El usuario " + usuario + " participa en las sesiones " + otra + " y "
                                    + r.sesionId() + " en el mismo período " + r.periodoId() + "."));
                }
            }
        }
    }

    /** Reglas 5 y 6: disponibilidad de usuarios y compatibilidad del equipo. */
    private void verificarDisponibilidadYCompatibilidad(ProblemaPlanificacion p, List<ReservaPropuesta> reservas,
                                                        List<Violacion> out) {
        for (ReservaPropuesta r : reservas) {
            if (!p.sesiones().contains(r.sesionId())) {
                continue;
            }
            for (Long usuario : p.participantesDe(r.sesionId())) {
                if (!p.usuarioDisponible(usuario, r.periodoId())) {
                    out.add(new Violacion(Tipo.USUARIO_NO_DISPONIBLE,
                            "El usuario " + usuario + " no está disponible en el período " + r.periodoId() + "."));
                }
            }
            if (!p.equipoCompatible(r.sesionId(), r.equipoId())) {
                out.add(new Violacion(Tipo.EQUIPO_INCOMPATIBLE,
                        "El equipo " + r.equipoId() + " no es compatible con la sesión " + r.sesionId() + "."));
            }
        }
    }
}
