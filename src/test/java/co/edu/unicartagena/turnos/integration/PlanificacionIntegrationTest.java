package co.edu.unicartagena.turnos.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unicartagena.turnos.model.Equipo;
import co.edu.unicartagena.turnos.model.Periodo;
import co.edu.unicartagena.turnos.model.Sesion;
import co.edu.unicartagena.turnos.repository.EquipoRepository;
import co.edu.unicartagena.turnos.repository.PeriodoRepository;
import co.edu.unicartagena.turnos.repository.SesionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Prueba de integración de extremo a extremo con H2 y la instancia de ejemplo del TCC. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlanificacionIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SesionRepository sesiones;
    @Autowired
    private PeriodoRepository periodos;
    @Autowired
    private EquipoRepository equipos;

    private long idSesion(String codigo) {
        return sesiones.findAll().stream().filter(s -> s.getCodigo().equals(codigo))
                .map(Sesion::getId).findFirst().orElseThrow();
    }

    private long idPeriodo(String codigo) {
        return periodos.findAll().stream().filter(p -> p.getCodigo().equals(codigo))
                .map(Periodo::getId).findFirst().orElseThrow();
    }

    private long idEquipo(String codigo) {
        return equipos.findAll().stream().filter(e -> e.getCodigo().equals(codigo))
                .map(Equipo::getId).findFirst().orElseThrow();
    }

    private String cuerpoReserva(String sesion, String periodo, String equipo) {
        return "{\"sesionId\":" + idSesion(sesion) + ",\"periodoId\":" + idPeriodo(periodo)
                + ",\"equipoId\":" + idEquipo(equipo) + "}";
    }

    @Test
    @DisplayName("Los datos de ejemplo cargan siete sesiones")
    void datosDeEjemplo() throws Exception {
        mvc.perform(get("/api/v1/sesiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
        assertEquals(7, sesiones.count());
    }

    @Test
    @DisplayName("Generar la agenda usa tres períodos y la deja consistente")
    void generarYValidar() throws Exception {
        mvc.perform(post("/api/v1/planificacion/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.factible").value(true))
                .andExpect(jsonPath("$.periodosUsados").value(3))
                .andExpect(jsonPath("$.reservas.length()").value(7));

        mvc.perform(get("/api/v1/planificacion/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("El grafo de conflictos tiene diez aristas y cota cromática tres")
    void grafo() throws Exception {
        mvc.perform(get("/api/v1/planificacion/grafo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodos.length()").value(7))
                .andExpect(jsonPath("$.aristas.length()").value(10))
                .andExpect(jsonPath("$.cotaInferiorCromatica").value(3));
    }

    @Test
    @DisplayName("Sin reservas la validación reporta las siete sesiones pendientes")
    void validarSinReservas() throws Exception {
        mvc.perform(get("/api/v1/planificacion/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].tipo").value("SESION_SIN_RESERVA"));
    }

    @Test
    @DisplayName("Una reserva manual que choca por usuario compartido responde 422")
    void reservaManualConConflicto() throws Exception {
        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoReserva("A", "t1", "c1")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoReserva("B", "t1", "c2")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalles[0]").exists());
    }

    @Test
    @DisplayName("Reservar dos veces la misma sesión responde 409")
    void reservaDuplicada() throws Exception {
        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoReserva("A", "t1", "c1")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoReserva("A", "t2", "c2")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Crear una sesión sin participantes responde 400")
    void sesionSinParticipantes() throws Exception {
        mvc.perform(post("/api/v1/sesiones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"Z\",\"titulo\":\"Vacía\",\"participanteIds\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
