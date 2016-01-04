/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.util.List;
import javax.persistence.TypedQuery;
import data.pojo.Word;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Stefan
 */
public class WordDAO extends AbstractDAO<Word>{
	
	private static WordDAO instance = null;
	
	private Map<String, Word> cache = null;
	
	public WordDAO() {
		
	}
	
	public static WordDAO getInstance() {
		if (instance == null) {
			instance = new WordDAO();
		}
		return instance;
	}
	
	public Word findByLabel(String label) {
		if (cache != null) return cache.get(label);
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

	public void loadAll() {
		if (cache != null) return;
		cache = findAll().stream().collect(
				Collectors.toConcurrentMap(Word::getLabel, Function.identity()));
	}
	
}
