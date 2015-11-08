/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO.db;

import java.util.List;
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
			query.setParameter("label", label);
			return query.getSingleResult();
		});
	}
    
    public List<Word> findByPrefix(String label) {
		return dao.executeQuery(em -> {
			TypedQuery<Word> query = em.createNamedQuery("Word.findByPrefix", Word.class);
            query.setParameter("label", label + "%");
			return query.getResultList();
		});
	}
	
}
