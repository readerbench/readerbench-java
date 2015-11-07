/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO.db;

import javax.persistence.TypedQuery;
import pojo.Word;

/**
 *
 * @author Stefan
 */
public class WordDAO extends AbstractDAO<Word>{
	
	private static WordDAO instance = null;
	
	private WordDAO() {
		
	}
	
	public static WordDAO getInstance() {
		if (instance == null) {
			instance = new WordDAO();
		}
		return instance;
	}
	
	public Word findByLabel(String label) {
		return dao.executeQuery(em -> {
			TypedQuery<Word> query = em.createNamedQuery("Word.findByLabel", Word.class);
			query.setParameter(1, label);
			return query.getSingleResult();
		});
	}
	
}
