package co.edu.unicartagena.turnos.repository;

import co.edu.unicartagena.turnos.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = "periodosDisponibles")
    List<Usuario> findAll();
}
