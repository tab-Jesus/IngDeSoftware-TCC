package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.domain.ProblemaPlanificacion;
import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.model.Usuario;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Traduce el estado persistido a la instancia matemática S, U, T, C, P, D y K. */
@Component
public class ProblemaFactory {

    private final SesionRepository sesiones;
    private final UsuarioRepository usuarios;
    private final PeriodoRepository periodos;
    private final EquipoRepository equipos;

    public ProblemaFactory(SesionRepository sesiones, UsuarioRepository usuarios, PeriodoRepository periodos,
                           EquipoRepository equipos) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.periodos = periodos;
        this.equipos = equipos;
    }

    @Transactional(readOnly = true)
    public ProblemaPlanificacion construir() {
        List<Sesion> listaSesiones = sesiones.findAll().stream()
                .sorted(Comparator.comparing(Sesion::getId)).toList();
        List<Long> idsPeriodos = periodos.findAllByOrderByFechaAscHoraInicioAsc().stream()
                .map(Periodo::getId).toList();
        List<Long> idsEquipos = equipos.findByActivoTrueOrderByCodigoAsc().stream()
                .map(Equipo::getId).toList();

        Map<Long, Set<Long>> participantes = new HashMap<>();
        Map<Long, Set<Long>> compatibilidad = new HashMap<>();
        for (Sesion sesion : listaSesiones) {
            participantes.put(sesion.getId(), sesion.getParticipantes().stream()
                    .map(Usuario::getId).collect(Collectors.toSet()));
            compatibilidad.put(sesion.getId(), new HashSet<>(idsEquipos));
        }
        Map<Long, Set<Long>> disponibilidad = new HashMap<>();
        for (Usuario usuario : usuarios.findAll()) {
            disponibilidad.put(usuario.getId(), usuario.getPeriodosDisponibles().stream()
                    .map(Periodo::getId).collect(Collectors.toSet()));
        }
        return new ProblemaPlanificacion(listaSesiones.stream().map(Sesion::getId).toList(), participantes,
                idsPeriodos, idsEquipos, disponibilidad, compatibilidad);
    }
}
