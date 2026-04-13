package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "REPLACEMENTORDER")
@IdClass(ReplacementOrderId.class)
public class ReplacementOrder {
    @Id
    private Timestamp dorder;

    @Id
    @ManyToOne
    @JoinColumn(name = "station")
    private Station station;

    private Timestamp dreplacement;
    private Integer roccupation;

    public Timestamp getDorder() {
        return dorder;
    }

    public void setDorder(Timestamp dorder) {
        this.dorder = dorder;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Timestamp getDreplacement() {
        return dreplacement;
    }

    public void setDreplacement(Timestamp dreplacement) {
        this.dreplacement = dreplacement;
    }

    public Integer getRoccupation() {
        return roccupation;
    }

    public void setRoccupation(Integer roccupation) {
        this.roccupation = roccupation;
    }
}