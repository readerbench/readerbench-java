package DAO.sentiment;

import java.util.Map;

/**
 * SentimentWeights contains an array that stores 
 * weights between primary sentiments and RAGE
 * sentiments. The array has the number of rows
 * equal to the number of primary sentiments and 
 * the number of columns equal to the number of 
 * RAGE sentiments.
 * 
 * @author Gabriel Gutu
 *
 */
public class SentimentWeights {
	
	/**
	 * Number of primary valences 
	 */
	private static Integer NO_PRIMARY_VALENCES = 10;
	
	/**
	 * Number of RAGE valences 
	 */
	private static Integer NO_RAGE_VALENCES = 6;
	
	private static Integer ANEW_VALENCE = 0;
	private static Integer ANEW_AROUSAL = 1;
	private static Integer ANEW_DOMINANCE = 2;
	private static Integer GI_ONE = 3;
	private static Integer GI_TWO = 4;
	private static Integer STANFORD_NLP = 5;
	
	private static Integer RAGE_ONE = 0;
	private static Integer RAGE_TWO = 1;
	private static Integer RAGE_THREE = 2;

	/**
	 * The sentiment grid contains the associations
	 * of (primary sentiment, RAGE sentiment) pairs
	 * for weights
	 */
	private SentimentGrid<Double> sentimentGrid;
	
	/**
	 * Initializes the sentiments grid
	 */
	public SentimentWeights() {
		sentimentGrid = new SentimentGrid<>(NO_PRIMARY_VALENCES, NO_RAGE_VALENCES);
	}
	
	/**
	 * Sets sentiments weights
	 */
	public void setSentimentsWeights() {
		// TODO: get weight from database?
		sentimentGrid.set(SentimentValence.ANEW_VALENCE.getId(), SentimentValence.RAGE_ONE.getId(), 1.0);
		sentimentGrid.set(SentimentValence.ANEW_AROUSAL.getId(), SentimentValence.RAGE_ONE.getId(), 1.0);
		sentimentGrid.set(SentimentValence.ANEW_DOMINANCE.getId(), SentimentValence.RAGE_ONE.getId(), 1.0);
	}
	
	/**
	 * Gets the weight of a pair of sentiments
	 * 
	 * @param primarySentiment
	 * 			the primary sentiment
	 * @param rageSentiment
	 * 			the RAGE sentiment
	 * @return
	 * 			the weight of the pair of sentiments
	 */
	public Double getSentimentsWeight(Integer primarySentiment, Integer rageSentiment) {
		return sentimentGrid.get(primarySentiment, rageSentiment);
	}
	
}
