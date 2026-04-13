package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "CLIENT")
public class Client {
    @Id
    @OneToOne
    @JoinColumn(name = "person", referencedColumnName = "id")
    private Person person;

    private Timestamp dtregister;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Timestamp getDtregister() {
        return dtregister;
    }

    public void setDtregister(Timestamp dtregister) {
        this.dtregister = dtregister;
    }

}
