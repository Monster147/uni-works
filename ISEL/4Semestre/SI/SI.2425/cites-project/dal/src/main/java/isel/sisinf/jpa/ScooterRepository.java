// ScooterRepository.java
package isel.sisinf.jpa;

import jakarta.persistence.*;
import java.sql.Timestamp;
import isel.sisinf.model.*;

public class ScooterRepository {
    private EntityManager em;

    public ScooterRepository() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cites-project");
        em = emf.createEntityManager();
    }

    public void parksScooter(int scooterId, int dockId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Dock dock = em.find(Dock.class, dockId, LockModeType.OPTIMISTIC);
            Scooter scooter = em.find(Scooter.class, scooterId, LockModeType.OPTIMISTIC);

            if (dock == null) {
                throw new RuntimeException("Invalid dock selection");
            }
            if (scooter == null) {
                throw new RuntimeException("Invalid scooter ID");
            }

            Long dockCount = em.createQuery(
                    "SELECT COUNT(d) FROM Dock d WHERE d.scooter = :scooter", Long.class)
                    .setParameter("scooter", scooter)
                    .getSingleResult();

            if (dockCount > 0) {
                throw new RuntimeException("Scooter is already docked elsewhere");
            }

            if (!"free".equals(dock.getState())) {
                throw new RuntimeException("Dock is no longer available");
            }

            dock.setState("occupy");
            dock.setScooter(scooter);
            em.merge(dock);

            scooter.setBattery(scooter.getBattery());
            em.merge(scooter);

            tx.commit();
        } catch (OptimisticLockException e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Conflict detected. Please refresh and try again.");
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
