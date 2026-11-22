package co.edu.unicartagena.turnos.repository;

import co.edu.unicartagena.turnos.model.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByCodigo(String codigo);

    List<Equipo> findByActivoTrueOrderByCodigoAsc();
}
