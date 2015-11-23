package data.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

/**
 *
 * @author Stefan
 */
public class DAOService {
    
    private static DAOService instance = null;
    
    private final EntityManagerFactory emf;
    private EntityManager em;
	
    private DAOService() {
        Properties p = null;
        try {
            p = new Properties();
            p.load(new FileInputStream("db.properties"));
        } catch (IOException ex) {
            Logger.getLogger(DAOService.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Database properties not set!");
        }
    	emf = Persistence.createEntityManagerFactory("ReaderBench", p); 
        em = emf.createEntityManager();
    }
        
    public static DAOService getInstance() {
        if (instance == null) instance = new DAOService();
        return instance;
    }
    
    public <T> T executeQuery(Function<EntityManager, T> f) {
        try {
            em.getTransaction().begin();
            T result = f.apply(em);
            em.getTransaction().commit();
			return result;
        } 
        catch (NoResultException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
			return null;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
			return null;
        }
    }
    
    
    public void close() {
        if (em != null) em.close();
        emf.close();
    }
    
}
