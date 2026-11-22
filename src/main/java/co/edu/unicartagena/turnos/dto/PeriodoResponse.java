package co.edu.unicartagena.turnos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PeriodoResponse(Long id, String codigo, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
}
