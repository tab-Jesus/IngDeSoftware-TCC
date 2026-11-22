package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.domain.GrafoConflictos;
import co.edu.unicartagena.turnos.domain.PlanificadorColoracion;
import co.edu.unicartagena.turnos.domain.Posicion;
import co.edu.unicartagena.turnos.domain.ProblemaPlanificacion;
import co.edu.unicartagena.turnos.domain.ResultadoPlanificacion;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda;
import co.edu.unicartagena.turnos.domain.ValidadorAgenda.ReservaPropuesta;
import co.edu.unicartagena.turnos.dto.GrafoResponse;
import co.edu.unicartagena.turnos.dto.PlanificacionResponse;
import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.OrigenReserva;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Reserva;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.model.Usuario;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanificacionService {

    private final ProblemaFactory problemaFactory;
    private final PlanificadorColoracion planificador;
    private final ValidadorAgenda validador;
    private final ReservaRepository reservas;
    private final SesionRepository sesiones;
    private final PeriodoRepository periodos;
    private final EquipoRepository equipos;
    private final UsuarioRepository usuarios;

    public PlanificacionService(ProblemaFactory problemaFactory, PlanificadorColoracion planificador,
                                ValidadorAgenda validador, ReservaRepository reservas, SesionRepository sesiones,
                                PeriodoRepository periodos, EquipoRepository equipos, UsuarioRepository usuarios) {
        this.problemaFactory = problemaFactory;
        this.planificador = planificador;
        this.validador = validador;
        this.reservas = reservas;
        this.sesiones = sesiones;
        this.periodos = periodos;
        this.equipos = equipos;
        this.usuarios = usuarios;
    }

    /**
     * Calcula la agenda óptima en cantidad de períodos. Si {@code persistir} es verdadero y existe
     * solución, reemplaza las reservas actuales; si no hay solución, no modifica nada.
     */
    @Transactional
    public PlanificacionResponse generar(boolean persistir) {
        ProblemaPlanificacion problema = problemaFactory.construir();
        ResultadoPlanificacion resultado = planificador.resolver(problema);
        if (!resultado.factible()) {
            return new PlanificacionResponse(false, false, 0, resultado.nodosExplorados(), List.of(),
                    resultado.mensajes());
        }
        Map<Long, Sesion> sesionesPorId = sesiones.findAll().stream()
                .collect(Collectors.toMap(Sesion::getId, Function.identity()));
        Map<Long, Periodo> periodosPorId = periodos.findAll().stream()
                .collect(Collectors.toMap(Periodo::getId, Function.identity()));
        Map<Long, Equipo> equiposPorId = equipos.findAll().stream()
                .collect(Collectors.toMap(Equipo::getId, Function.identity()));

        List<Reserva> nuevas = new ArrayList<>();
        for (Map.Entry<Long, Posicion> asignacion : resultado.asignaciones().entrySet()) {
            Posicion posicion = asignacion.getValue();
            nuevas.add(new Reserva(sesionesPorId.get(asignacion.getKey()), periodosPorId.get(posicion.periodoId()),
                    equiposPorId.get(posicion.equipoId()), OrigenReserva.AUTOMATICA));
        }
        List<Reserva> resultadoReservas = nuevas;
        if (persistir) {
            reservas.deleteAllInBatch();
            resultadoReservas = reservas.saveAll(nuevas);
        }
        List<ReservaResponse> respuesta = resultadoReservas.stream().map(ReservaMapper::aRespuesta).toList();
        return new PlanificacionResponse(true, resultado.optimo(), resultado.periodosUsados(),
                resultado.nodosExplorados(), respuesta, resultado.mensajes());
    }

    /** Aplica las seis reglas de lógica de predicados sobre la agenda almacenada. */
    @Transactional(readOnly = true)
    public List<ViolacionResponse> validarAgendaActual() {
        ProblemaPlanificacion problema = problemaFactory.construir();
        List<ReservaPropuesta> propuesta = reservas.findAll().stream()
                .map(r -> new ReservaPropuesta(r.getSesion().getId(), r.getPeriodo().getId(), r.getEquipo().getId()))
                .toList();
        return validador.validar(problema, propuesta, true).stream()
                .map(v -> new ViolacionResponse(v.tipo().name(), v.mensaje())).toList();
    }

    @Transactional(readOnly = true)
    public GrafoResponse grafo() {
        ProblemaPlanificacion problema = problemaFactory.construir();
        GrafoConflictos grafo = GrafoConflictos.construir(problema);
        Map<Long, String> codigosSesion = sesiones.findAll().stream()
                .collect(Collectors.toMap(Sesion::getId, Sesion::getCodigo));
        Map<Long, String> codigosUsuario = usuarios.findAll().stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getCodigo));
        List<String> nodos = grafo.vertices().stream().map(codigosSesion::get).toList();
        List<GrafoResponse.Arista> aristas = grafo.aristas().stream()
                .map(a -> new GrafoResponse.Arista(codigosSesion.get(a.origen()), codigosSesion.get(a.destino()),
                        a.usuariosCompartidos().stream().map(codigosUsuario::get).sorted().toList()))
                .toList();
        return new GrafoResponse(nodos, aristas, grafo.cotaInferiorCromatica());
    }
}
