package co.edu.unicartagena.turnos.dto;

import java.util.Set;

public record UsuarioResponse(Long id, String codigo, String nombre, Set<Long> periodosDisponibles) {
}
