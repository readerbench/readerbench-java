package edu.cmu.lti.lexical_db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.cmu.lti.jawjaw.db.SynlinkDAO;
import edu.cmu.lti.jawjaw.db.SynsetDAO;
import edu.cmu.lti.jawjaw.db.SynsetDefDAO;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.SynsetDef;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class NictWordNetDB implements ILexicalDatabase {

	private static ConcurrentMap<String, List<String>> cache;
	private static PorterStemmer stemmer;

	static {
		if (WS4JConfiguration.getInstance().useCache()) {
			cache = new ConcurrentHashMap<String, List<String>>(WS4JConfiguration.getInstance().getMaxCacheSize());
		}
		if (WS4JConfiguration.getInstance().useStem()) {
			stemmer = new PorterStemmer();
		}
	}

	public Collection<Concept> getAllConcepts(String word, String posText, Lang lang) {
		POS pos = POS.valueOf(posText);
		List<edu.cmu.lti.jawjaw.pobj.Synset> synsets = WordNetUtil.wordToSynsets(word, pos, lang);
		List<Concept> synsetStrings = new ArrayList<Concept>(synsets.size());
		for (edu.cmu.lti.jawjaw.pobj.Synset synset : synsets) {
			synsetStrings.add(new Concept(synset.getSynset(), POS.valueOf(pos.toString())));
		}
		return synsetStrings;
	}

	public Collection<String> getHypernyms(String synset, Lang lang) {
		List<Synlink> links = SynlinkDAO.findSynlinksBySynsetAndLink(synset, Link.hype, lang);
		List<String> hypernyms = new ArrayList<String>();
		for (Synlink link : links) {
			hypernyms.add(link.getSynset2());
		}
		return hypernyms;
	}

	public Concept getMostFrequentConcept(String word, String pos, Lang lang) {
		Collection<Concept> concepts = getAllConcepts(word, pos, lang);
		return concepts.size() > 0 ? concepts.iterator().next() : null;
	}

	public Concept findSynsetBySynset(String synset, Lang lang) {
		// TODO Auto-generated method stub
		return null;
	}

	// offset looks like "service#n#3"
	public String conceptToString(String synset, Lang lang) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getGloss(Concept synset, String linkString, Lang lang) {
		String key = synset + " " + linkString + " " + lang;

		if (WS4JConfiguration.getInstance().useCache()) {
			List<String> cachedObj = cache.get(key);
			if (cachedObj != null)
				return clone(cachedObj);
		}

		List<String> linkedSynsets = new ArrayList<String>();
		Link link = null;
		try {
			link = Link.valueOf(linkString);
			if (link.equals(Link.mero)) {
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.mmem, lang));
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.msub, lang));
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.mprt, lang));
			} else if (link.equals(Link.holo)) {
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.hmem, lang));
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.hsub, lang));
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), Link.hprt, lang));
			} else if (link.equals(Link.syns)) {
				linkedSynsets.add(synset.getSynset());
			} else {
				linkedSynsets.addAll(linkToSynsets(synset.getSynset(), link, lang));
			}
		} catch (IllegalArgumentException e) {
			// I know it's not a good use of catching
			// this is how normal gloss is obtained
			linkedSynsets.add(synset.getSynset());
		}

		List<String> glosses = new ArrayList<String>(linkedSynsets.size());
		for (String linkedSynset : linkedSynsets) {
			String gloss = null;
			if (Link.syns.equals(link)) {
				// Special case when you want name assigned to the synset, not
				// the gloss.
				gloss = synset.getName();
				if (gloss == null) {
					gloss = SynsetDAO.findSynsetBySynset(linkedSynset, lang).getName(lang);
				}
			} else {
				// This path is the majority
				SynsetDef synsetDef = SynsetDefDAO.findSynsetDefBySynsetAndLang(linkedSynset, lang);

				/*
				 * Let's separate the gloss and example
				 */
				gloss = WordNetUtil.getGloss(synsetDef);
			}

			if (gloss == null)
				continue;

			// postprocess
			// gloss = gloss.replaceAll("[^a-zA-Z0-9]", " ");
			gloss = gloss.replaceAll("[.;:,?!(){}\"`$%@<>]", " ");
			gloss = gloss.replaceAll("&", " and ");
			gloss = gloss.replaceAll("_", " ");
			gloss = gloss.replaceAll("[ ]+", " ");
			gloss = gloss.replaceAll("(?<!\\w)'", " ");
			gloss = gloss.replaceAll("'(?!\\w)", " ");
			gloss = gloss.replaceAll("--", " ");
			gloss = gloss.toLowerCase();

			if (WS4JConfiguration.getInstance().useStem()) {
				gloss = stemmer.stemSentence(gloss);
			}

			glosses.add(gloss);
		}

		if (WS4JConfiguration.getInstance().useCache()) {
			// synchronized ( cache ) {
			if (cache.size() >= WS4JConfiguration.getInstance().getMaxCacheSize()) {
				cache.remove(cache.keySet().iterator().next());
			}
			if (glosses != null)
				cache.put(key, clone(glosses));
			// }
		}
		return glosses;
	}

	/**
	 * Create the copied instance
	 * 
	 * @param original
	 * @return clone
	 */
	private List<String> clone(List<String> original) {
		return new ArrayList<String>(original);
	}

	private List<String> linkToSynsets(String synset, Link link, Lang lang) {
		List<String> linkedSynsets = new ArrayList<String>();
		List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynsetAndLink(synset, link, lang);
		for (Synlink synlink : synlinks) {
			linkedSynsets.add(synlink.getSynset2());
		}
		return linkedSynsets;
	}
}
