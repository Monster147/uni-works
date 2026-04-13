package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "REPLACEMENT")
public class Replacement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "number")
    private Integer id;

    private Timestamp dreplacement;
    private String action;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "reporder", referencedColumnName = "dorder"),
            @JoinColumn(name = "repstation", referencedColumnName = "station")
    })
    private ReplacementOrder reporder;

    @ManyToOne
    @JoinColumn(name = "employee")
    private Employee employee;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getDreplacement() {
        return dreplacement;
    }

    public void setDreplacement(Timestamp dreplacement) {
        this.dreplacement = dreplacement;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ReplacementOrder getReporder() {
        return reporder;
    }

    public void setReporder(ReplacementOrder reporder) {
        this.reporder = reporder;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}