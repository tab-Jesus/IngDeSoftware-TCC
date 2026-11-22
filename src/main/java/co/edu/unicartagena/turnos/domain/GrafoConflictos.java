package co.edu.unicartagena.turnos.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Grafo simple no dirigido G = (S, E): cada vértice es una sesión y existe una arista
 * entre dos sesiones distintas si comparten al menos un usuario.
 */
public final class GrafoConflictos {

    /** Arista con los usuarios que provocan el conflicto. */
    public record Arista(long origen, long destino, Set<Long> usuariosCompartidos) {
    }

    private final List<Long> vertices;
    private final Map<Long, Set<Long>> adyacencia = new TreeMap<>();
    private final List<Arista> aristas = new ArrayList<>();

    private GrafoConflictos(List<Long> vertices) {
        this.vertices = List.copyOf(vertices);
    }

    /** Construye el grafo con un índice invertido usuario → sesiones. */
    public static GrafoConflictos construir(ProblemaPlanificacion problema) {
        GrafoConflictos grafo = new GrafoConflictos(problema.sesiones());
        for (Long sesion : problema.sesiones()) {
            grafo.adyacencia.put(sesion, new TreeSet<>());
        }
        Map<Long, List<Long>> sesionesPorUsuario = new HashMap<>();
        for (Long sesion : problema.sesiones()) {
            for (Long usuario : problema.participantesDe(sesion)) {
                sesionesPorUsuario.computeIfAbsent(usuario, k -> new ArrayList<>()).add(sesion);
            }
        }
        Map<List<Long>, Set<Long>> compartidos = new TreeMap<>((a, b) -> {
            int c = Long.compare(a.get(0), b.get(0));
            return c != 0 ? c : Long.compare(a.get(1), b.get(1));
        });
        for (Map.Entry<Long, List<Long>> entrada : sesionesPorUsuario.entrySet()) {
            List<Long> lista = entrada.getValue();
            for (int i = 0; i < lista.size(); i++) {
                for (int j = i + 1; j < lista.size(); j++) {
                    long a = Math.min(lista.get(i), lista.get(j));
                    long b = Math.max(lista.get(i), lista.get(j));
                    compartidos.computeIfAbsent(List.of(a, b), k -> new TreeSet<>()).add(entrada.getKey());
                }
            }
        }
        for (Map.Entry<List<Long>, Set<Long>> e : compartidos.entrySet()) {
            long a = e.getKey().get(0);
            long b = e.getKey().get(1);
            grafo.adyacencia.get(a).add(b);
            grafo.adyacencia.get(b).add(a);
            grafo.aristas.add(new Arista(a, b, Collections.unmodifiableSet(e.getValue())));
        }
        return grafo;
    }

    public List<Long> vertices() {
        return vertices;
    }

    public List<Arista> aristas() {
        return Collections.unmodifiableList(aristas);
    }

    public Set<Long> vecinos(long sesion) {
        return Collections.unmodifiableSet(adyacencia.getOrDefault(sesion, Set.of()));
    }

    public int grado(long sesion) {
        return vecinos(sesion).size();
    }

    public boolean sonAdyacentes(long a, long b) {
        return vecinos(a).contains(b);
    }

    /** Verifica la coloración propia: ningún par de adyacentes comparte color. */
    public boolean esColoracionPropia(Map<Long, Long> color) {
        for (Arista arista : aristas) {
            Long ca = color.get(arista.origen());
            Long cb = color.get(arista.destino());
            if (ca != null && ca.equals(cb)) {
                return false;
            }
        }
        return true;
    }

    /** Cota inferior del número cromático: existencia de un triángulo (3) o de una arista (2). */
    public int cotaInferiorCromatica() {
        if (aristas.isEmpty()) {
            return vertices.isEmpty() ? 0 : 1;
        }
        for (Arista arista : aristas) {
            for (long tercero : vecinos(arista.origen())) {
                if (tercero != arista.destino() && sonAdyacentes(tercero, arista.destino())) {
                    return 3;
                }
            }
        }
        return 2;
    }
}
