package co.edu.unicartagena.turnos.config;

import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.model.Usuario;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga la instancia simulada del TCC (6 usuarios, 7 sesiones A-G, 3 períodos y 3 equipos)
 * cuando {@code app.seed.enabled=true}. Es idempotente: no hace nada si ya hay usuarios.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatosEjemplo implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PeriodoRepository periodos;
    private final EquipoRepository equipos;
    private final SesionRepository sesiones;

    public DatosEjemplo(UsuarioRepository usuarios, PeriodoRepository periodos, EquipoRepository equipos,
                        SesionRepository sesiones) {
        this.usuarios = usuarios;
        this.periodos = periodos;
        this.equipos = equipos;
        this.sesiones = sesiones;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        LocalDate jornada = LocalDate.now().plusDays(1);
        List<Periodo> periodosCreados = periodos.saveAll(List.of(
                new Periodo("t1", jornada, LocalTime.of(8, 0), LocalTime.of(9, 0)),
                new Periodo("t2", jornada, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new Periodo("t3", jornada, LocalTime.of(10, 0), LocalTime.of(11, 0))));

        equipos.saveAll(List.of(
                new Equipo("c1", "Computador 1", true),
                new Equipo("c2", "Computador 2", true),
                new Equipo("c3", "Computador 3", true)));

        Map<String, Usuario> porCodigo = new LinkedHashMap<>();
        for (int i = 1; i <= 6; i++) {
            Usuario usuario = new Usuario("u" + i, "Usuario " + i);
            usuario.setPeriodosDisponibles(new HashSet<>(periodosCreados));
            porCodigo.put(usuario.getCodigo(), usuarios.save(usuario));
        }

        Map<String, String[]> participacion = new LinkedHashMap<>();
        participacion.put("A", new String[] {"u1", "u2"});
        participacion.put("B", new String[] {"u2", "u3"});
        participacion.put("C", new String[] {"u1", "u3"});
        participacion.put("D", new String[] {"u3", "u4"});
        participacion.put("E", new String[] {"u4", "u5"});
        participacion.put("F", new String[] {"u5", "u6"});
        participacion.put("G", new String[] {"u1", "u6"});
        participacion.forEach((codigo, miembros) -> {
            Set<Usuario> participantes = new HashSet<>();
            for (String miembro : miembros) {
                participantes.add(porCodigo.get(miembro));
            }
            sesiones.save(new Sesion(codigo, "Sesión " + codigo, participantes));
        });
    }
}
