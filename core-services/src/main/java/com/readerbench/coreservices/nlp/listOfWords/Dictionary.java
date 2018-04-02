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

import java.util.Set;

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
	public static ListOfWords dictionary_la = null;

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
		case la:
			return getDictionaryLa().getWords();
		default:
			return getDictionaryEn().getWords();
		}
	}

	public synchronized static ListOfWords getDictionaryRo() {
		if (dictionary_ro == null)
			dictionary_ro = new ListOfWords("resources/config/RO/word lists/dict_ro.txt");
		return dictionary_ro;
	}

	public synchronized static ListOfWords getDictionaryFr() {
		if (dictionary_fr == null)
			dictionary_fr = new ListOfWords("resources/config/FR/word lists/dict_fr.txt");
		return dictionary_fr;
	}

	public synchronized static ListOfWords getDictionaryIt() {
		if (dictionary_it == null)
			dictionary_it = new ListOfWords("resources/config/IT/word lists/dict_it.txt");
		return dictionary_it;
	}

	public synchronized static ListOfWords getDictionaryEn() {
		if (dictionary_en == null)
			dictionary_en = new ListOfWords("resources/config/EN/word lists/dict_en.txt");
		return dictionary_en;
	}

	public synchronized static ListOfWords getDictionaryEs() {
		if (dictionary_es == null)
			dictionary_es = new ListOfWords("resources/config/ES/word lists/dict_es.txt");
		return dictionary_es;
	}

	public synchronized static ListOfWords getDictionaryNl() {
		if (dictionary_nl == null)
			dictionary_nl = new ListOfWords("resources/config/NL/word lists/dict_nl.txt");
		return dictionary_nl;
	}

	public synchronized static ListOfWords getDictionaryLa() {
		if (dictionary_la == null)
			dictionary_la = new ListOfWords("resources/config/LA/word lists/dict_la.txt");
		return dictionary_la;
	}

	public static boolean isDictionaryWord(String s, Lang lang) {
		if (lang == null)
			return true;
		return getDictionaryWords(lang).contains(s);
	}

	public static void main(String[] args) {
		
		System.out.println(Dictionary.isDictionaryWord("abalobé", Lang.fr));
		System.out.println(Dictionary.isDictionaryWord("cosi", Lang.it));
		System.out.println(Dictionary.isDictionaryWord("atarugaear", Lang.es));
	}
}
