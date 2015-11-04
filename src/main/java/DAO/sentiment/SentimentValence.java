package DAO.sentiment;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.*;

import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;

/**
 * @author Gabriel Gutu
 *
 */
public enum SentimentValence {
	
	ANEW_VALENCE("ANEW Valence", false),
	ANEW_AROUSAL("ANEW Arousal", false),
	ANEW_DOMINANCE("ANEW Dominance", false),
	
	GI_ONE("GI One", false),
	GI_TWO("GI Two", false),
	
	STANFORD_NLP("Stanford NLP", false),
	
	RAGE_ONE("RAGE One", true)
	;
	
	private Integer id;
	private String name;
	private boolean rage;
	
	private SentimentValence(String name, boolean rage) {
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