package co.edu.unicartagena.turnos.controller;

import co.edu.unicartagena.turnos.dto.SesionRequest;
import co.edu.unicartagena.turnos.dto.SesionResponse;
import co.edu.unicartagena.turnos.service.SesionService;
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
@RequestMapping("/api/v1/sesiones")
public class SesionController {

    private final SesionService servicio;

    public SesionController(SesionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SesionResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public SesionResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SesionResponse crear(@Valid @RequestBody SesionRequest request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public SesionResponse actualizar(@PathVariable Long id, @Valid @RequestBody SesionRequest request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
