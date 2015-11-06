package DAO.sentiment;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.*;

import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;

/**
 * Holds a sentiment valence
 * 
 * @author Gabriel Gutu
 *
 */
public class SentimentValence {
	
	private Integer id;
	private String name;
	private boolean rage;
	
	/**
	 * Creates a sentiment valence
	 * 
	 * @param id
	 * 			id of the sentiment valence
	 * @param name
	 * 			name of the sentiment valence
	 * @param rage
	 * 			shows if it is a RAGE valence
	 * 			or a primary one
	 */
	private SentimentValence(Integer id, String name, boolean rage) {
		this.id = id;
		this.name = name;
		this.rage = rage;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getRage() {
		return rage;
	}
	
	public void setRage(boolean rage) {
		this.rage = rage;
	}
	
}	