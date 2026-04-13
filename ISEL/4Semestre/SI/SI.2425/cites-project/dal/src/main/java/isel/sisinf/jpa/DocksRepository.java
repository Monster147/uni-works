package isel.sisinf.jpa;

import isel.sisinf.model.DockOccupancyDTO;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class DocksRepository {
    private EntityManager em;

    public DocksRepository() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cites-project");
        em = emf.createEntityManager();
    }

    public List<DockOccupancyDTO> getAllDocksWithOccupancy() {
        List<Object[]> resultList = em.createNativeQuery(
                """
                SELECT d.number, s.id, s.latitude, s.longitude, d.state,
                       COALESCE(d.scooter::text, 'None'), fx_dock_occupancy(s.id)
                FROM DOCK d
                JOIN STATION s ON d.station = s.id
                ORDER BY s.id, d.number
                """
        ).getResultList();

        List<DockOccupancyDTO> docks = new ArrayList<>();
        for (Object[] row : resultList) {
            docks.add(new DockOccupancyDTO(
                    ((Number) row[0]).intValue(),
                    ((Number) row[1]).intValue(),
                    ((Number) row[2]).doubleValue(),
                    ((Number) row[3]).doubleValue(),
                    (String) row[4],
                    (String) row[5],
                    ((Number) row[6]).doubleValue()
            ));
        }

        return docks;
    }

    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
