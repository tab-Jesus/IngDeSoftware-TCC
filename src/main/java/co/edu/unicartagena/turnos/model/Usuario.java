package co.edu.unicartagena.turnos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/** Persona que solicita atención. Se identifica por un código simbólico (sin datos personales sensibles). */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Relación D ⊆ U × T. */
    @ManyToMany
    @JoinTable(name = "disponibilidad",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "periodo_id"))
    private Set<Periodo> periodosDisponibles = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Periodo> getPeriodosDisponibles() {
        return periodosDisponibles;
    }

    public void setPeriodosDisponibles(Set<Periodo> periodosDisponibles) {
        this.periodosDisponibles = periodosDisponibles;
    }
}
