package isel.sisinf;

import isel.sisinf.jpa.ScooterRepository;
import isel.sisinf.model.Dock;
import isel.sisinf.model.Scooter;
import jakarta.persistence.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /**
     * Test for parksScooter method to handle OptimisticLockException.
     */
    public void testOptimisticLockingConflict() {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("cites-project");

        final CountDownLatch commitLatch = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            EntityManager em1 = emf.createEntityManager();
            try {
                em1.getTransaction().begin();
                Dock dock1 = em1.find(Dock.class, 9);
                em1.refresh(dock1);
                System.out.println("Thread 1: versão lida = " + dock1.getVersion());

                Scooter scooter6 = em1.find(Scooter.class, 6);
                dock1.setScooter(scooter6);
                dock1.setState("occupy");

                em1.flush();

                commitLatch.await();

                em1.getTransaction().commit();
                System.out.println("Thread 1 commit OK");
            } catch (OptimisticLockException ole) {
                System.out.println("Thread 1 apanhou OptimisticLockException: " + ole.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (em1.isOpen()) em1.close();
            }
        });

        Thread t2 = new Thread(() -> {
            EntityManager em2 = emf.createEntityManager();
            try {
                em2.getTransaction().begin();
                Dock dock2 = em2.find(Dock.class, 9);
                em2.refresh(dock2);

                Scooter scooter5 = em2.find(Scooter.class, 5);
                dock2.setScooter(scooter5);
                dock2.setState("occupy");

                em2.flush();

                commitLatch.await();

                em2.getTransaction().commit();
                System.out.println("Thread 2 commit OK");
            } catch (OptimisticLockException ole) {
                System.out.println("Thread 2 apanhou OptimisticLockException: " + ole.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (em2.isOpen()) em2.close();
            }
        });

        t1.start();
        t2.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        commitLatch.countDown();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        emf.close();
    }
}
