package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 200) String descripcion,
        Boolean activo) {
}
