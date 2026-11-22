package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record SesionRequest(
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 150) String titulo,
        @NotEmpty Set<Long> participanteIds) {
}
