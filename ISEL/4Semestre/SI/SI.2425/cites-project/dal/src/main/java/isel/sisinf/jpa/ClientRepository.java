package isel.sisinf.jpa;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import isel.sisinf.model.*;

public class ClientRepository {
    private EntityManager em;

    public ClientRepository() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cites-project");
        em = emf.createEntityManager();
    }

    public void createCustomer(Person person, String cardType, Double initialCredit) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query q = em.createNativeQuery(
                    "INSERT INTO rider (name, email, taxnumber, dtregister, typeofcard, credit) " +
                            "VALUES (?, ?, ?, ?, ?, ?)"
            );
            q.setParameter(1, person.getName());
            q.setParameter(2, person.getEmail());
            q.setParameter(3, person.getTaxNumber());
            q.setParameter(4, new Timestamp(System.currentTimeMillis()));
            q.setParameter(5, cardType);
            q.setParameter(6, initialCredit);
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    public List<CustomerPassInfoDTO> getAllCustomersWithCardInfo() {
        return em.createQuery(
                        """
                    SELECT new isel.sisinf.model.CustomerPassInfoDTO(
                        p.id,
                        p.name,
                        p.email,
                        p.taxNumber,
                        c.dtregister,
                        cd.typeOfCard.reference,
                        cd.credit
                    )
                    FROM Person p
                    JOIN Client c ON c.person = p
                    JOIN Card cd ON cd.client = c
                    ORDER BY p.id
                    """,
                        CustomerPassInfoDTO.class)
                .getResultList();
    }

    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
