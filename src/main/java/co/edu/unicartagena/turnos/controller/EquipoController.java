package co.edu.unicartagena.turnos.controller;

import co.edu.unicartagena.turnos.dto.EquipoRequest;
import co.edu.unicartagena.turnos.dto.EquipoResponse;
import co.edu.unicartagena.turnos.service.EquipoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equipos")
public class EquipoController {

    private final EquipoService servicio;

    public EquipoController(EquipoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<EquipoResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@Valid @RequestBody EquipoRequest request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequest request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
