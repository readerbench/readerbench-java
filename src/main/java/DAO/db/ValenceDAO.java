package DAO.db;

import pojo.SentimentValence;

/**
 *
 * @author Stefan
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
	
}
