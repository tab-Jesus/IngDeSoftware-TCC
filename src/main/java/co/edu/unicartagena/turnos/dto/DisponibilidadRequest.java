package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record DisponibilidadRequest(@NotNull Set<Long> periodoIds) {
}
