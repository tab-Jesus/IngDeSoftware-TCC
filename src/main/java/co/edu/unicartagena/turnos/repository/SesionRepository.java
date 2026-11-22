package co.edu.unicartagena.turnos.repository;

import co.edu.unicartagena.turnos.model.Sesion;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByParticipantes_Id(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = "participantes")
    List<Sesion> findAll();
}
