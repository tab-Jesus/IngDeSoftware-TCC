package co.edu.unicartagena.turnos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicartagena.turnos.domain.ValidadorAgenda.ReservaPropuesta;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda.Tipo;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda.Violacion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidadorAgendaTest {

    private ValidadorAgenda validador;
    private ProblemaPlanificacion problema;

    @BeforeEach
    void preparar() {
        validador = new ValidadorAgenda();
        problema = Instancias.base();
    }

    private boolean contiene(List<Violacion> violaciones, Tipo tipo) {
        return violaciones.stream().anyMatch(v -> v.tipo() == tipo);
    }

    private List<ReservaPropuesta> reemplazar(long sesion, ReservaPropuesta nueva) {
        List<ReservaPropuesta> agenda = new ArrayList<>(Instancias.sin(Instancias.agendaDelTcc(), sesion));
        agenda.add(nueva);
        return agenda;
    }

    @Test
    @DisplayName("La agenda del TCC es válida")
    void agendaValida() {
        assertTrue(validador.validar(problema, Instancias.agendaDelTcc(), true).isEmpty());
    }

    @Test
    @DisplayName("Compartir período es válido si no hay usuarios ni equipos en común (A y D en t1)")
    void simultaneidadPermitida() {
        List<ReservaPropuesta> parcial = List.of(new ReservaPropuesta(Instancias.A, 1, 1),
                new ReservaPropuesta(Instancias.D, 1, 2));
        assertTrue(validador.validar(problema, parcial, false).isEmpty());
    }

    @Test
    @DisplayName("Mover B a t1 en c2 provoca doble actividad de u2")
    void conflictoDeUsuario() {
        List<Violacion> v = validador.validar(problema,
                reemplazar(Instancias.B, new ReservaPropuesta(Instancias.B, 1, 3)), true);
        assertTrue(contiene(v, Tipo.USUARIO_DOBLE_ACTIVIDAD));
    }

    @Test
    @DisplayName("Se reportan a la vez el conflicto de usuario y la doble reserva de equipo")
    void variasViolaciones() {
        List<Violacion> v = validador.validar(problema,
                reemplazar(Instancias.B, new ReservaPropuesta(Instancias.B, 1, 2)), true);
        assertTrue(contiene(v, Tipo.USUARIO_DOBLE_ACTIVIDAD));
        assertTrue(contiene(v, Tipo.EQUIPO_DOBLE_RESERVA));
    }

    @Test
    @DisplayName("Una reserva de una sesión inexistente (H) se rechaza")
    void sesionInexistente() {
        List<ReservaPropuesta> agenda = new ArrayList<>(Instancias.agendaDelTcc());
        agenda.add(new ReservaPropuesta(99, 3, 2));
        assertTrue(contiene(validador.validar(problema, agenda, true), Tipo.ENTIDAD_INEXISTENTE));
    }

    @Test
    @DisplayName("Quitar la reserva de C deja una agenda incompleta")
    void faltaCobertura() {
        List<Violacion> v = validador.validar(problema, Instancias.sin(Instancias.agendaDelTcc(), Instancias.C), true);
        assertEquals(1, v.size());
        assertEquals(Tipo.SESION_SIN_RESERVA, v.get(0).tipo());
    }

    @Test
    @DisplayName("La cobertura no se exige en la validación parcial")
    void coberturaOpcional() {
        assertTrue(validador.validar(problema, Instancias.sin(Instancias.agendaDelTcc(), Instancias.C), false)
                .isEmpty());
    }

    @Test
    @DisplayName("Una sesión con dos reservas distintas viola la unicidad")
    void unicidadDeReservaPorSesion() {
        List<ReservaPropuesta> agenda = new ArrayList<>(Instancias.agendaDelTcc());
        agenda.add(new ReservaPropuesta(Instancias.A, 3, 3));
        assertTrue(contiene(validador.validar(problema, agenda, true), Tipo.RESERVA_DUPLICADA_SESION));
    }

    @Test
    @DisplayName("Se detecta un usuario no disponible en el período reservado")
    void usuarioNoDisponible() {
        ProblemaPlanificacion restringido = Instancias.construir(problema.participantes(),
                List.of(1L, 2L, 3L), List.of(1L, 2L, 3L), Map.of(1L, Set.of(2L, 3L)));
        assertTrue(contiene(validador.validar(restringido, Instancias.agendaDelTcc(), true),
                Tipo.USUARIO_NO_DISPONIBLE));
    }

    @Test
    @DisplayName("Se detecta un equipo incompatible con la sesión")
    void equipoIncompatible() {
        ProblemaPlanificacion sinC1ParaA = new ProblemaPlanificacion(problema.sesiones(), problema.participantes(),
                problema.periodos(), problema.equipos(), problema.disponibilidad(),
                Map.of(Instancias.A, Set.of(2L, 3L), Instancias.B, Set.of(1L, 2L, 3L),
                        Instancias.C, Set.of(1L, 2L, 3L), Instancias.D, Set.of(1L, 2L, 3L),
                        Instancias.E, Set.of(1L, 2L, 3L), Instancias.F, Set.of(1L, 2L, 3L),
                        Instancias.G, Set.of(1L, 2L, 3L)));
        assertTrue(contiene(validador.validar(sinC1ParaA, Instancias.agendaDelTcc(), true),
                Tipo.EQUIPO_INCOMPATIBLE));
    }
}
