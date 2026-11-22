package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.domain.ProblemaPlanificacion;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda.ReservaPropuesta;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda.Violacion;
import co.edu.unicartagena.turnos.dto.ReservaRequest;
import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
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
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {

    private final ReservaRepository reservas;
    private final SesionRepository sesiones;
    private final PeriodoRepository periodos;
    private final EquipoRepository equipos;
    private final ProblemaFactory problemaFactory;
    private final ValidadorAgenda validador;

    public ReservaService(ReservaRepository reservas, SesionRepository sesiones, PeriodoRepository periodos,
                          EquipoRepository equipos, ProblemaFactory problemaFactory, ValidadorAgenda validador) {
        this.reservas = reservas;
        this.sesiones = sesiones;
        this.periodos = periodos;
        this.equipos = equipos;
        this.problemaFactory = problemaFactory;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar() {
        return reservas.findAll().stream().map(ReservaMapper::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id) {
        return ReservaMapper.aRespuesta(buscar(id));
    }

    /**
     * Registra una reserva manual solo si la agenda resultante respeta las reglas del modelo
     * (la cobertura total no se exige: pueden quedar sesiones pendientes).
     */
    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        Sesion sesion = sesiones.findById(request.sesionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión", request.sesionId()));
        Periodo periodo = periodos.findById(request.periodoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Período", request.periodoId()));
        Equipo equipo = equipos.findById(request.equipoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo", request.equipoId()));
        if (reservas.existsBySesion_Id(sesion.getId())) {
            throw new ConflictoException("La sesión " + sesion.getCodigo() + " ya tiene una reserva.");
        }

        ProblemaPlanificacion problema = problemaFactory.construir();
        List<ReservaPropuesta> propuesta = new ArrayList<>();
        for (Reserva existente : reservas.findAll()) {
            propuesta.add(new ReservaPropuesta(existente.getSesion().getId(), existente.getPeriodo().getId(),
                    existente.getEquipo().getId()));
        }
        propuesta.add(new ReservaPropuesta(sesion.getId(), periodo.getId(), equipo.getId()));

        List<Violacion> violaciones = validador.validar(problema, propuesta, false);
        if (!violaciones.isEmpty()) {
            List<ViolacionResponse> detalle = violaciones.stream()
                    .map(v -> new ViolacionResponse(v.tipo().name(), v.mensaje())).toList();
            throw new ReglaNegocioException("La reserva viola reglas de consistencia de la agenda.", detalle);
        }
        Reserva guardada = reservas.save(new Reserva(sesion, periodo, equipo, OrigenReserva.MANUAL));
        return ReservaMapper.aRespuesta(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        reservas.delete(buscar(id));
    }

    private Reserva buscar(Long id) {
        return reservas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }
}
