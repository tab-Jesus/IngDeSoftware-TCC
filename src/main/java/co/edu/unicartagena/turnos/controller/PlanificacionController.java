package co.edu.unicartagena.turnos.controller;

import co.edu.unicartagena.turnos.dto.GrafoResponse;
import co.edu.unicartagena.turnos.dto.PlanificacionResponse;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import co.edu.unicartagena.turnos.service.PlanificacionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/planificacion")
public class PlanificacionController {

    private final PlanificacionService servicio;

    public PlanificacionController(PlanificacionService servicio) {
        this.servicio = servicio;
    }

    /** Genera la agenda con el mínimo de períodos. Con {@code persistir=false} solo la calcula. */
    @PostMapping("/generar")
    public PlanificacionResponse generar(@RequestParam(defaultValue = "true") boolean persistir) {
        return servicio.generar(persistir);
    }

    /** Devuelve las violaciones de las reglas; una lista vacía significa agenda consistente. */
    @GetMapping("/validar")
    public List<ViolacionResponse> validar() {
        return servicio.validarAgendaActual();
    }

    @GetMapping("/grafo")
    public GrafoResponse grafo() {
        return servicio.grafo();
    }
}
