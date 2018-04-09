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
package com.readerbench.coreservices.nlp.listOfWords;

import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * 
 * @author Mihai Dascalu
 */
public class MapOfWordWeights {

	private static final Logger LOGGER = LoggerFactory.getLogger(MapOfWordWeights.class);

	private Map<String, Double> words;

	public MapOfWordWeights(String path, Lang lang) {
		BufferedReader in = null;
		words = new TreeMap<String, Double>();
		try {
			FileInputStream inputFile = new FileInputStream(path);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			in = new BufferedReader(ir);
			String line = null;
			while ((line = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line.trim());
				String word = st.nextToken();
				Double weight = 0.0;
				try {
					weight = Double.valueOf(st.nextToken());
					if (weight < 0 || weight > 1)
						throw new Exception("Weight not in [0; 1] interval!");
				} catch (Exception e) {
					e.printStackTrace();
					weight = 1.0;
				}
				if (Dictionary.isDictionaryWord(word, lang))
					words.put(word, weight);
				else
					LOGGER.info("Word " + word
							+ " was not found within the dictionary words");
			}
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				LOGGER.error(ex.getMessage());
			}
		}
	}

	public Map<String, Double> getWords() {
		return words;
	}

	public void setWords(Map<String, Double> words) {
		this.words = words;
	}

}
