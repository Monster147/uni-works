package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TRAVEL")
public class Travel {
    @Id
    private Timestamp dinitial;

    @ManyToOne
    @JoinColumn(name = "scooter")
    private Scooter scooter;

    private String comment;
    private Integer evaluation;
    private Timestamp dfinal;

    @ManyToOne
    @JoinColumn(name = "client")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "stinitial")
    private Station stinitial;

    @ManyToOne
    @JoinColumn(name = "stfinal")
    private Station stfinal;

    public Timestamp getDinitial() {
        return dinitial;
    }

    public void setDinitial(Timestamp dinitial) {
        this.dinitial = dinitial;
    }

    public Scooter getScooter() {
        return scooter;
    }

    public void setScooter(Scooter scooter) {
        this.scooter = scooter;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Integer evaluation) {
        this.evaluation = evaluation;
    }

    public Timestamp getDfinal() {
        return dfinal;
    }

    public void setDfinal(Timestamp dfinal) {
        this.dfinal = dfinal;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Station getStinitial() {
        return stinitial;
    }

    public void setStinitial(Station stinitial) {
        this.stinitial = stinitial;
    }

    public Station getStfinal() {
        return stfinal;
    }

    public void setStfinal(Station stfinal) {
        this.stfinal = stfinal;
    }
}