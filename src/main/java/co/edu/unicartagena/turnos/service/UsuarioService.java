package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.dto.DisponibilidadRequest;
import co.edu.unicartagena.turnos.dto.UsuarioRequest;
import co.edu.unicartagena.turnos.dto.UsuarioResponse;
import co.edu.unicartagena.turnos.exception.ConflictoException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Usuario;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import co.edu.unicartagena.turnos.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PeriodoRepository periodos;
    private final SesionRepository sesiones;

    public UsuarioService(UsuarioRepository usuarios, PeriodoRepository periodos, SesionRepository sesiones) {
        this.usuarios = usuarios;
        this.periodos = periodos;
        this.sesiones = sesiones;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    /** Un usuario nuevo queda disponible en todos los períodos existentes (D = U × T por defecto). */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarios.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un usuario con código " + request.codigo() + ".");
        }
        Usuario usuario = new Usuario(request.codigo(), request.nombre());
        usuario.setPeriodosDisponibles(new HashSet<>(periodos.findAll()));
        return aRespuesta(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        if (!usuario.getCodigo().equals(request.codigo()) && usuarios.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un usuario con código " + request.codigo() + ".");
        }
        usuario.setCodigo(request.codigo());
        usuario.setNombre(request.nombre());
        return aRespuesta(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse definirDisponibilidad(Long id, DisponibilidadRequest request) {
        Usuario usuario = buscar(id);
        List<Periodo> encontrados = periodos.findAllById(request.periodoIds());
        if (encontrados.size() != request.periodoIds().size()) {
            throw new RecursoNoEncontradoException("Alguno de los períodos indicados no existe.");
        }
        usuario.setPeriodosDisponibles(new HashSet<>(encontrados));
        return aRespuesta(usuarios.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (sesiones.existsByParticipantes_Id(id)) {
            throw new ConflictoException("El usuario " + usuario.getCodigo()
                    + " participa en sesiones y no puede eliminarse.");
        }
        usuarios.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        Set<Long> disponibles = u.getPeriodosDisponibles().stream().map(Periodo::getId).collect(Collectors.toSet());
        return new UsuarioResponse(u.getId(), u.getCodigo(), u.getNombre(), disponibles);
    }
}
