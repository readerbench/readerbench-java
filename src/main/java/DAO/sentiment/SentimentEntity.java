package DAO.sentiment;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Holds a HashMap containing different sentiments and their 
 * quantifiers.
 * The available sentiments are: very negative, negative, 
 * neutral, positive and very positive.
 * Each sentiment has a value representing it's relevance.
 *   
 * @author Gabriel Gutu
 *
 */
public class SentimentEntity {
	
	/**
	 * Constants representing the sentiments that can be held. 
	 */
	public static final int VERY_NEGATIVE 	= -2;
	public static final int NEGATIVE 		= -1;
	public static final int NEUTRAL 		= 0;
	public static final int POSITIVE 		= 1;
	public static final int VERY_POSITIVE 	= 2;

	/**
	 * HashMap storing the quantifiers for each sentiment. 
	 */
	private HashMap<Integer, Double> sentiments;
	
	/**
	 * HashMap storing the text representation for each sentiment.
	 */
	private HashMap<Integer, String> sentimentsToString;
	
	/**
	 * Initializes an empty HashMap for the sentiments and for the 
	 * sentimentsToString representation and builds the sentimentsToString
	 * HashMap values
	 */
	public SentimentEntity() {
		sentiments = new HashMap<>();
		sentimentsToString = new HashMap<>();
		
		// TODO: replace strings with values from .properties file
		sentimentsToString.put(VERY_NEGATIVE, 	"Very Negative");
		sentimentsToString.put(NEGATIVE, 		"Negative");
		sentimentsToString.put(NEUTRAL, 		"Neutral");
		sentimentsToString.put(POSITIVE, 		"Positive");
		sentimentsToString.put(VERY_POSITIVE, 	"Very Positive");
	}

	/**
	 * Initializes an empty HashMap for the sentiments and for the 
	 * sentimentsToString representation and builds the sentimentsToString
	 * HashMap values and adds the sentiment "key" with the score "value"  
	 * 
	 * @param key
	 * 			the sentiment to be added (should be used as constant) 
	 * @param value
	 * 			sentiment's quantifier
	 */
	public SentimentEntity(Integer key, double value) {
		this();
		add(key, value);
	}

	/**
	 * Adds the sentiment "key" with the score "value" to the
	 * sentiment HashMap
	 * 
	 * @param key
	 * 			the sentiment to be added (should be used as constant)
	 * @param value
	 * 			sentiment's quantifier
	 */
	public void add(Integer key, double value) {
		sentiments.put(key, value);
	}

	/**
	 * @return
	 * 			sentiments representation
	 */
	public HashMap<Integer, Double> getSentiments() {
		return sentiments;
	}

	/**
	 * @return
	 * 			number of held sentiments
	 */
	public int size() {
		return sentiments.size();
	}

	/**
	 * Sets the score "value" for the sentiment "key" and 
	 * returns the key if sentiment was found. Otherwise, 
	 * returns -1
	 * 
	 * @param key
	 * 			the sentiment to be added (should be used as constant)
	 * @param value
	 * 			sentiment's quantifier
	 * @return
	 * 			the key if sentiment was found; -1 otherwise
	 */
	public Integer setValue(Integer key, double value) {
		if (sentiments.containsKey(key)) {
			sentiments.put(key, value);
			return key;
		} else {
			return -1;
		}
	}

	/**
	 * Gets the score for the sentiment "key" and returns
	 * the key if sentiment was found. Otherwise, returns -1.0
	 * 
	 * @param key
	 * 			the sentiment to be selected (should be used as constant)
	 * @return
	 * 			the score of the sentiment it sentiment was found; 
	 * 			otherwise, -1.0 
	 */
	public Double getValue(Integer key) {
		if (sentiments.containsKey(key)) {
			return sentiments.get(key);
		} else {
			return -1.0;
		}
	}

	/**
	 * Gets current sentiment using an iterator.
	 * 
	 * @return
	 * 			current sentiment if available; otherwise, null
	 */
	public Entry<Integer, Double> get() {
		if (!sentiments.isEmpty()) {
			return sentiments.entrySet().iterator().next();
		} else {
			return null;
		}
	}
	
	/**
	 * Gets current sentiment's value using an iterator. 
	 * 
	 * @return
	 * 			current sentiment's value if available; otherwise, null
	 */
	public Double getValue() {
		return getValue(sentiments.entrySet().iterator().next().getKey());
	}

	/**
	 * Gets current sentiment's key using an iterator. 
	 * 
	 * @return
	 * 			current sentiment's key if available; otherwise, null
	 */
	public Integer getKey() {
		if (!sentiments.isEmpty()) {
			return sentiments.entrySet().iterator().next().getKey();
		} else {
			return null;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Integer key : sentiments.keySet()) {
			sb.append(sentimentsToString.get(key));
			sb.append("=");
			sb.append(sentiments.get(key));
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
}
