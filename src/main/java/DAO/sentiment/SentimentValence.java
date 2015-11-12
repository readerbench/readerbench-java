package DAO.sentiment;

import DAO.db.ValenceDAO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	
	private static final Map<String, SentimentValence> valenceMap;
	static {
		Map<String, SentimentValence> valenceMaplocal = new HashMap<>();
		List<pojo.SentimentValence> valences = ValenceDAO.getInstance().findAll();
		for (pojo.SentimentValence v : valences) {
			SentimentValence sv = new SentimentValence(v.getId(), v.getLabel(), v.getIndexLabel(), v.getRage());
			valenceMaplocal.put(v.getIndexLabel(), sv);
		}
		valenceMap = Collections.unmodifiableMap(valenceMaplocal);
	}
	
	private Integer id;
	private String name;
	private String indexLabel;
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
	public SentimentValence(Integer id, String name, String index, boolean rage) {
		this.id = id;
		this.name = name;
		this.indexLabel = index;
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
	
	public String getIndexLabel() {
		return indexLabel;
	}
	
	public void setIndexLabel(String index) {
		this.indexLabel = index;
	}
	
	public boolean getRage() {
		return rage;
	}
	
	public void setRage(boolean rage) {
		this.rage = rage;
	}
	
	public static Map<String, SentimentValence> getValenceMap() {
		return valenceMap;
	}
	
	public static SentimentValence get(String index) {
		return valenceMap.get(index);
	}
	
}	