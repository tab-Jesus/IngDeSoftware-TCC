package co.edu.unicartagena.turnos.dto;

import java.util.Set;

public record SesionResponse(Long id, String codigo, String titulo, Set<Long> participanteIds) {
}
