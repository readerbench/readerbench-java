/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Collections;
import services.commons.Formatting;
import webService.result.ResultSentiment;
import webService.result.ResultValence;

public class SentimentAnalysis {

	private static Logger logger = Logger.getLogger(SentimentAnalysis.class);
	
	/**
	 * Get sentiment values for the entire document and for each paragraph
	 *
	 * @param queryDoc The document to be analyzed
	 * @return List of sentiment values per entity
	 */
	public static List<ResultSentiment> getSentiment(AbstractDocument queryDoc) {
		List<ResultSentiment> resultsSentiments = new ArrayList<>();

		logger.info("Starting building sentiments...");
		Map<SentimentValence, Double> rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
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
        Collections.sort(localResults);

		List<ResultSentiment> blockSentiments = new ArrayList<>();		
		for (Block b : queryDoc.getBlocks()) {
			rageSentimentsValues = b.getSentimentEntity().getAggregatedValue();
			it = rageSentimentsValues.entrySet().iterator();
			localResults = new ArrayList<>();
			while (it.hasNext()) {
				Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
				SentimentValence sentimentValence = (SentimentValence) pair.getKey();
				localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""),
						Formatting.formatNumber(pair.getValue())));
			}
            Collections.sort(localResults);

			List<ResultSentiment> sentencesSentiments = new ArrayList<>();
			for (Sentence s : b.getSentences()) {
				rageSentimentsValues = s.getSentimentEntity().getAggregatedValue();
				it = rageSentimentsValues.entrySet().iterator();
				localResults = new ArrayList<>();
				while (it.hasNext()) {
					Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
					SentimentValence sentimentValence = (SentimentValence) pair.getKey();
					localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""),
							Formatting.formatNumber(pair.getValue())));
                }
                Collections.sort(localResults);
				sentencesSentiments.add(new ResultSentiment("Sentence " + s.getIndex(), localResults, null, s.getText()));
			}
			blockSentiments.add(new ResultSentiment("Paragraph " + b.getIndex(), localResults, sentencesSentiments, b.getText()));
		}
		resultsSentiments.add(new ResultSentiment("Document", localResults, blockSentiments, queryDoc.getText()));

		return resultsSentiments;
	}
}
