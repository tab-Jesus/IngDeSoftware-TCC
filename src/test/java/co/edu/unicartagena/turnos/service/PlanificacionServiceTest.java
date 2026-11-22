package co.edu.unicartagena.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicartagena.turnos.domain.PlanificadorColoracion;
import co.edu.unicartagena.turnos.domain.ProblemaPlanificacion;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda;
import co.edu.unicartagena.turnos.dto.PlanificacionResponse;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.OrigenReserva;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Reserva;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlanificacionServiceTest {

    @Mock
    private ProblemaFactory problemaFactory;
    @Mock
    private ReservaRepository reservas;
    @Mock
    private SesionRepository sesiones;
    @Mock
    private PeriodoRepository periodos;
    @Mock
    private EquipoRepository equipos;
    @Mock
    private UsuarioRepository usuarios;

    private PlanificacionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PlanificacionService(problemaFactory, new PlanificadorColoracion(), new ValidadorAgenda(),
                reservas, sesiones, periodos, equipos, usuarios);
    }

    private static <T> T conId(T entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }

    @Test
    @DisplayName("Si no existe solución no se modifican las reservas almacenadas")
    void infactibleNoModificaReservas() {
        // El usuario 10 participa en dos sesiones pero solo tiene un período disponible.
        ProblemaPlanificacion problema = new ProblemaPlanificacion(
                List.of(1L, 2L),
                Map.of(1L, Set.of(10L), 2L, Set.of(10L)),
                List.of(1L, 2L),
                List.of(1L, 2L),
                Map.of(10L, Set.of(1L)),
                Map.of(1L, Set.of(1L, 2L), 2L, Set.of(1L, 2L)));
        when(problemaFactory.construir()).thenReturn(problema);

        PlanificacionResponse respuesta = servicio.generar(true);

        assertFalse(respuesta.factible());
        assertTrue(respuesta.reservas().isEmpty());
        assertFalse(respuesta.mensajes().isEmpty());
        verify(reservas, never()).deleteAllInBatch();
        verify(reservas, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("Con persistir=false se calcula la agenda sin tocar la base de datos")
    void calcularSinPersistir() {
        ProblemaPlanificacion problema = new ProblemaPlanificacion(
                List.of(1L), Map.of(1L, Set.of(10L)), List.of(1L), List.of(1L),
                Map.of(10L, Set.of(1L)), Map.of(1L, Set.of(1L)));
        when(problemaFactory.construir()).thenReturn(problema);
        Sesion sesion = conId(new Sesion("A", "Sesión A", new HashSet<>()), 1L);
        Periodo periodo = conId(new Periodo("t1", LocalDate.of(2026, 11, 2), LocalTime.of(8, 0),
                LocalTime.of(9, 0)), 1L);
        Equipo equipo = conId(new Equipo("c1", "Computador 1", true), 1L);
        when(sesiones.findAll()).thenReturn(List.of(sesion));
        when(periodos.findAll()).thenReturn(List.of(periodo));
        when(equipos.findAll()).thenReturn(List.of(equipo));

        PlanificacionResponse respuesta = servicio.generar(false);

        assertTrue(respuesta.factible());
        assertEquals(1, respuesta.periodosUsados());
        assertEquals(1, respuesta.reservas().size());
        assertEquals("AUTOMATICA", respuesta.reservas().get(0).origen());
        verify(reservas, never()).deleteAllInBatch();
        verify(reservas, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("La validación informa las sesiones sin reserva")
    void validarAgendaVacia() {
        ProblemaPlanificacion problema = new ProblemaPlanificacion(
                List.of(1L), Map.of(1L, Set.of(10L)), List.of(1L), List.of(1L),
                Map.of(10L, Set.of(1L)), Map.of(1L, Set.of(1L)));
        when(problemaFactory.construir()).thenReturn(problema);
        when(reservas.findAll()).thenReturn(List.of());

        List<ViolacionResponse> violaciones = servicio.validarAgendaActual();

        assertEquals(1, violaciones.size());
        assertEquals("SESION_SIN_RESERVA", violaciones.get(0).tipo());
    }

    @Test
    @DisplayName("La validación acepta una agenda consistente")
    void validarAgendaConsistente() {
        ProblemaPlanificacion problema = new ProblemaPlanificacion(
                List.of(1L), Map.of(1L, Set.of(10L)), List.of(1L), List.of(1L),
                Map.of(10L, Set.of(1L)), Map.of(1L, Set.of(1L)));
        when(problemaFactory.construir()).thenReturn(problema);
        Sesion sesion = conId(new Sesion("A", "Sesión A", new HashSet<>()), 1L);
        Periodo periodo = conId(new Periodo("t1", LocalDate.of(2026, 11, 2), LocalTime.of(8, 0),
                LocalTime.of(9, 0)), 1L);
        Equipo equipo = conId(new Equipo("c1", "Computador 1", true), 1L);
        when(reservas.findAll()).thenReturn(List.of(new Reserva(sesion, periodo, equipo, OrigenReserva.AUTOMATICA)));

        assertTrue(servicio.validarAgendaActual().isEmpty());
    }
}
