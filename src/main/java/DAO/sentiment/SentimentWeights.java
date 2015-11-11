package DAO.sentiment;

import java.util.Map;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import DAO.db.ValenceDAO;
import DAO.db.WeightDAO;
import webService.ReaderBenchServer;

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
	
	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	
	/**
	 * Number of primary valences 
	 */
	private static Integer NO_PRIMARY_VALENCES = 10;
	
	/**
	 * Number of RAGE valences 
	 */
	private static Integer NO_RAGE_VALENCES = 6;

	/**
	 * The sentiment grid contains the associations
	 * of (primary sentiment, RAGE sentiment) pairs
	 * for weights
	 */
	private static SentimentGrid<Double> sentimentGrid;
	private SentimentValence[] sentimentValences;
	
	/**
	 * Initializes the sentiments grid
	 */
	public SentimentWeights() {
		sentimentGrid = new SentimentGrid<>(NO_PRIMARY_VALENCES, NO_RAGE_VALENCES);
		
		// load all sentiment valences from database
		List<pojo.SentimentValence> svs = ValenceDAO.getInstance().findAll();
		for (pojo.SentimentValence sv : svs) {
			System.out.println("Valenta " + sv.getLabel());
		}
		
		// load all sentiment valences weights and store them in SentimentGrid
		List<pojo.Weight> weights = WeightDAO.getInstance().findAll();
		for (pojo.Weight w : weights) {
			logger.info("Perchea (" + w.getFkPrimaryValence().getLabel() + ", " + w.getFkRageValence().getIndexLabel() + ")" + " = " + w.getValue());
			sentimentGrid.set(w.getFkPrimaryValence().getId(), w.getFkRageValence().getId(), w.getValue());
		}
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
	public static Double getSentimentsWeight(Integer primarySentiment, Integer rageSentiment) {
		return sentimentGrid.get(primarySentiment, rageSentiment);
	}
	
}
