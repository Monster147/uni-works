package isel.sisinf.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CARD")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double credit;

    @ManyToOne
    @JoinColumn(name = "typeofcard", referencedColumnName = "reference")
    private TypeOfCard typeOfCard;

    @ManyToOne
    @JoinColumn(name = "client", referencedColumnName = "person")
    private Client client;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public TypeOfCard getTypeOfCard() {
        return typeOfCard;
    }

    public void setTypeOfCard(TypeOfCard typeOfCard) {
        this.typeOfCard = typeOfCard;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}