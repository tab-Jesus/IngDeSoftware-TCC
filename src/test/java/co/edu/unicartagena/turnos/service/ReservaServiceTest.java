package co.edu.unicartagena.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicartagena.turnos.domain.ProblemaPlanificacion;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda;
import co.edu.unicartagena.turnos.dto.ReservaRequest;
import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.exception.ConflictoException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.exception.ReglaNegocioException;
import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.OrigenReserva;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Reserva;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservas;
    @Mock
    private SesionRepository sesiones;
    @Mock
    private PeriodoRepository periodos;
    @Mock
    private EquipoRepository equipos;
    @Mock
    private ProblemaFactory problemaFactory;

    private ReservaService servicio;
    private Sesion sesion1;
    private Sesion sesion2;
    private Periodo periodo1;
    private Periodo periodo2;
    private Equipo equipo1;
    private Equipo equipo2;

    @BeforeEach
    void preparar() {
        servicio = new ReservaService(reservas, sesiones, periodos, equipos, problemaFactory, new ValidadorAgenda());
        sesion1 = conId(new Sesion("A", "Sesión A", new HashSet<>()), 1L);
        sesion2 = conId(new Sesion("B", "Sesión B", new HashSet<>()), 2L);
        LocalDate dia = LocalDate.of(2026, 11, 2);
        periodo1 = conId(new Periodo("t1", dia, LocalTime.of(8, 0), LocalTime.of(9, 0)), 1L);
        periodo2 = conId(new Periodo("t2", dia, LocalTime.of(9, 0), LocalTime.of(10, 0)), 2L);
        equipo1 = conId(new Equipo("c1", "Computador 1", true), 1L);
        equipo2 = conId(new Equipo("c2", "Computador 2", true), 2L);
    }

    private static <T> T conId(T entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }

    /** Sesión 1 = {10, 11}; sesión 2 = {11, 12}; comparten al usuario 11. */
    private ProblemaPlanificacion problema() {
        return new ProblemaPlanificacion(
                List.of(1L, 2L),
                Map.of(1L, Set.of(10L, 11L), 2L, Set.of(11L, 12L)),
                List.of(1L, 2L),
                List.of(1L, 2L),
                Map.of(10L, Set.of(1L, 2L), 11L, Set.of(1L, 2L), 12L, Set.of(1L, 2L)),
                Map.of(1L, Set.of(1L, 2L), 2L, Set.of(1L, 2L)));
    }

    private void stubBusquedaDeLaSesion2() {
        when(sesiones.findById(2L)).thenReturn(Optional.of(sesion2));
        when(periodos.findById(1L)).thenReturn(Optional.of(periodo1));
        when(equipos.findById(2L)).thenReturn(Optional.of(equipo2));
    }

    @Test
    @DisplayName("Reservar una sesión inexistente lanza RecursoNoEncontradoException")
    void sesionInexistente() {
        when(sesiones.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.crear(new ReservaRequest(99L, 1L, 1L)));
        verify(reservas, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Una sesión que ya tiene reserva produce un conflicto")
    void sesionYaReservada() {
        stubBusquedaDeLaSesion2();
        when(reservas.existsBySesion_Id(2L)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> servicio.crear(new ReservaRequest(2L, 1L, 2L)));
        verify(reservas, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Una reserva que hace participar a un usuario en dos sesiones simultáneas se rechaza")
    void conflictoDeUsuario() {
        stubBusquedaDeLaSesion2();
        when(problemaFactory.construir()).thenReturn(problema());
        when(reservas.findAll()).thenReturn(List.of(new Reserva(sesion1, periodo1, equipo1, OrigenReserva.MANUAL)));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(new ReservaRequest(2L, 1L, 2L)));
        assertTrue(ex.getViolaciones().stream().anyMatch(v -> v.tipo().equals("USUARIO_DOBLE_ACTIVIDAD")));
        verify(reservas, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Una reserva consistente se guarda como manual")
    void reservaValida() {
        when(sesiones.findById(2L)).thenReturn(Optional.of(sesion2));
        when(periodos.findById(2L)).thenReturn(Optional.of(periodo2));
        when(equipos.findById(1L)).thenReturn(Optional.of(equipo1));
        when(problemaFactory.construir()).thenReturn(problema());
        when(reservas.findAll()).thenReturn(List.of(new Reserva(sesion1, periodo1, equipo1, OrigenReserva.MANUAL)));
        when(reservas.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse respuesta = servicio.crear(new ReservaRequest(2L, 2L, 1L));

        assertEquals("B", respuesta.sesionCodigo());
        assertEquals("t2", respuesta.periodoCodigo());
        assertEquals("c1", respuesta.equipoCodigo());
        assertEquals("MANUAL", respuesta.origen());
    }
}
