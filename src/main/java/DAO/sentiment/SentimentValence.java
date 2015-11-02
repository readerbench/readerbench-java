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
	
	ANEW_VALENCE("ANEW Valence", null),
	ANEW_AROUSAL("ANEW Arousal", null),
	ANEW_DOMINANCE("ANEW Dominance", null),
	
	GI_ONE("GI One", null),
	GI_TWO("GI Two", null),
	
	STANFORD_NLP("Stanford NLP", null)
	;
	
	private String name;
	private Double weight;
	private boolean isRageValence;
	
	private Map<Integer, Double> rageWeights;
	
	private SentimentValence(String name) {
		this(name, 1.0, false);
	}
	
	private SentimentValence(String name, Double weight) {
		this(name, weight, false);
	}
	
	private SentimentValence(String name, Double weight, boolean isRageValence) {
		this.name = name;
		this.weight = weight;
		this.isRageValence = isRageValence;
		if (isRageValence) this.rageWeights = new HashMap<Integer, Double>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Double getWeight() {
		return weight;
	}
	
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	public boolean isRageValence() {
		return isRageValence;
	}
	
	public void setRageValence(boolean isRageValence) {
		this.isRageValence = isRageValence;
	}
	
	public Map<Integer, Double> getRageWeights() {
		if (!isRageValence) return null;
		return this.rageWeights;
	}
	
	public void setRageWeights(Map<Integer, Double> rageWeights) {
		if (!isRageValence) return;
		this.rageWeights = rageWeights;
	}
	
	public Double getRageWeight(Integer index) {
		if (!isRageValence) return null;
		return this.rageWeights.get(index);
	}
	
	public void setRageWeight(Integer index, Double value) {
		if (!isRageValence) return;
		this.rageWeights.put(index, value);
	}
	
}	