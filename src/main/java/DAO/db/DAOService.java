package DAO.db;

import java.util.function.Function;
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
		emf = Persistence.createEntityManagerFactory("ReaderBench"); 
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
