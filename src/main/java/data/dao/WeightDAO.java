/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.dao;

import javax.persistence.TypedQuery;

import data.pojo.Weight;

/**
 *
 * @author Gabriel Gutu
 */
public class WeightDAO extends AbstractDAO<Weight>{
	
	private static WeightDAO instance = null;
	
	public WeightDAO() {
		
	}
	
	public static WeightDAO getInstance() {
		if (instance == null) {
			instance = new WeightDAO();
		}
		return instance;
	}
	
	public Weight findByPair(Integer primaryValenceId, Integer rageValenceId) {
		return dao.executeQuery(em -> {
			TypedQuery<Weight> query = em.createNamedQuery("Weight.findByPair", Weight.class);
			query.setParameter("primaryValence", primaryValenceId);
			query.setParameter("rageValence", rageValenceId);
			return query.getSingleResult();
		});
	}
	
}
