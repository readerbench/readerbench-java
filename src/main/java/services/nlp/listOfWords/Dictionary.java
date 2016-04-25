/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package services.nlp.listOfWords;

import java.util.Set;

import org.apache.log4j.BasicConfigurator;

import data.Lang;

/**
 * 
 * @author Mihai Dascalu
 */
public class Dictionary {
	public static ListOfWords dictionary_ro = null;
	public static ListOfWords dictionary_fr = null;
	public static ListOfWords dictionary_it = null;
	public static ListOfWords dictionary_en = null;
	public static ListOfWords dictionary_es = null;
	public static ListOfWords dictionary_nl = null;

	public static Set<String> getDictionaryWords(Lang lang) {
		if (lang == null)
			return null;
		switch (lang) {
		case fr:
			return getDictionaryFr().getWords();
		case ro:
			return getDictionaryRo().getWords();
		case it:
			return getDictionaryIt().getWords();
		case es:
			return getDictionaryEs().getWords();
		case nl:
			return getDictionaryNl().getWords();
		default:
			return getDictionaryEn().getWords();
		}
	}

	public static ListOfWords getDictionaryRo() {
		if (dictionary_ro == null)
			dictionary_ro = new ListOfWords("resources/config/Dictionary/dict_ro.txt");
		return dictionary_ro;
	}

	public static ListOfWords getDictionaryFr() {
		if (dictionary_fr == null)
			dictionary_fr = new ListOfWords("resources/config/Dictionary/dict_fr.txt");
		return dictionary_fr;
	}

	public static ListOfWords getDictionaryIt() {
		if (dictionary_it == null)
			dictionary_it = new ListOfWords("resources/config/Dictionary/dict_it.txt");
		return dictionary_it;
	}

	public static ListOfWords getDictionaryEn() {
		if (dictionary_en == null)
			dictionary_en = new ListOfWords("resources/config/Dictionary/dict_en.txt");
		return dictionary_en;
	}

	public static ListOfWords getDictionaryEs() {
		if (dictionary_es == null)
			dictionary_es = new ListOfWords("resources/config/Dictionary/dict_es.txt");
		return dictionary_es;
	}

	public static ListOfWords getDictionaryNl() {
		if (dictionary_nl == null)
			dictionary_nl = new ListOfWords("resources/config/Dictionary/dict_nl.txt");
		return dictionary_nl;
	}

	public static boolean isDictionaryWord(String s, Lang lang) {
		if (lang == null)
			return true;
		return getDictionaryWords(lang).contains(s);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		System.out.println(Dictionary.isDictionaryWord("abalobé", Lang.fr));
		System.out.println(Dictionary.isDictionaryWord("cosi", Lang.it));
		System.out.println(Dictionary.isDictionaryWord("atarugaear", Lang.es));
	}
}
