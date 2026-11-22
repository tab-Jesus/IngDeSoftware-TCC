package co.edu.unicartagena.turnos.dto;

public record ReservaResponse(Long id, Long sesionId, String sesionCodigo, Long periodoId, String periodoCodigo,
                              Long equipoId, String equipoCodigo, String origen) {
}
