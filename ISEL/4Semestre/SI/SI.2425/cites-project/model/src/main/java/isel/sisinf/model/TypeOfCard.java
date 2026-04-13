package isel.sisinf.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "TYPEOFCARD")
public class TypeOfCard {
    @Id
    private String reference;

    private Integer nodays;
    private Double price;

    @OneToMany(mappedBy = "typeOfCard")
    private List<Card> cards;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getNodays() {
        return nodays;
    }

    public void setNodays(Integer nodays) {
        this.nodays = nodays;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}