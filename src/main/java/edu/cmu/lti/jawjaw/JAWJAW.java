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
package edu.cmu.lti.jawjaw;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.jawjaw.db.SenseDAO;
import edu.cmu.lti.jawjaw.db.SynlinkDAO;
import edu.cmu.lti.jawjaw.db.SynsetDAO;
import edu.cmu.lti.jawjaw.db.SynsetDefDAO;
import edu.cmu.lti.jawjaw.db.WordDAO;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Sense;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.SynsetDef;
import edu.cmu.lti.jawjaw.pobj.WordJAW;
import edu.cmu.lti.jawjaw.util.WordNetUtil;

/**
 * Java Wrapper for Japanese WordNet.
 * 
 * This is a facade class that provides simple APIs for end users. For doing
 * more complicated stuff, use DAO classes under the package
 * edu.cmu.lti.jawjaw.dao
 * 
 * @author Hideki Shima
 * 
 */
public class JAWJAW {

	/**
	 * Finds hypernyms of a word. According to
	 * <a href="http://en.wikipedia.org/wiki/WordNet">wikipedia</a>,
	 * <ul>
	 * <li>(Noun) hypernyms: Y is a hypernym of X if every X is a (kind of) Y
	 * (canine is a hypernym of dog)</li>
	 * <li>(Verb) hypernym: the verb Y is a hypernym of the verb X if the
	 * activity X is a (kind of) Y (travel is an hypernym of movement)</li>
	 * </ul>
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return hypernyms
	 */
	public static Set<String> findHypernyms(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.hype, lang);
	}

	public static Set<Sense> findHypernyms(Sense sense, Lang lang) {
		return WordNetUtil.findLinks(sense, Link.hype, lang);
	}

	/**
	 * Finds hyponyms of a word. According to
	 * <a href="http://en.wikipedia.org/wiki/WordNet">wikipedia</a>,
	 * <ul>
	 * <li>(Noun) hyponyms: Y is a hyponym of X if every Y is a (kind of) X (dog
	 * is a hyponym of canine)
	 * </ul>
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return hyponyms
	 */
	public static Set<String> findHyponyms(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.hypo, lang);
	}

	public static Set<Sense> findHyponyms(Sense sense, Lang lang) {
		return WordNetUtil.findLinks(sense, Link.hypo, lang);
	}

	/**
	 * Finds meronyms of a word. According to
	 * <a href="http://en.wikipedia.org/wiki/WordNet">wikipedia</a>,
	 * <ul>
	 * <li>(Noun) meronym: Y is a meronym of X if Y is a part of X (window is a
	 * meronym of building)
	 * </ul>
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return meronyms
	 */
	public static Set<String> findMeronyms(String word, POS pos, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		results.addAll(WordNetUtil.findLinks(word, pos, Link.mmem, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.msub, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.mprt, lang));
		return results;
	}

	/**
	 * Finds holonyms of a word. According to
	 * <a href="http://en.wikipedia.org/wiki/WordNet">wikipedia</a>,
	 * <ul>
	 * <li>(Noun) holonym: Y is a holonym of X if X is a part of Y (building is
	 * a holonym of window)
	 * </ul>
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return holonyms
	 */
	public static Set<String> findHolonyms(String word, POS pos, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		results.addAll(WordNetUtil.findLinks(word, pos, Link.hmem, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.hsub, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.hprt, lang));
		return results;
	}

	/**
	 * Finds instances of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return instances
	 */
	public static Set<String> findInstances(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.inst, lang);
	}

	/**
	 * Finds has-instance relations of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return has-instance relations
	 */
	public static Set<String> findHasInstances(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.hasi, lang);
	}

	/**
	 * Get attributes of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return attributes
	 */
	public static Set<String> findAttributes(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.attr, lang);
	}

	/**
	 * Finds similar-to relations of an adjective(?).
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return similar-to relations
	 */
	public static Set<String> findSimilarTo(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.sim, lang);
	}

	/**
	 * Finds entailed consequents of a word. According to
	 * <a href="http://en.wikipedia.org/wiki/WordNet">wikipedia</a>,
	 * <ul>
	 * <li>(Verb) entailment: the verb Y is entailed by X if by doing X you must
	 * be doing Y (to sleep is entailed by to snore)
	 * </ul>
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return consequents
	 */
	public static Set<String> findEntailments(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.enta, lang);
	}

	/**
	 * Finds causes of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return causes
	 */
	public static Set<String> findCauses(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.caus, lang);
	}

	/**
	 * Finds words in see also relationship.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return see also words
	 */
	public static Set<String> findSeeAlso(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.also, lang);
	}

	/**
	 * Find synonyms of a word. We assume words sharing the same synsets are
	 * synonyms.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return synonyms
	 */
	public static Set<String> findSynonyms(String word, POS pos, Lang lang) {
		return WordNetUtil.findSynonyms(word, pos, false, lang);
	}

	public static Set<Sense> findSynonyms(Sense sense, Lang lang) {
		return WordNetUtil.findSynonyms(sense, lang);
	}

	/**
	 * Find antonyms of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return antonyms
	 */
	public static Set<String> findSeeAntonyms(String word, POS pos, Lang lang) {
		return WordNetUtil.findLinks(word, pos, Link.ants, lang);
	}

	public static Set<Sense> findSeeAntonyms(Sense sense, Lang lang) {
		return WordNetUtil.findLinks(sense, Link.ants, lang);
	}

	/**
	 * Get domains of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return domains
	 */
	public static Set<String> findDomains(String word, POS pos, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmnc, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmnr, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmnu, lang));
		return results;
	}

	/**
	 * Get in-domain relations of a word.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return in-domain relations
	 */
	public static Set<String> findInDomains(String word, POS pos, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmtc, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmtr, lang));
		results.addAll(WordNetUtil.findLinks(word, pos, Link.dmtu, lang));
		return results;
	}

	/**
	 * Finds translations of a word. Basically, synonyms in different language
	 * are assumed to be translations.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return translations
	 */
	public static Set<String> findTranslations(String word, POS pos, Lang lang) {
		return WordNetUtil.findSynonyms(word, pos, true, lang);
	}

	/**
	 * Finds definitions of a word. As of Japanese WordNet version 0.9, only
	 * English definitions are available.
	 * 
	 * @param word
	 *            word in English or Japanese
	 * @param pos
	 *            part of speech
	 * @return definitions in English
	 */
	public static Set<String> findDefinitions(String word, POS pos, Lang lang) {
		Set<String> results = new LinkedHashSet<String>();
		List<Synset> synsets = WordNetUtil.wordToSynsets(word, pos, lang);

		for (Synset synset : synsets) {
			SynsetDef def = SynsetDefDAO.findSynsetDefBySynsetAndLang(synset.getSynset(), lang);
			results.add(def.getDef());
		}
		return results;
	}

	private static void run(String word, POS pos, Lang lang) {
		List<WordJAW> words = WordDAO.findWordsByLemmaAndPos(word, pos, lang);
		List<Sense> senses = SenseDAO.findSensesByWordid(words.get(0).getWordid(), lang);
		String synsetId = senses.get(0).getSynset();
		Synset synset = SynsetDAO.findSynsetBySynset(synsetId, lang);
		SynsetDef synsetDef = SynsetDefDAO.findSynsetDefBySynsetAndLang(synsetId, lang);
		List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynset(synsetId, lang);

		System.out.println(words.get(0));
		for (Sense s : senses)
			System.out.println(s);
		System.out.println(synset);
		System.out.println(synsetDef);

		try {
			System.out.println(synlinks.get(0));
		} catch (IndexOutOfBoundsException e) {
			System.out.println("--------------------\nSynlinks not found\n--------------------");
		}

		Set<String> synonyms = JAWJAW.findSynonyms(word, pos, lang);
		Set<String> hypernyms = JAWJAW.findHypernyms(word, pos, lang);
		Set<String> hyponyms = JAWJAW.findHyponyms(word, pos, lang);
		Set<String> meronyms = JAWJAW.findMeronyms(word, pos, lang);
		Set<String> holonyms = JAWJAW.findHolonyms(word, pos, lang);
		Set<String> translations = JAWJAW.findTranslations(word, pos, lang);
		Set<String> definitions = JAWJAW.findDefinitions(word, pos, lang);

		System.out.println("synonyms of " + word + " : \t" + synonyms);
		System.out.println("hypernyms of " + word + " : \t" + hypernyms);
		System.out.println("hyponyms of " + word + " : \t" + hyponyms);
		System.out.println("meronyms of " + word + " : \t" + meronyms);
		System.out.println("holonyms of " + word + " : \t" + holonyms);
		System.out.println("translations of " + word + " : \t" + translations);
		System.out.println("definitions of " + word + " : \t" + definitions);
	}

	public static void main(String[] args) {
		// JAWJAW.run("author", POS.n);
		// JAWJAW.run("maison", POS.n, Lang.fr);
		// JAWJAW.run("voiture", POS.n, Lang.fr);
		JAWJAW.run("automobile", POS.n, Lang.eng);
		JAWJAW.run("automobile", POS.n, Lang.fr);

		JAWJAW.run("cald", POS.a, Lang.ro);
	}

}
