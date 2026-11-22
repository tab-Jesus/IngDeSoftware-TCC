package co.edu.unicartagena.turnos.controller;

import co.edu.unicartagena.turnos.dto.PeriodoRequest;
import co.edu.unicartagena.turnos.dto.PeriodoResponse;
import co.edu.unicartagena.turnos.service.PeriodoService;
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
@RequestMapping("/api/v1/periodos")
public class PeriodoController {

    private final PeriodoService servicio;

    public PeriodoController(PeriodoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PeriodoResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public PeriodoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodoResponse crear(@Valid @RequestBody PeriodoRequest request) {
        return servicio.crear(request);
    }

    @PutMapping("/{id}")
    public PeriodoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PeriodoRequest request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
