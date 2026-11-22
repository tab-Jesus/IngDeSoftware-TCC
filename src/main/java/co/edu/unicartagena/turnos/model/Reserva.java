package co.edu.unicartagena.turnos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Reserva R(s, t, c). La restricción única (periodo, equipo) impide la doble ocupación de un
 * equipo y la restricción única sobre la sesión garantiza que cada sesión tenga una sola reserva.
 */
@Entity
@Table(name = "reserva", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reserva_periodo_equipo", columnNames = {"periodo_id", "equipo_id"}),
        @UniqueConstraint(name = "uk_reserva_sesion", columnNames = {"sesion_id"})
})
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private Sesion sesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private Periodo periodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrigenReserva origen;

    protected Reserva() {
    }

    public Reserva(Sesion sesion, Periodo periodo, Equipo equipo, OrigenReserva origen) {
        this.sesion = sesion;
        this.periodo = periodo;
        this.equipo = equipo;
        this.origen = origen;
    }

    public Long getId() {
        return id;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public OrigenReserva getOrigen() {
        return origen;
    }
}
