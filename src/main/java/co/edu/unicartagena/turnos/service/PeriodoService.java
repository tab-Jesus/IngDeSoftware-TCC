package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.dto.PeriodoRequest;
import co.edu.unicartagena.turnos.dto.PeriodoResponse;
import co.edu.unicartagena.turnos.exception.ConflictoException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.exception.ReglaNegocioException;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodoService {

    private final PeriodoRepository periodos;
    private final ReservaRepository reservas;

    public PeriodoService(PeriodoRepository periodos, ReservaRepository reservas) {
        this.periodos = periodos;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<PeriodoResponse> listar() {
        return periodos.findAllByOrderByFechaAscHoraInicioAsc().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public PeriodoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public PeriodoResponse crear(PeriodoRequest request) {
        validarHorario(request);
        if (periodos.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un período con código " + request.codigo() + ".");
        }
        Periodo periodo = new Periodo(request.codigo(), request.fecha(), request.horaInicio(), request.horaFin());
        return aRespuesta(periodos.save(periodo));
    }

    @Transactional
    public PeriodoResponse actualizar(Long id, PeriodoRequest request) {
        validarHorario(request);
        Periodo periodo = buscar(id);
        if (!periodo.getCodigo().equals(request.codigo()) && periodos.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un período con código " + request.codigo() + ".");
        }
        periodo.setCodigo(request.codigo());
        periodo.setFecha(request.fecha());
        periodo.setHoraInicio(request.horaInicio());
        periodo.setHoraFin(request.horaFin());
        return aRespuesta(periodos.save(periodo));
    }

    @Transactional
    public void eliminar(Long id) {
        Periodo periodo = buscar(id);
        if (reservas.existsByPeriodo_Id(id)) {
            throw new ConflictoException("El período " + periodo.getCodigo() + " tiene reservas y no puede eliminarse.");
        }
        periodos.delete(periodo);
    }

    private void validarHorario(PeriodoRequest request) {
        if (!request.horaInicio().isBefore(request.horaFin())) {
            throw new ReglaNegocioException("El horario del período no es válido.",
                    List.of(new ViolacionResponse("HORARIO_INVALIDO", "La hora de inicio debe ser anterior a la hora de fin.")));
        }
    }

    private Periodo buscar(Long id) {
        return periodos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Período", id));
    }

    private PeriodoResponse aRespuesta(Periodo p) {
        return new PeriodoResponse(p.getId(), p.getCodigo(), p.getFecha(), p.getHoraInicio(), p.getHoraFin());
    }
}
