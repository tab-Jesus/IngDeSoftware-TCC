package co.edu.unicartagena.turnos.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Solucionador exacto. Minimiza la cantidad de períodos distintos utilizados:
 * examina subconjuntos de períodos de tamaño creciente y, para cada uno, ejecuta una
 * búsqueda con retroceso (mínimo dominio restante, desempate por grado en el grafo
 * de conflictos). La primera solución encontrada es óptima respecto a ese objetivo.
 */
public final class PlanificadorColoracion {

    /** Presupuesto por defecto de asignaciones tentativas antes de renunciar a probar optimalidad. */
    public static final long LIMITE_NODOS_POR_DEFECTO = 200_000L;

    private final long limiteNodos;

    public PlanificadorColoracion() {
        this(LIMITE_NODOS_POR_DEFECTO);
    }

    public PlanificadorColoracion(long limiteNodos) {
        this.limiteNodos = limiteNodos;
    }

    /** Estado mutable de una ejecución: contador de nodos y bandera de presupuesto agotado. */
    private static final class Estado {
        private final long limite;
        private long nodos;
        private boolean agotado;

        private Estado(long limite) {
            this.limite = limite;
        }
    }

    public ResultadoPlanificacion resolver(ProblemaPlanificacion problema) {
        List<String> mensajes = new ArrayList<>();
        if (problema.sesiones().isEmpty()) {
            return new ResultadoPlanificacion(true, Map.of(), 0, 0, true, List.of("No hay sesiones por programar."));
        }
        GrafoConflictos grafo = GrafoConflictos.construir(problema);

        Map<Long, List<Posicion>> dominios = construirDominios(problema, mensajes);
        if (!mensajes.isEmpty()) {
            return infactible(0, mensajes);
        }

        int cota = cotaInferior(problema, grafo);
        Estado estado = new Estado(limiteNodos);

        // 1) Cota superior: una agenda factible cualquiera usando todos los períodos.
        Map<Long, Posicion> mejor = buscar(problema, grafo, dominios, estado);
        if (mejor == null) {
            if (estado.agotado) {
                mensajes.add("Se agotó el presupuesto de búsqueda sin hallar una agenda; no se puede afirmar "
                        + "que el problema sea infactible.");
            } else {
                mensajes.add("No existe una agenda que respete usuarios, disponibilidad y equipos con los recursos "
                        + "actuales. Amplíe la disponibilidad de los usuarios, agregue períodos o equipos, o "
                        + "modifique los participantes.");
            }
            return infactible(estado.nodos, mensajes);
        }

        // 2) Mejora: se prueban subconjuntos de períodos de tamaño creciente hasta la cota superior.
        int usados = contarPeriodos(mejor);
        for (int k = cota; k < usados && !estado.agotado; k++) {
            List<List<Long>> subconjuntos = new ArrayList<>();
            combinaciones(problema.periodos(), k, 0, new ArrayList<>(), subconjuntos);
            for (List<Long> subconjunto : subconjuntos) {
                Map<Long, List<Posicion>> restringidos = restringir(dominios, new HashSet<>(subconjunto));
                if (restringidos == null) {
                    continue;
                }
                Map<Long, Posicion> solucion = buscar(problema, grafo, restringidos, estado);
                if (solucion != null) {
                    return exito(problema, solucion, estado, true);
                }
                if (estado.agotado) {
                    break;
                }
            }
        }
        return exito(problema, mejor, estado, !estado.agotado);
    }

    private ResultadoPlanificacion exito(ProblemaPlanificacion problema, Map<Long, Posicion> solucion,
                                         Estado estado, boolean optimo) {
        int periodos = contarPeriodos(solucion);
        String mensaje = optimo
                ? "Agenda óptima generada con " + periodos + " período(s)."
                : "Agenda válida con " + periodos + " período(s); se agotó el presupuesto de búsqueda y no se "
                        + "garantiza que sea el mínimo.";
        return new ResultadoPlanificacion(true, ordenar(problema, solucion), periodos, estado.nodos, optimo,
                List.of(mensaje));
    }

    private ResultadoPlanificacion infactible(long nodos, List<String> mensajes) {
        return new ResultadoPlanificacion(false, Map.of(), 0, nodos, false, List.copyOf(mensajes));
    }

    /** Dominio de cada sesión: pares (t, c) con participantes disponibles y equipo compatible. */
    private Map<Long, List<Posicion>> construirDominios(ProblemaPlanificacion problema, List<String> mensajes) {
        Map<Long, List<Posicion>> dominios = new LinkedHashMap<>();
        for (Long sesion : problema.sesiones()) {
            List<Posicion> dominio = new ArrayList<>();
            for (Long periodo : problema.periodos()) {
                if (!problema.periodoAdmisible(sesion, periodo)) {
                    continue;
                }
                for (Long equipo : problema.equipos()) {
                    if (problema.equipoCompatible(sesion, equipo)) {
                        dominio.add(new Posicion(periodo, equipo));
                    }
                }
            }
            if (dominio.isEmpty()) {
                mensajes.add("La sesión " + sesion + " no tiene ningún período y equipo admisibles.");
            }
            dominios.put(sesion, dominio);
        }
        return dominios;
    }

    /** Cota inferior: capacidad, mayor carga de un usuario y número cromático mínimo. */
    private int cotaInferior(ProblemaPlanificacion problema, GrafoConflictos grafo) {
        int equipos = Math.max(1, problema.equipos().size());
        int porCapacidad = (problema.sesiones().size() + equipos - 1) / equipos;
        Map<Long, Integer> carga = new HashMap<>();
        int mayorCarga = 0;
        for (Long sesion : problema.sesiones()) {
            for (Long usuario : problema.participantesDe(sesion)) {
                int valor = carga.merge(usuario, 1, Integer::sum);
                mayorCarga = Math.max(mayorCarga, valor);
            }
        }
        return Math.max(1, Math.max(porCapacidad, Math.max(mayorCarga, grafo.cotaInferiorCromatica())));
    }

    private Map<Long, List<Posicion>> restringir(Map<Long, List<Posicion>> dominios, Set<Long> permitidos) {
        Map<Long, List<Posicion>> resultado = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Posicion>> entrada : dominios.entrySet()) {
            List<Posicion> filtrado = new ArrayList<>();
            for (Posicion posicion : entrada.getValue()) {
                if (permitidos.contains(posicion.periodoId())) {
                    filtrado.add(posicion);
                }
            }
            if (filtrado.isEmpty()) {
                return null;
            }
            resultado.put(entrada.getKey(), filtrado);
        }
        return resultado;
    }

    private Map<Long, Posicion> buscar(ProblemaPlanificacion problema, GrafoConflictos grafo,
                                       Map<Long, List<Posicion>> dominios, Estado estado) {
        Map<Long, Posicion> asignadas = new HashMap<>();
        Map<Long, Set<Long>> periodosPorUsuario = new HashMap<>();
        Set<Posicion> ocupadas = new HashSet<>();
        boolean encontrada = retroceso(problema, grafo, dominios, asignadas, periodosPorUsuario, ocupadas, estado);
        return encontrada ? asignadas : null;
    }

    private boolean retroceso(ProblemaPlanificacion problema, GrafoConflictos grafo,
                              Map<Long, List<Posicion>> dominios, Map<Long, Posicion> asignadas,
                              Map<Long, Set<Long>> periodosPorUsuario, Set<Posicion> ocupadas, Estado estado) {
        if (asignadas.size() == dominios.size()) {
            return true;
        }
        Long elegida = null;
        List<Posicion> mejoresOpciones = null;
        for (Long sesion : dominios.keySet()) {
            if (asignadas.containsKey(sesion)) {
                continue;
            }
            List<Posicion> opciones = opcionesLegales(problema, dominios.get(sesion), sesion,
                    periodosPorUsuario, ocupadas);
            if (opciones.isEmpty()) {
                return false;
            }
            boolean mejor = mejoresOpciones == null
                    || opciones.size() < mejoresOpciones.size()
                    || (opciones.size() == mejoresOpciones.size() && grafo.grado(sesion) > grafo.grado(elegida));
            if (mejor) {
                elegida = sesion;
                mejoresOpciones = opciones;
            }
        }
        for (Posicion posicion : mejoresOpciones) {
            if (estado.nodos >= estado.limite) {
                estado.agotado = true;
                return false;
            }
            estado.nodos++;
            asignar(problema, elegida, posicion, asignadas, periodosPorUsuario, ocupadas);
            if (retroceso(problema, grafo, dominios, asignadas, periodosPorUsuario, ocupadas, estado)) {
                return true;
            }
            revertir(problema, elegida, posicion, asignadas, periodosPorUsuario, ocupadas);
        }
        return false;
    }

    private List<Posicion> opcionesLegales(ProblemaPlanificacion problema, List<Posicion> dominio, Long sesion,
                                           Map<Long, Set<Long>> periodosPorUsuario, Set<Posicion> ocupadas) {
        List<Posicion> legales = new ArrayList<>();
        for (Posicion posicion : dominio) {
            if (ocupadas.contains(posicion)) {
                continue;
            }
            boolean libre = true;
            for (Long usuario : problema.participantesDe(sesion)) {
                if (periodosPorUsuario.getOrDefault(usuario, Set.of()).contains(posicion.periodoId())) {
                    libre = false;
                    break;
                }
            }
            if (libre) {
                legales.add(posicion);
            }
        }
        return legales;
    }

    private void asignar(ProblemaPlanificacion problema, Long sesion, Posicion posicion,
                         Map<Long, Posicion> asignadas, Map<Long, Set<Long>> periodosPorUsuario,
                         Set<Posicion> ocupadas) {
        asignadas.put(sesion, posicion);
        ocupadas.add(posicion);
        for (Long usuario : problema.participantesDe(sesion)) {
            periodosPorUsuario.computeIfAbsent(usuario, k -> new HashSet<>()).add(posicion.periodoId());
        }
    }

    private void revertir(ProblemaPlanificacion problema, Long sesion, Posicion posicion,
                          Map<Long, Posicion> asignadas, Map<Long, Set<Long>> periodosPorUsuario,
                          Set<Posicion> ocupadas) {
        asignadas.remove(sesion);
        ocupadas.remove(posicion);
        for (Long usuario : problema.participantesDe(sesion)) {
            periodosPorUsuario.get(usuario).remove(posicion.periodoId());
        }
    }

    private void combinaciones(List<Long> elementos, int k, int inicio, List<Long> actual,
                               List<List<Long>> salida) {
        if (actual.size() == k) {
            salida.add(new ArrayList<>(actual));
            return;
        }
        for (int i = inicio; i < elementos.size(); i++) {
            actual.add(elementos.get(i));
            combinaciones(elementos, k, i + 1, actual, salida);
            actual.remove(actual.size() - 1);
        }
    }

    private int contarPeriodos(Map<Long, Posicion> solucion) {
        Set<Long> periodos = new HashSet<>();
        for (Posicion posicion : solucion.values()) {
            periodos.add(posicion.periodoId());
        }
        return periodos.size();
    }

    private Map<Long, Posicion> ordenar(ProblemaPlanificacion problema, Map<Long, Posicion> solucion) {
        Map<Long, Posicion> ordenada = new LinkedHashMap<>();
        for (Long sesion : problema.sesiones()) {
            ordenada.put(sesion, solucion.get(sesion));
        }
        return ordenada;
    }
}
