package co.edu.unicartagena.turnos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unicartagena.turnos.dto.ReservaRequest;
import co.edu.unicartagena.turnos.dto.ReservaResponse;
import co.edu.unicartagena.turnos.dto.ViolacionResponse;
import co.edu.unicartagena.turnos.exception.ReglaNegocioException;
import co.edu.unicartagena.turnos.exception.RecursoNoEncontradoException;
import co.edu.unicartagena.turnos.service.ReservaService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReservaService servicio;

    @Test
    @DisplayName("POST /reservas con datos válidos responde 201")
    void crearValida() throws Exception {
        when(servicio.crear(any(ReservaRequest.class)))
                .thenReturn(new ReservaResponse(1L, 1L, "A", 1L, "t1", 1L, "c1", "MANUAL"));

        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sesionId\":1,\"periodoId\":1,\"equipoId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sesionCodigo").value("A"))
                .andExpect(jsonPath("$.origen").value("MANUAL"));
    }

    @Test
    @DisplayName("POST /reservas sin campos obligatorios responde 400 con detalle")
    void crearInvalida() throws Exception {
        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(3));
    }

    @Test
    @DisplayName("Una violación de reglas de negocio responde 422")
    void reglaDeNegocio() throws Exception {
        when(servicio.crear(any(ReservaRequest.class))).thenThrow(new ReglaNegocioException("Reserva inválida.",
                List.of(new ViolacionResponse("USUARIO_DOBLE_ACTIVIDAD", "El usuario 11 está duplicado."))));

        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sesionId\":2,\"periodoId\":1,\"equipoId\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalles[0]").value("USUARIO_DOBLE_ACTIVIDAD: El usuario 11 está duplicado."));
    }

    @Test
    @DisplayName("GET de una reserva inexistente responde 404")
    void noEncontrada() throws Exception {
        when(servicio.obtener(99L)).thenThrow(new RecursoNoEncontradoException("Reserva", 99L));

        mvc.perform(get("/api/v1/reservas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
