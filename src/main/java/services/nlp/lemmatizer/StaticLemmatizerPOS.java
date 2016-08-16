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
package services.nlp.lemmatizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.Lang;
import services.nlp.lemmatizer.morphalou.Digest_FR;
import services.nlp.lemmatizer.morphalou.Digest_IT;

public class StaticLemmatizerPOS {
	static Logger logger = Logger.getLogger(StaticLemmatizerPOS.class);

	private static Map<String, String> lemmas_fr = null;
	private static Map<String, String> lemmas_it = null;

	private static Map<String, String> initialize(String path, Lang lang) {
		logger.info("Initializing lemmas from " + path);
		Map<String, String> lemmas = new TreeMap<String, String>();
		BufferedReader in;
		try {
			FileInputStream inputFile = new FileInputStream(path);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			in = new BufferedReader(ir);
			String str_linie = "";
			StringTokenizer strk;
			while ((str_linie = in.readLine()) != null) {
				strk = new StringTokenizer(str_linie, "|");
				lemmas.put(strk.nextToken().toLowerCase(), strk.nextToken().toLowerCase());
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lemmas;
	}

	public static String lemmaStatic(String w, String pos, Lang lang) {
		Map<String, String> lemmas;
		switch (lang) {
		case fr:
			lemmas = getLemmasFr();
			break;
		case it:
			lemmas = getLemmasIt();
			break;
		case es:
			return StaticLemmatizer.lemmaStatic(w, Lang.es);
		case eng:
			return StaticLemmatizer.lemmaStatic(w, Lang.eng);
		// return Morphology.lemmaStatic(w, pos, true);
		default:
			lemmas = null;
		}
		if (lemmas == null) {
			return w;
		}
		String lemma = null;
		if (pos != null) {
			lemma = lemmas.get((w + "_" + pos).toLowerCase());
			if (lemma != null) {
				return lemma;
			}
		}
		// try each significant POS
		String[] possiblePOSs = { "NN", "VB", "JJ", "RB", "PR", "DT", "IN", "UH", "CC" };
		for (String possiblePOS : possiblePOSs) {
			String concept = (w + "_" + possiblePOS).toLowerCase();
			if (lemmas.containsKey(concept)) {
				lemma = lemmas.get(concept);
				break;
			}
		}
		if (lemma != null) {
			return lemma;
		}
		return w;
	}

	public static Map<String, String> getLemmasFr() {
		if (lemmas_fr == null) {
			lemmas_fr = initialize(Digest_FR.PATH_TO_TEXT_LEMMAS_FR, Lang.fr);
		}
		return lemmas_fr;
	}

	public static Map<String, String> getLemmasIt() {
		if (lemmas_it == null) {
			lemmas_it = initialize(Digest_IT.PATH_TO_TEXT_LEMMAS_IT, Lang.it);
		}
		return lemmas_it;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		System.out.println(StaticLemmatizerPOS.lemmaStatic("pointés", null, Lang.fr));
		System.out.println(StaticLemmatizerPOS.lemmaStatic("mangio", "VB", Lang.it));
	}
}
