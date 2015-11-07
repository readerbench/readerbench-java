package DAO.db;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * @author Stefan
 */
public class DAOService {
    
    private static DAOService instance = null;
    
    private final EntityManagerFactory emf;
	
    private DAOService() {
		emf = Persistence.createEntityManagerFactory("ReaderBench"); 
    }
        
    public static DAOService getInstance() {
        if (instance == null) instance = new DAOService();
        return instance;
    }
    
    public <T> T executeQuery(Function<EntityManager, T> f) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = f.apply(em);
            em.getTransaction().commit();
			em.close();
            return result;
        } catch (Exception ex) {
            em.getTransaction().rollback();
			em.close();
            return null;
        }
    }
    
    
    public void close() {
        emf.close();
    }
    
}
