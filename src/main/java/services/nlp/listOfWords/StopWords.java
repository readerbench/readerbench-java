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
package services.nlp.listOfWords;

import data.Lang;

/**
 * 
 * @author Mihai Dascalu
 */
public class StopWords {
	public static ListOfWords stopwords_ro = null;
	public static ListOfWords stopwords_fr = null;
	public static ListOfWords stopwords_en = null;
	public static ListOfWords stopwords_it = null;
	public static ListOfWords stopwords_es = null;
	public static ListOfWords stopwords_nl = null;
	public static ListOfWords stopwords_la = null;

	public static boolean isStopWord(String s, Lang lang) {
		if (lang == null)
			return false;
		switch (lang) {
		case fr:
			return getStopwordsFr().getWords().contains(s);
		case it:
			return getStopwordsIt().getWords().contains(s);
		case es:
			return getStopwordsEs().getWords().contains(s);
		case ro:
			return getStopwordsRo().getWords().contains(s);
		case nl:
			return getStopwordsNl().getWords().contains(s);
		case la:
			return getStopwordsLa().getWords().contains(s);
		default:
			return getStopwordsEn().getWords().contains(s);
		}
	}

	public static ListOfWords getStopwordsRo() {
		if (stopwords_ro == null)
			stopwords_ro = new ListOfWords("resources/config/Stopwords/stopwords_ro.txt");
		return stopwords_ro;
	}

	public static ListOfWords getStopwordsFr() {
		if (stopwords_fr == null)
			stopwords_fr = new ListOfWords("resources/config/Stopwords/stopwords_fr.txt");
		return stopwords_fr;
	}

	public static ListOfWords getStopwordsEn() {
		if (stopwords_en == null)
			stopwords_en = new ListOfWords("resources/config/Stopwords/stopwords_en.txt");
		return stopwords_en;
	}

	public static ListOfWords getStopwordsIt() {
		if (stopwords_it == null)
			stopwords_it = new ListOfWords("resources/config/Stopwords/stopwords_it.txt");
		return stopwords_it;
	}

	public static ListOfWords getStopwordsEs() {
		if (stopwords_es == null)
			stopwords_es = new ListOfWords("resources/config/Stopwords/stopwords_es.txt");
		return stopwords_es;
	}

	public static ListOfWords getStopwordsNl() {
		if (stopwords_nl == null)
			stopwords_nl = new ListOfWords("resources/config/Stopwords/stopwords_nl.txt");
		return stopwords_nl;
	}

	public static ListOfWords getStopwordsLa() {
		if (stopwords_la == null)
			stopwords_la = new ListOfWords("resources/config/Stopwords/stopwords_la.txt");
		return stopwords_la;
	}
}
