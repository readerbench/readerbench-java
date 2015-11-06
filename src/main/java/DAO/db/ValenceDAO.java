/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
