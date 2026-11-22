package co.edu.unicartagena.turnos.dto;

import java.util.List;

public record GrafoResponse(List<String> nodos, List<Arista> aristas, int cotaInferiorCromatica) {

    public record Arista(String origen, String destino, List<String> usuariosCompartidos) {
    }
}
