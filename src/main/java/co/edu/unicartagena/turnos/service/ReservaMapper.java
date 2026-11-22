package co.edu.unicartagena.turnos.service;

import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.model.Reserva;

final class ReservaMapper {

    private ReservaMapper() {
    }

    static ReservaResponse aRespuesta(Reserva r) {
        return new ReservaResponse(r.getId(), r.getSesion().getId(), r.getSesion().getCodigo(),
                r.getPeriodo().getId(), r.getPeriodo().getCodigo(), r.getEquipo().getId(),
                r.getEquipo().getCodigo(), r.getOrigen().name());
    }
}
