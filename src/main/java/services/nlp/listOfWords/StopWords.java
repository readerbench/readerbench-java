/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package services.nlp.listOfWords;

import edu.cmu.lti.jawjaw.pobj.Lang;

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
}
