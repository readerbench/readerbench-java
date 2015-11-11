package DAO.db;

import java.util.List;
import javax.persistence.TypedQuery;
import pojo.SentimentValence;

/**
 *
 * @author Gabriel Gutu
 */
public class ValenceDAO extends AbstractDAO<SentimentValence>{
	
	private static ValenceDAO instance = null;
	
	private ValenceDAO() {
		
	}
	
	public static ValenceDAO getInstance() {
		if (instance == null) {
			instance = new ValenceDAO();
		}
		return instance;
	}
	
	public List<SentimentValence> findByRage(boolean rage) {
		return dao.executeQuery(em -> {
			TypedQuery<SentimentValence> query = em.createNamedQuery("SentimentValence.findByRage", SentimentValence.class);
			query.setParameter("rage", rage);
			return query.getResultList();
		});
	}
	
}
