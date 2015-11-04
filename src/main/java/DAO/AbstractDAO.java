package DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.poi.hslf.record.RecordTypes;

/**
 *
 * @author Stefan
 */
public class AbstractDAO {
    
    private static AbstractDAO instance = null;
    
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReaderBench"); 
    
    private AbstractDAO() {
    }
        
    public static AbstractDAO getInstance() {
        if (instance == null) instance = new AbstractDAO();
        return instance;
    }
    
    public <T> T executeQuery(Function<EntityManager, T> f) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = f.apply(em);
            em.close();
            return result;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            return null;
        }
    }
    
    public <T> List<T> findAll(Class<T> type) {
        return executeQuery(em -> {
            List<T> result = new ArrayList<>();
            TypedQuery<T> query = em.createNamedQuery(type.getSimpleName() + ".findAll", type);
            try {
                result = query.getResultList();
            }
            catch (Exception ex) {
                
            }
            return result;
        });
    }
    
    public <T> T findById(Class<T> type, int id) {
        return executeQuery(em -> {
            return em.find(type, id);
        });
    }
    
    public void close() {
        emf.close();
    }
    
}
