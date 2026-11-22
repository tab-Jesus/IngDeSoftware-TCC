package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(@NotNull Long sesionId, @NotNull Long periodoId, @NotNull Long equipoId) {
}
