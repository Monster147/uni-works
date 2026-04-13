package isel.sisinf.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TOPUP")
@IdClass(TopUpId.class)
public class TopUp {
    @Id
    private Timestamp dttopup;

    @Id
    @ManyToOne
    @JoinColumn(name = "card")
    private Card card;

    private Double value;

    public Timestamp getDttopup() {
        return dttopup;
    }

    public void setDttopup(Timestamp dttopup) {
        this.dttopup = dttopup;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}