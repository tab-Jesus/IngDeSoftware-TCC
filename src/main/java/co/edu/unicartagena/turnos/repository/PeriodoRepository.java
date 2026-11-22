package co.edu.unicartagena.turnos.repository;

import co.edu.unicartagena.turnos.model.Periodo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodoRepository extends JpaRepository<Periodo, Long> {

    boolean existsByCodigo(String codigo);

    List<Periodo> findAllByOrderByFechaAscHoraInicioAsc();
}
