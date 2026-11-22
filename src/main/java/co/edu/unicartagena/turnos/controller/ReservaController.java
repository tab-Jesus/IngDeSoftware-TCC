package co.edu.unicartagena.turnos.controller;

import co.edu.unicartagena.turnos.dto.ReservaRequest;
import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.service.ReservaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ReservaResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest request) {
        return servicio.crear(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
