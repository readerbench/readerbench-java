package dao;

import data.pojo.SentimentEntity;

/**
 *
 * @author Stefan
 */
public class SentimentEntityDAO extends AbstractDAO<SentimentEntity>{
	
	private static SentimentEntityDAO instance = null;
	
	private SentimentEntityDAO() {
		
	}
	
	public static SentimentEntityDAO getInstance() {
		if (instance == null) {
            instance = new SentimentEntityDAO();
		}
		return instance;
	}
	
}
