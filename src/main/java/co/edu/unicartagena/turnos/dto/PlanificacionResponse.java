package co.edu.unicartagena.turnos.dto;

import java.util.List;

public record PlanificacionResponse(boolean factible, boolean optimo, int periodosUsados,
                                    long nodosExplorados, List<ReservaResponse> reservas, List<String> mensajes) {
}
