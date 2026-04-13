package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "SCOOTER")
public class Scooter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double weight;
    private Double maxvelocity;
    private Integer battery;

    @ManyToOne
    @JoinColumn(name = "model", referencedColumnName = "number")
    private ScooterModel model;

    @Version
    private Timestamp version;

    @OneToMany(mappedBy = "scooter")
    private List<Travel> travels;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getMaxvelocity() {
        return maxvelocity;
    }

    public void setMaxvelocity(Double maxvelocity) {
        this.maxvelocity = maxvelocity;
    }

    public Integer getBattery() {
        return battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public ScooterModel getModel() {
        return model;
    }

    public void setModel(ScooterModel model) {
        this.model = model;
    }

    public Timestamp getVersion() {
        return version;
    }

    public void setVersion(Timestamp version) {
        this.version = version;
    }

    public List<Travel> getTravels() {
        return travels;
    }

    public void setTravels(List<Travel> travels) {
        this.travels = travels;
    }

}