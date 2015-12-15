package data.sentiment;

import java.util.Map;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data.dao.ValenceDAO;
import data.dao.WeightDAO;
import data.pojo.SentimentValence;
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
	 * The sentiment grid contains the associations
	 * of (primary sentiment, RAGE sentiment) pairs
	 * for weights
	 */
	private static SentimentGrid<Double> sentimentGrid;
	
	/**
	 * Initializes the sentiments grid
	 */
	public SentimentWeights() {
		
		int noPrimarySentiments = ValenceDAO.getInstance().findByRage(false).size();
		int noRageSentiments = ValenceDAO.getInstance().findByRage(true).size();
		sentimentGrid = new SentimentGrid<>(noPrimarySentiments, noRageSentiments);;
		
		// load all sentiment valences from database
		List<data.pojo.SentimentValence> svs = ValenceDAO.getInstance().findAll();
        int i = 0, j = 0;
		for (data.pojo.SentimentValence sv : svs) {
			logger.info("Valenta " + (sv.getRage() == true ? " rage: " : " primara: ") + sv.getLabel());
			// create DAO SentimentValences HashMap for Sentiment Entity initialization 
			if (!sv.getRage())
				sentimentGrid.setIndex(sv.getIndexLabel(), i++);
			else
				sentimentGrid.setIndex(sv.getIndexLabel(), j++);
		}
		
		// load all sentiment valences weights and store them in SentimentGrid
		List<data.pojo.Weight> weights = WeightDAO.getInstance().findAll();
		for (data.pojo.Weight w : weights) {
			logger.info("Perechea (" +
					w.getFkPrimaryValence().getLabel() + 
					" (id: " + w.getFkPrimaryValence().getId() + "), " 
					+ w.getFkRageValence().getIndexLabel() + 
					" (id: " + w.getFkRageValence().getId() + "))" 
					+ " = " + w.getValue());
			sentimentGrid.set(w.getFkPrimaryValence().getIndexLabel(), w.getFkRageValence().getIndexLabel(), w.getValue());
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
	public static Double getSentimentsWeight(String primarySentiment, String rageSentiment) {
		return sentimentGrid.get(primarySentiment, rageSentiment);
	}
	
}