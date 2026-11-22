package co.edu.unicartagena.turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 100) String nombre) {
}
