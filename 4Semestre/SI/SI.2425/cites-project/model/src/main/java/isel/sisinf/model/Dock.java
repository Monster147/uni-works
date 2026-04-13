package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "DOCK")
public class Dock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "number")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "station")
    private Station station;

    private String state;

    @OneToOne
    @JoinColumn(name = "scooter")
    private Scooter scooter;

    @Version
    private Timestamp version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Scooter getScooter() {
        return scooter;
    }

    public void setScooter(Scooter scooter) {
        this.scooter = scooter;
    }

    public Timestamp getVersion() {
        return version;
    }

    public void setVersion(Timestamp version) {
        this.version = version;
    }
}