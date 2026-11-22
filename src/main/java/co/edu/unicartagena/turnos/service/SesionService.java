package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.dto.SesionRequest;
import co.edu.unicartagena.turnos.dto.SesionResponse;
import co.edu.unicartagena.turnos.exception.ConflictoException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.model.Usuario;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SesionService {

    private final SesionRepository sesiones;
    private final UsuarioRepository usuarios;
    private final ReservaRepository reservas;

    public SesionService(SesionRepository sesiones, UsuarioRepository usuarios, ReservaRepository reservas) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<SesionResponse> listar() {
        return sesiones.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public SesionResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public SesionResponse crear(SesionRequest request) {
        if (sesiones.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe una sesión con código " + request.codigo() + ".");
        }
        Sesion sesion = new Sesion(request.codigo(), request.titulo(), resolverParticipantes(request.participanteIds()));
        return aRespuesta(sesiones.save(sesion));
    }

    /** Si la sesión ya tenía reserva, esta se elimina: la agenda debe validarse de nuevo. */
    @Transactional
    public SesionResponse actualizar(Long id, SesionRequest request) {
        Sesion sesion = buscar(id);
        if (!sesion.getCodigo().equals(request.codigo()) && sesiones.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe una sesión con código " + request.codigo() + ".");
        }
        reservas.findBySesion_Id(id).ifPresent(reservas::delete);
        sesion.setCodigo(request.codigo());
        sesion.setTitulo(request.titulo());
        sesion.setParticipantes(resolverParticipantes(request.participanteIds()));
        return aRespuesta(sesiones.save(sesion));
    }

    @Transactional
    public void eliminar(Long id) {
        Sesion sesion = buscar(id);
        reservas.findBySesion_Id(id).ifPresent(reservas::delete);
        sesiones.delete(sesion);
    }

    private Set<Usuario> resolverParticipantes(Set<Long> ids) {
        List<Usuario> encontrados = usuarios.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RecursoNoEncontradoException("Alguno de los participantes indicados no existe.");
        }
        return new HashSet<>(encontrados);
    }

    private Sesion buscar(Long id) {
        return sesiones.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sesión", id));
    }

    private SesionResponse aRespuesta(Sesion s) {
        Set<Long> ids = s.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toSet());
        return new SesionResponse(s.getId(), s.getCodigo(), s.getTitulo(), ids);
    }
}
