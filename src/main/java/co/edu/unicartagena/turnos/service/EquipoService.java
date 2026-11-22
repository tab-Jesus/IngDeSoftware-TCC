package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.dto.EquipoRequest;
import co.edu.unicartagena.turnos.dto.EquipoResponse;
import co.edu.unicartagena.turnos.exception.ConflictoException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.ReservaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private final EquipoRepository equipos;
    private final ReservaRepository reservas;

    public EquipoService(EquipoRepository equipos, ReservaRepository reservas) {
        this.equipos = equipos;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listar() {
        return equipos.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest request) {
        if (equipos.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un equipo con código " + request.codigo() + ".");
        }
        boolean activo = request.activo() == null || request.activo();
        return aRespuesta(equipos.save(new Equipo(request.codigo(), request.descripcion(), activo)));
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo equipo = buscar(id);
        if (!equipo.getCodigo().equals(request.codigo()) && equipos.existsByCodigo(request.codigo())) {
            throw new ConflictoException("Ya existe un equipo con código " + request.codigo() + ".");
        }
        equipo.setCodigo(request.codigo());
        equipo.setDescripcion(request.descripcion());
        if (request.activo() != null) {
            equipo.setActivo(request.activo());
        }
        return aRespuesta(equipos.save(equipo));
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscar(id);
        if (reservas.existsByEquipo_Id(id)) {
            throw new ConflictoException("El equipo " + equipo.getCodigo() + " tiene reservas y no puede eliminarse.");
        }
        equipos.delete(equipo);
    }

    private Equipo buscar(Long id) {
        return equipos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Equipo", id));
    }

    private EquipoResponse aRespuesta(Equipo e) {
        return new EquipoResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.isActivo());
    }
}
