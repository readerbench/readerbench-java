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
package com.readerbench.data.sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Holds a HashMap containing different sentiments and their scores. Sentiments
 * are stored as valences from the Valences class.
 *
 * @author Gabriel Gutu
 *
 */
public class SentimentEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(SentimentEntity.class);

	/**
	 * Map that stores the valences and their quantifier for sentiments
	 */
	private Map<SentimentValence, Double> sentiments;

	/**
	 * Initializes an empty Map for the sentiments
	 */
	public SentimentEntity() {
		sentiments = new HashMap<>();

	}

	public void init() {

		SentimentValence.getAllValences().stream().forEach((daoSe) -> {
			add(daoSe, 0.0);
		});
	}

	/**
	 * Adds a sentiment with the valence "key" and the score "value" to the
	 * sentiment Map
	 *
	 * @param key
	 *            the valence to be added (should be used as one constant from
	 *            the Valences class)
	 * @param value
	 *            sentiment's quantifier
	 */
	public void add(SentimentValence key, double value) {
		sentiments.put(key, value);
	}

	/**
	 * Removes the sentiment with the valence "key" from the sentiment Map
	 *
	 * @param key
	 *            the valence of the sentiment that has to be removed
	 */
	public void remove(SentimentValence key) {
		if (sentiments.containsKey(key)) {
			sentiments.remove(key);
		}
	}

	/**
	 * Gets the score for the sentiment with the valence "key" and returns the
	 * sentiment value if sentiment was found. Otherwise, returns null
	 *
	 * @param key
	 *            the sentiment to be selected (should be used as one constant
	 *            from the Valences class)
	 * @return the score of the sentiment if the sentiment was found; otherwise,
	 *         null
	 */
	public Double get(SentimentValence key) {
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
	 *            the valence to be added (should be used as constant)
	 * @param value
	 *            sentiment's quantifier
	 */
	public void set(SentimentValence key, double value) {
		if (sentiments.containsKey(key)) {
			sentiments.put(key, value);
		}
	}

	/**
	 * Returns the sentiments Map
	 *
	 * @return sentiments representation
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
	 * @return number of held sentiments
	 */
	public int size() {
		return sentiments.size();
	}

	/**
	 * 
	 * @return true if the map contains that sentimentValence, otherwise return
	 *         false
	 * 
	 */
	public boolean contains(SentimentValence daoSe) {
		if (sentiments.containsKey(daoSe)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the aggregated score of sentiments
	 *
	 * @return aggregated score of sentiments
	 */
	public Map<SentimentValence, Double> getAggregatedValue() {
		Map<SentimentValence, Double> rageSentimentsValues = new HashMap<>();

		Iterator<Map.Entry<SentimentValence, Double>> itRageSentiments = sentiments.entrySet().iterator();
		// iterate all rage sentiments
		SentimentValence.getAllValences().stream().filter(SentimentValence::getRage).forEach(rage -> {
			for (Map.Entry<SentimentValence, Double> e : sentiments.entrySet()) {
				if (!e.getKey().getRage()) {
					Double rageValence = rageSentimentsValues.get(rage);
					Double weight = SentimentWeights.getSentimentsWeight(e.getKey().getIndexLabel(),
							rage.getIndexLabel());
					rageSentimentsValues.put(rage,
							(rageValence == null ? 0.0 : rageValence) + (weight == null ? 0.0 : weight) * e.getValue());
				}

			}
		});

		return normalizeValues(rageSentimentsValues);
	}

	public static Map<SentimentValence, Double> normalizeValues(Map<SentimentValence, Double> valences) {
		Map<SentimentValence, Double> result = new HashMap<>();
		valences.entrySet().stream().forEach(e -> {
			result.put(e.getKey(), Math.exp(e.getValue()));
		});
		double sum = result.entrySet().stream().mapToDouble(e -> e.getValue()).sum();
		return result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / sum));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (SentimentValence sentimentValence : sentiments.keySet()) {
			try {
				// sb.append(Valences.getValenceName(key).toString());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(sentimentValence.getIndexLabel());
			sb.append("=");
			sb.append(sentiments.get(sentimentValence));
			sb.append(",");
		}
		// delete trailing comma
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}
}
