package co.edu.unicartagena.turnos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanificadorColoracionTest {

    private PlanificadorColoracion planificador;
    private ValidadorAgenda validador;

    @BeforeEach
    void preparar() {
        planificador = new PlanificadorColoracion();
        validador = new ValidadorAgenda();
    }

    private List<ValidadorAgenda.ReservaPropuesta> comoReservas(ResultadoPlanificacion r) {
        List<ValidadorAgenda.ReservaPropuesta> reservas = new ArrayList<>();
        r.asignaciones().forEach((sesion, pos) ->
                reservas.add(new ValidadorAgenda.ReservaPropuesta(sesion, pos.periodoId(), pos.equipoId())));
        return reservas;
    }

    @Test
    @DisplayName("La instancia del TCC se programa en exactamente tres períodos")
    void instanciaBaseUsaTresPeriodos() {
        ProblemaPlanificacion problema = Instancias.base();
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertTrue(r.factible());
        assertEquals(3, r.periodosUsados());
        assertEquals(7, r.asignaciones().size());
        assertTrue(r.optimo());
    }

    @Test
    @DisplayName("La agenda generada pasa el verificador independiente sin violaciones")
    void agendaGeneradaEsValida() {
        ProblemaPlanificacion problema = Instancias.base();
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertTrue(validador.validar(problema, comoReservas(r), true).isEmpty());
    }

    @Test
    @DisplayName("La agenda generada es una coloración propia del grafo de conflictos")
    void agendaEsColoracionPropia() {
        ProblemaPlanificacion problema = Instancias.base();
        ResultadoPlanificacion r = planificador.resolver(problema);
        Map<Long, Long> colores = new LinkedHashMap<>();
        r.asignaciones().forEach((s, pos) -> colores.put(s, pos.periodoId()));
        assertTrue(GrafoConflictos.construir(problema).esColoracionPropia(colores));
    }

    @Test
    @DisplayName("Si u1 solo está disponible en dos períodos no existe solución (palomar)")
    void infactiblePorDisponibilidad() {
        ProblemaPlanificacion problema = Instancias.construir(
                Instancias.base().participantes(), List.of(1L, 2L, 3L), List.of(1L, 2L, 3L),
                Map.of(1L, Set.of(1L, 2L)));
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertFalse(r.factible());
        assertTrue(r.asignaciones().isEmpty());
        assertFalse(r.mensajes().isEmpty());
    }

    @Test
    @DisplayName("Con un solo equipo las siete sesiones requieren siete períodos")
    void escasezDeEquipos() {
        Map<Long, Set<Long>> participantes = new LinkedHashMap<>();
        for (long s = 1; s <= 7; s++) {
            participantes.put(s, Set.of(10 + s));
        }
        List<Long> periodos = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        ProblemaPlanificacion problema = Instancias.construir(participantes, periodos, List.of(1L), Map.of());
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertTrue(r.factible());
        assertEquals(7, r.periodosUsados());
    }

    @Test
    @DisplayName("Sesiones sin usuarios compartidos comparten período si hay equipos suficientes")
    void sesionesIndependientesComparten() {
        Map<Long, Set<Long>> participantes = new LinkedHashMap<>();
        participantes.put(1L, Set.of(1L, 2L));
        participantes.put(2L, Set.of(3L, 4L));
        participantes.put(3L, Set.of(5L, 6L));
        ProblemaPlanificacion problema = Instancias.construir(participantes, List.of(1L, 2L, 3L),
                List.of(1L, 2L, 3L), Map.of());
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertTrue(r.factible());
        assertEquals(1, r.periodosUsados());
    }

    @Test
    @DisplayName("Una sesión sin ningún período admisible hace infactible el problema con mensaje")
    void dominioVacio() {
        ProblemaPlanificacion problema = Instancias.construir(
                Instancias.base().participantes(), List.of(1L, 2L, 3L), List.of(1L, 2L, 3L),
                Map.of(2L, Set.of()));
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertFalse(r.factible());
        assertTrue(r.mensajes().get(0).contains("no tiene ningún período"));
    }

    @Test
    @DisplayName("Sin sesiones el resultado es factible y vacío")
    void sinSesiones() {
        ProblemaPlanificacion problema = new ProblemaPlanificacion(List.of(), Map.of(), List.of(1L),
                List.of(1L), Map.of(), Map.of());
        ResultadoPlanificacion r = planificador.resolver(problema);
        assertTrue(r.factible());
        assertEquals(0, r.periodosUsados());
    }

    @Test
    @DisplayName("El resultado es determinista para la misma entrada")
    void determinista() {
        ProblemaPlanificacion problema = Instancias.base();
        assertEquals(planificador.resolver(problema).asignaciones(), planificador.resolver(problema).asignaciones());
    }

    @Test
    @DisplayName("Con un presupuesto mínimo no se afirma que el problema sea infactible")
    void presupuestoAgotado() {
        ResultadoPlanificacion r = new PlanificadorColoracion(1).resolver(Instancias.base());
        assertFalse(r.factible());
        assertTrue(r.mensajes().get(0).contains("presupuesto"));
    }

    @Test
    @DisplayName("Una instancia infactible por disponibilidad no es optimizable y se marca no óptima")
    void infactibleNoEsOptima() {
        ProblemaPlanificacion problema = Instancias.construir(
                Instancias.base().participantes(), List.of(1L, 2L, 3L), List.of(1L, 2L, 3L),
                Map.of(1L, Set.of(1L, 2L)));
        assertFalse(planificador.resolver(problema).optimo());
    }
}
