package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record PeriodoRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin) {
}
