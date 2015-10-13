/*
 * Copyright 2009 Carnegie Mellon University
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.cmu.lti.jawjaw.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.jawjaw.db.SenseDAO;
import edu.cmu.lti.jawjaw.db.SynlinkDAO;
import edu.cmu.lti.jawjaw.db.WordDAO;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Sense;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.SynsetDef;
import edu.cmu.lti.jawjaw.pobj.WordJAW;

public class WordNetUtil {

	public static List<Synset> wordToSynsets(String word, POS pos, Lang lang) {
		List<WordJAW> words = WordDAO.findWordsByLemmaAndPos(word, pos, lang);
		List<Synset> results = new ArrayList<Synset>();
		for (WordJAW wordObj : words) {
			int wordid = wordObj.getWordid();
			List<Sense> senses = SenseDAO.findSensesByWordid(wordid, lang);
			for (Sense sense : senses) {
				Synset synset = new Synset(sense.getSynset(), null, null, null);
				results.add(synset);
			}
		}
		return results;
	}

	public static List<WordJAW> synsetToWords(String synset, Lang lang) {
		List<WordJAW> words = new ArrayList<WordJAW>();
		List<Sense> senses = SenseDAO.findSensesBySynset(synset, lang);
		for (Sense sense : senses) {
			WordJAW word = WordDAO.findWordByWordid(sense.getWordid(), lang);
			words.add(word);
		}
		return words;
	}

	public static String getGloss(SynsetDef synsetDef) {
		/*
		 * def field looks like this: powerful and effective language;
		 * "his eloquence attracted a large congregation";
		 * "fluency in spoken and written English is essential";
		 * "his oily smoothness concealed his guilt from the police"
		 */
		if (synsetDef != null && synsetDef.getDef() != null)
			return synsetDef.getDef().replaceFirst("; \".+", "");

		return "";
	}

	/**
	 * Find words that have a specific relationship with the given word
	 * 
	 * @param word
	 * @param pos
	 * @param link
	 * @return words
	 */
	public static Set<String> findLinks(String word, POS pos, Link link, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		List<Synset> synsets = wordToSynsets(word, pos, lang);
		for (Synset synset : synsets) {
			List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynsetAndLink(synset.getSynset(), link, lang);
			for (Synlink synlink : synlinks) {
				List<Sense> senses = SenseDAO.findSensesBySynsetAndLang(synlink.getSynset2(), lang);
				for (Sense sense : senses) {
					WordJAW wordObj = WordDAO.findWordByWordid(sense.getWordid(), lang);
					results.add(wordObj.getLemma());
				}
			}
		}
		return results;
	}

	// public static Set<String> findLinks(Sense sense, Link link) {
	// Set<String> results = new LinkedHashSet<String>();
	// List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynset(sense
	// .getSynset());
	// for (Synlink synlink : synlinks) {
	// if (synlink.getLink().equals(link)) {
	// List<Sense> senses = SenseDAO.findSensesBySynsetAndLang(
	// synlink.getSynset2(), Lang.eng);
	// for (Sense s : senses) {
	// WordJAW wordObj = WordDAO.findWordByWordid(s.getWordid());
	// results.add(wordObj.getLemma());
	// }
	// }
	// }
	// return results;
	// }

	public static Set<Sense> findLinks(Sense sense, Link link, Lang lang) {
		Set<Sense> results = new LinkedHashSet<Sense>();
		List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynsetAndLink(sense.getSynset(), link, lang);
		for (Synlink synlink : synlinks) {
			results.addAll(SenseDAO.findSensesBySynsetAndLang(synlink.getSynset2(), lang));
		}
		return results;
	}

	public static Set<String> findSynonyms(String word, POS pos, boolean translate, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		List<Synset> synsets = WordNetUtil.wordToSynsets(word, pos, lang);

		// translation only possible between eng and jpn
		Lang anotherLang, targetLang;
		if (lang.equals(Lang.fr)) {
			anotherLang = Lang.fr;
			targetLang = Lang.fr;
		} else if (lang.equals(Lang.it)) {
			anotherLang = Lang.it;
			targetLang = Lang.it;
		} else if (lang.equals(Lang.ro)) {
			anotherLang = Lang.ro;
			targetLang = Lang.ro;
		} else {
			anotherLang = lang.equals(Lang.jpn) ? Lang.eng : Lang.jpn;
			targetLang = translate ? anotherLang : lang;
		}
		for (Synset synset : synsets) {
			List<Sense> moreSenses = SenseDAO.findSensesBySynsetAndLang(synset.getSynset(), targetLang);
			for (Sense moreSense : moreSenses) {
				WordJAW synonym = WordDAO.findWordByWordid(moreSense.getWordid(), targetLang);
				results.add(synonym.getLemma());
			}
		}
		// remove the original if any
		results.remove(word);
		return results;
	}

	// public static Set<String> findSynonyms(Sense sense) {
	// Set<String> results = new LinkedHashSet<String>();
	// List<Sense> moreSenses = SenseDAO.findSensesBySynsetAndLang(
	// sense.getSynset(), Lang.eng);
	// for (Sense moreSense : moreSenses) {
	// WordJAW synonym = WordDAO.findWordByWordid(moreSense.getWordid());
	// results.add(synonym.getLemma());
	// }
	// return results;
	// }

	public static Set<Sense> findSynonyms(Sense sense, Lang lang) {
		Set<Sense> results = new LinkedHashSet<Sense>();
		results.addAll(SenseDAO.findSensesBySynsetAndLang(sense.getSynset(), lang));
		return results;
	}

}
