package services.nlp.stemmer;

import edu.cmu.lti.jawjaw.pobj.Lang;

public class Stemmer {
	public static String stemWord(String word, Lang lang) {
		if (lang == null)
			return word;
		switch (lang) {
		case fr:
			return Stemmer_FR.stemWord(word);
		case ro:
			return Stemmer_RO.stemWord(word);
		case it:
			return Stemmer_IT.stemWord(word);
		case es:
			return Stemmer_ES.stemWord(word);
		default:
			return Stemmer_EN.stemWord(word);
		}
	}

	public static void main(String[] args) {
		System.out.println(stemWord("information", Lang.eng));
	}
}
