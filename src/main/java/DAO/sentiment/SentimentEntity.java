package DAO.sentiment;

import java.util.HashMap;
import java.util.Iterator;
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
	 * Map that stores the valences and their 
	 * quantifier for sentiments 
	 */
	private Map<SentimentValence, Double> sentiments;
	
	private Map<SentimentValence, Double> rageSentiments;
	
	/**
	 * Initializes an empty Map for the sentiments 
	 */
	public SentimentEntity() {
		sentiments = new HashMap<>();
		rageSentiments = new HashMap<>();
	}
	
	public void computeRageWeights() {
		Iterator<Map.Entry<SentimentValence, Double>> it = sentiments.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        
	        // compute Rage valences here
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
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
	public void add(SentimentValence key, double value) {
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
	public void set(SentimentValence key, double value) {
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
	public Map<SentimentValence, Double> getAll() {
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
	
	/**
	 * Returns the aggregated score of sentiments
	 * 
	 * @return
	 * 			aggregated score of sentiments
	 */
	public Map<SentimentValence,Double> getAggregatedValue() {
		Map<SentimentValence, Double> sentimentAggregatedValues = new HashMap<SentimentValence, Double>();
		Iterator<Map.Entry<SentimentValence, Double>> it = sentiments.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>)it.next();
			SentimentValence sentimentValence = (SentimentValence)pair.getKey();
			Double sentimentValue = (Double)pair.getValue();
			sentimentAggregatedValues.put(sentimentValence, 0.0);
			
			Iterator<Map.Entry<SentimentValence, Double>> itRage = rageSentiments.entrySet().iterator();
			while (itRage.hasNext()) {
				Map.Entry<SentimentValence, Double> pairRage = (Map.Entry<SentimentValence, Double>)it.next();
				SentimentValence sentimentValenceRage = (SentimentValence)pairRage.getKey();
				Double sentimentValueRage = (Double)pairRage.getValue();
				
				sentimentAggregatedValues.put(sentimentValence, 
						SentimentWeights.getSentimentsWeight(
								sentimentValence.getId(), sentimentValenceRage.getId()) *
						sentimentAggregatedValues.get(sentimentValence));
				
				itRage.remove();
			}
			
	        // TODO: compute (primary sentiment, RAGE sentiment) weights here
	        it.remove();
		}
		return sentimentAggregatedValues;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (SentimentValence sentimentValence : sentiments.keySet()) {
			try {
				//sb.append(Valences.getValenceName(key).toString());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append("=");
			sb.append(sentiments.get(sentimentValence));
			sb.append(",");
		}
		// delete trailing comma
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
}
