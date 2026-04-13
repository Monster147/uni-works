package isel.sisinf.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.StoredProcedureQuery;
import java.util.List;

public class TripRepository {
    private EntityManager em;

    public TripRepository() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cites-project");
        em = emf.createEntityManager();
    }

    public void startNewTrip(int dockId, int clientId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            StoredProcedureQuery query = em.createStoredProcedureQuery("startTrip")
                    .registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN)
                    .setParameter(1, dockId)
                    .setParameter(2, clientId);

            query.execute();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Failed to start trip: " + e.getMessage(), e);
        }
    }

    public void endTrip(int scooterId, String comment, Integer evaluation){
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            StoredProcedureQuery query = em.createStoredProcedureQuery("endTrip")
                    .registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(3, Integer.class, ParameterMode.IN)
                    .setParameter(1, scooterId)
                    .setParameter(2, comment)
                    .setParameter(3, evaluation);

            query.execute();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Failed to end trip: " + e.getMessage(), e);
        }
    }

    public boolean checkUnfinishedTrip(int scooterId) {
        List<?> result = em.createNativeQuery(
        "SELECT 1 FROM TRAVEL WHERE scooter = ?1 AND dfinal IS NULL"
        )
        .setParameter(1, scooterId)
        .getResultList();

    return !result.isEmpty();
    }

    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
