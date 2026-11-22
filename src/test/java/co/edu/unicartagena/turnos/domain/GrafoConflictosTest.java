package co.edu.unicartagena.turnos.domain;

import static co.edu.unicartagena.turnos.domain.Instancias.A;
import static co.edu.unicartagena.turnos.domain.Instancias.B;
import static co.edu.unicartagena.turnos.domain.Instancias.C;
import static co.edu.unicartagena.turnos.domain.Instancias.D;
import static co.edu.unicartagena.turnos.domain.Instancias.E;
import static co.edu.unicartagena.turnos.domain.Instancias.F;
import static co.edu.unicartagena.turnos.domain.Instancias.G;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrafoConflictosTest {

    private GrafoConflictos grafo;

    @BeforeEach
    void preparar() {
        grafo = GrafoConflictos.construir(Instancias.base());
    }

    @Test
    @DisplayName("La instancia base tiene diez aristas de conflicto")
    void tieneDiezAristas() {
        assertEquals(10, grafo.aristas().size());
    }

    @Test
    @DisplayName("Grados esperados y lema del apretón de manos")
    void gradosYApretonDeManos() {
        assertEquals(3, grafo.grado(A));
        assertEquals(3, grafo.grado(B));
        assertEquals(4, grafo.grado(C));
        assertEquals(3, grafo.grado(D));
        assertEquals(2, grafo.grado(E));
        assertEquals(2, grafo.grado(F));
        assertEquals(3, grafo.grado(G));
        int suma = grafo.vertices().stream().mapToInt(v -> grafo.grado(v)).sum();
        assertEquals(2 * grafo.aristas().size(), suma);
    }

    @Test
    @DisplayName("La relación de conflicto es simétrica y sin lazos")
    void simetricaSinLazos() {
        for (Long v : grafo.vertices()) {
            assertFalse(grafo.sonAdyacentes(v, v));
            for (Long w : grafo.vecinos(v)) {
                assertTrue(grafo.sonAdyacentes(w, v));
            }
        }
    }

    @Test
    @DisplayName("La arista A-B existe por el usuario compartido u2")
    void aristaConUsuarioCompartido() {
        GrafoConflictos.Arista ab = grafo.aristas().stream()
                .filter(a -> a.origen() == A && a.destino() == B).findFirst().orElseThrow();
        assertEquals(Set.of(2L), ab.usuariosCompartidos());
    }

    @Test
    @DisplayName("El triángulo A-B-C impone una cota inferior de tres colores")
    void cotaCromaticaPorTriangulo() {
        assertTrue(grafo.sonAdyacentes(A, B) && grafo.sonAdyacentes(B, C) && grafo.sonAdyacentes(A, C));
        assertEquals(3, grafo.cotaInferiorCromatica());
    }

    @Test
    @DisplayName("La coloración del TCC es propia y una coloración con A y B iguales no lo es")
    void coloracionPropia() {
        Map<Long, Long> color = new HashMap<>();
        color.put(A, 1L);
        color.put(B, 2L);
        color.put(C, 3L);
        color.put(D, 1L);
        color.put(E, 2L);
        color.put(F, 1L);
        color.put(G, 2L);
        assertTrue(grafo.esColoracionPropia(color));
        color.put(B, 1L);
        assertFalse(grafo.esColoracionPropia(color));
    }

    @Test
    @DisplayName("Sesiones sin usuarios compartidos no tienen aristas")
    void sinConflictos() {
        ProblemaPlanificacion p = Instancias.construir(
                Map.of(1L, Set.of(1L), 2L, Set.of(2L)), List.of(1L), List.of(1L, 2L), Map.of());
        GrafoConflictos g = GrafoConflictos.construir(p);
        assertEquals(0, g.aristas().size());
        assertEquals(1, g.cotaInferiorCromatica());
    }
}
