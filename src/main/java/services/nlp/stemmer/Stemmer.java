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
package services.nlp.stemmer;

import data.Lang;
import services.nlp.lemmatizer.StaticLemmatizer;

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
		case nl:
			return Stemmer_NL.stemWord(word);
		// TODO implement latin stemmer, for now rely on lemmas
		case la:
			return StaticLemmatizer.lemmaStatic(word, Lang.la);
		default:
			return Stemmer_EN.stemWord(word);
		}
	}

	public static void main(String[] args) {
		System.out.println(stemWord("information", Lang.eng));
	}
}
