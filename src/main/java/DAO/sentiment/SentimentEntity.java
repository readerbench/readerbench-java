package DAO.sentiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a HashMap containing different sentiments and their 
 * scores.
 * Sentiments are stored as valences from the Valences class.
 *   
 * @author Gabriel Gutu
 *
 */
public class SentimentEntity {
	
	/**
	 * HashMap storing the valences and their quantifier 
	 */
	private Map<Integer, Double> sentiments;
	
	/**
	 * Initializes an empty Map for the sentiments 
	 */
	public SentimentEntity() {
		sentiments = new HashMap<>();
	}

	/**
	 * Adds a sentiment with the valence "key" and 
	 * the score "value" to the sentiment Map
	 * 
	 * @param key
	 * 			the valence to be added (should be 
	 * 			used as one constant from the Valences class)
	 * @param value
	 * 			sentiment's quantifier
	 */
	public void add(Integer key, double value) {
		sentiments.put(key, value);
	}
	
	/**
	 * Removes the sentiment with the valence "key"
	 * from the sentiment Map
	 *  
	 * @param key
	 * 			the valence of the sentiment that
	 * 			has to be removed
	 */
	public void remove(Integer key) {
		if (sentiments.containsKey(key)) {
			sentiments.remove(key);
		}
	}
	
	/**
	 * Gets the score for the sentiment with the 
	 * valence "key" and returns the sentiment value
	 * if sentiment was found. Otherwise, returns null
	 * 
	 * @param key
	 * 			the sentiment to be selected (should be 
	 * 			used as one constant from the Valences class)
	 * @return
	 * 			the score of the sentiment if the sentiment 
	 * 			was found; otherwise, null 
	 */
	public Double get(Integer key) {
		if (sentiments.containsKey(key)) {
			return sentiments.get(key);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the score "value" for the valence "key"
	 * 
	 * @param key
	 * 			the valence to be added (should be used as constant)
	 * @param value
	 * 			sentiment's quantifier
	 */
	public void set(Integer key, double value) {
		if (sentiments.containsKey(key)) {
			sentiments.put(key, value);
		}
	}

	/**
	 * Returns the sentiments Map 
	 * 
	 * @return
	 * 			sentiments representation
	 */
	public Map<Integer, Double> getAll() {
		return sentiments;
	}
	
	/**
	 * Removes all the sentiments
	 */
	public void removeAll() {
		sentiments = null;
	}

	/**
	 * Returns the size of the sentiments Map 
	 * 
	 * @return
	 * 			number of held sentiments
	 */
	public int size() {
		return sentiments.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Integer key : sentiments.keySet()) {
			try {
				sb.append(Valences.getValenceName(key).toString());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append("=");
			sb.append(sentiments.get(key));
			sb.append(",");
		}
		// delete trailing comma
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
}
