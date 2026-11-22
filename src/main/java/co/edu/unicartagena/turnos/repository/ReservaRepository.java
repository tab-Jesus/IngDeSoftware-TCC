package co.edu.unicartagena.turnos.repository;

import co.edu.unicartagena.turnos.model.Reserva;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsBySesion_Id(Long sesionId);

    boolean existsByPeriodo_Id(Long periodoId);

    boolean existsByEquipo_Id(Long equipoId);

    Optional<Reserva> findBySesion_Id(Long sesionId);

    @Override
    @EntityGraph(attributePaths = {"sesion", "periodo", "equipo"})
    List<Reserva> findAll();
}
