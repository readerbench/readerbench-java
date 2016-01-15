package webService.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.sentiment.SentimentValence;
import services.commons.Formatting;
import webService.ReaderBenchServer;
import webService.result.ResultSentiment;
import webService.result.ResultValence;

public class SentimentAnalysis {

	private static Logger logger = Logger.getLogger(SentimentAnalysis.class);
	
	/**
	 * Get sentiment values for the entire document and for each paragraph
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	public static List<ResultSentiment> getSentiment(AbstractDocument queryDoc) {

		List<ResultValence> results = new ArrayList<ResultValence>();
		List<ResultSentiment> resultsSentiments = new ArrayList<ResultSentiment>();

		logger.info("Starting building sentiments...");
		// results.add(new Result("Document",
		// Formatting.formatNumber(queryDoc.getSentimentEntity().getAggregatedValue())));
		Map<SentimentValence, Double> rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
		// logger.info("There are " + rageSentimentsValues.size() + " rage
		// setiments.");
		Iterator<Map.Entry<SentimentValence, Double>> it = rageSentimentsValues.entrySet().iterator();
		List<ResultValence> localResults = new ArrayList<>();
		while (it.hasNext()) {
			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
			SentimentValence sentimentValence = (SentimentValence) pair.getKey();
			Double sentimentValue = pair.getValue();
			localResults.add(new ResultValence(
				sentimentValence.getIndexLabel().replace("_RAGE", ""),
				Formatting.formatNumber(sentimentValue)
			));
		}

		List<ResultSentiment> blockSentiments = new ArrayList<ResultSentiment>();
		
		for (Block b : queryDoc.getBlocks()) {
			/*
			 * results.add(new Result("Paragraph " + b.getIndex(),
			 * Formatting.formatNumber(b.getSentimentEntity().getAggregatedValue
			 * ())));
			 */

			rageSentimentsValues = b.getSentimentEntity().getAggregatedValue();
			it = rageSentimentsValues.entrySet().iterator();
			localResults = new ArrayList<ResultValence>();
			while (it.hasNext()) {
				Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
				SentimentValence sentimentValence = (SentimentValence) pair.getKey();
				Double sentimentValue = (Double) pair.getValue();
				localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""),
						Formatting.formatNumber(sentimentValue)));
			}

			List<ResultSentiment> sentencesSentiments = new ArrayList<ResultSentiment>();
			
			for (Sentence s : b.getSentences()) {
				/*
				 * results.add(new Result("Paragraph " + b.getIndex() +
				 * " / Sentence " + s.getIndex(),
				 * Formatting.formatNumber(s.getSentimentEntity().
				 * getAggregatedValue())));
				 */

				rageSentimentsValues = s.getSentimentEntity().getAggregatedValue();
				it = rageSentimentsValues.entrySet().iterator();
				localResults = new ArrayList<ResultValence>();
				while (it.hasNext()) {
					Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
					SentimentValence sentimentValence = (SentimentValence) pair.getKey();
					Double sentimentValue = (Double) pair.getValue();
					localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""),
							Formatting.formatNumber(sentimentValue)));
				}
				
				sentencesSentiments.add(new ResultSentiment("\t\tSentence " + s.getIndex(), localResults, null));
			}
			
			blockSentiments.add(new ResultSentiment("\tParagraph " + b.getIndex(), localResults, sentencesSentiments));
		}
		
		resultsSentiments.add(new ResultSentiment("Document", localResults, blockSentiments));

		return resultsSentiments;
	}
	
}
