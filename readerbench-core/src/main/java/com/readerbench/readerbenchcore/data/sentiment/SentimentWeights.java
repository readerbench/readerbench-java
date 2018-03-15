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
package com.readerbench.readerbenchcore.data.sentiment;

import com.readerbench.dao.ValenceDAO;
import com.readerbench.dao.WeightDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SentimentWeights contains an array that stores weights between primary
 * sentiments and RAGE sentiments. The array has the number of rows equal to the
 * number of primary sentiments and the number of columns equal to the number of
 * RAGE sentiments.
 * 
 * @author Gabriel Gutu
 *
 */
public class SentimentWeights {

	private static final Logger LOGGER = LoggerFactory.getLogger(SentimentWeights.class);

	/**
	 * The sentiment grid contains the associations of (primary sentiment, RAGE
	 * sentiment) pairs for weights
	 */
	private static SentimentGrid<Double> sentimentGrid;

	/**
	 * Initializes the sentiments grid
	 */
	public static void initialize() {

		int noPrimarySentiments = ValenceDAO.getInstance().findByRage(false).size();
		int noRageSentiments = ValenceDAO.getInstance().findByRage(true).size();
		sentimentGrid = new SentimentGrid<>(noPrimarySentiments, noRageSentiments);

		// load all sentiment valences from database
		List<com.readerbench.data.pojo.SentimentValence> svs = ValenceDAO.getInstance().findAll();
		int i = 0, j = 0;
		for (com.readerbench.data.pojo.SentimentValence sv : svs) {
			// create DAO SentimentValences HashMap for Sentiment Entity
			// initialization
			if (!sv.getRage())
				sentimentGrid.setIndex(sv.getIndexLabel(), i++);
			else
				sentimentGrid.setIndex(sv.getIndexLabel(), j++);
		}

		// load all sentiment valences weights and store them in SentimentGrid
		List<com.readerbench.data.pojo.Weight> weights = WeightDAO.getInstance().findAll();
		for (com.readerbench.data.pojo.Weight w : weights) {
			sentimentGrid.set(w.getFkPrimaryValence().getIndexLabel(), w.getFkRageValence().getIndexLabel(),
					w.getValue());
		}
	}

	/**
	 * Gets the weight of a pair of sentiments
	 * 
	 * @param primarySentiment
	 *            the primary sentiment
	 * @param rageSentiment
	 *            the RAGE sentiment
	 * @return the weight of the pair of sentiments
	 */
	public static Double getSentimentsWeight(String primarySentiment, String rageSentiment) {
		return sentimentGrid.get(primarySentiment, rageSentiment);
	}

}
