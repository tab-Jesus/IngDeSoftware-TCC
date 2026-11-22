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

/** Solicitud de atención que reúne a uno o más usuarios en un equipo durante un período. */
@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(length = 150)
    private String titulo;

    /** Relación de participación P ⊆ U × S. */
    @ManyToMany
    @JoinTable(name = "participacion",
            joinColumns = @JoinColumn(name = "sesion_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> participantes = new HashSet<>();

    protected Sesion() {
    }

    public Sesion(String codigo, String titulo, Set<Usuario> participantes) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.participantes = participantes;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Set<Usuario> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Set<Usuario> participantes) {
        this.participantes = participantes;
    }
}
