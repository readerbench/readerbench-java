package services.semanticModels.WordNet;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import data.Word;
import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.jawjaw.db.SenseDAO;
import edu.cmu.lti.jawjaw.db.WordDAO;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Sense;
import edu.cmu.lti.jawjaw.pobj.WordJAW;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNetDB;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;

public class OntologySupport {
	static Logger logger = Logger.getLogger(OntologySupport.class);
	private static int id = 0;

	public static final int LEACOCK_CHODOROW = id++;
	public static final int WU_PALMER = id++;
	public static final int PATH_SIM = id++;
	public static final int NO_SIMILARITIES = id;

	public static final double SYNONYM_WEIGHT = 1.0;
	public static final double HYPERNYM_WEIGHT = 1.0;
	private static final double[] THRESHOLDS = { 1, 1, 1 };
	private static final int MAX_NO_SYNONYMS = 2;
	private static final int MAX_NO_HYPERNYMS = 1;

	private static ILexicalDatabase wn = new NictWordNetDB();
	private static RelatednessCalculator[] related_en = { new LeacockChodorow(wn, Lang.eng), new WuPalmer(wn, Lang.eng),
			new Path(wn, Lang.eng) };
	private static RelatednessCalculator[] related_fr = { new LeacockChodorow(wn, Lang.fr), new WuPalmer(wn, Lang.fr),
			new Path(wn, Lang.fr) };

	public static POS getPOS(String posTag) {
		if (posTag == null) {
			return null;
		}
		if (posTag.startsWith("NN")) {
			return POS.n;
		}
		if (posTag.startsWith("VB")) {
			return POS.v;
		}
		if (posTag.startsWith("RB")) {
			return POS.r;
		}
		if (posTag.startsWith("JJ")) {
			return POS.a;
		}
		return null;
	}

	public static double semanticSimilarity(Word w1, Word w2, int type) {
		if (!w1.getLanguage().equals(w2.getLanguage()))
			return 0;
		if (w1 == null || w2 == null || w1.getPOS() == null || w2.getPOS() == null || !w1.getPOS().equals(w2.getPOS()))
			return 0;
		String word1 = w1.getLemma() + "#" + getPOS(w1.getPOS());
		String word2 = w2.getLemma() + "#" + getPOS(w2.getPOS());
		double sim = 0;
		switch (w1.getLanguage()) {
		case fr:
			sim = related_fr[type].calcRelatednessOfWords(word1, word2);
		default:
			sim = related_en[type].calcRelatednessOfWords(word1, word2);
		}

		if (sim > THRESHOLDS[type]) {
			sim = THRESHOLDS[type];
		}
		return sim;
	}

	public static boolean haveCommonElements(Set<Sense> set1, Set<Sense> set2) {
		for (Sense s1 : set1)
			if (set2.contains(s1))
				return true;
		return false;
	}

	public static boolean areSynonyms(Sense s1, Sense s2, Lang language) {
		Set<Sense> synonyms1 = JAWJAW.findSynonyms(s1, language);
		Set<Sense> synonyms2 = JAWJAW.findSynonyms(s2, language);

		return haveCommonElements(synonyms1, synonyms2);
		// return synonyms1.contains(s2) || synonyms2.contains(s1);
	}

	public static boolean areSynonyms(Word w1, Word w2, Lang language) {
		if (!w1.getPOS().equals(w2.getPOS()))
			return false;
		if (getPOS(w1.getPOS()) == null || getPOS(w2.getPOS()) == null)
			return false;
		Set<String> synonyms1 = JAWJAW.findSynonyms(w1.getLemma(), getPOS(w1.getPOS()), language);

		Set<String> synonyms2 = JAWJAW.findSynonyms(w2.getLemma(), getPOS(w2.getPOS()), language);

		return synonyms1.contains(w2.getLemma()) || synonyms2.contains(w1.getLemma());
	}

	public static boolean areAntonyms(Sense s1, Sense s2, Lang language) {
		Set<Sense> synonyms1 = JAWJAW.findSynonyms(s1, language);
		Set<Sense> synonyms2 = JAWJAW.findSynonyms(s2, language);
		Set<Sense> antonyms1 = JAWJAW.findSeeAntonyms(s1, language);
		Set<Sense> antonyms2 = JAWJAW.findSeeAntonyms(s2, language);

		return haveCommonElements(synonyms1, antonyms2) || haveCommonElements(antonyms1, synonyms2);
		// return antonyms2.contains(s1) || antonyms1.contains(s2);
	}

	public static boolean areHypernym(Sense s1, Sense s2, Lang language) {
		Set<Sense> synonyms1 = JAWJAW.findSynonyms(s1, language);
		Set<Sense> synonyms2 = JAWJAW.findSynonyms(s2, language);
		Set<Sense> hyponyms1 = JAWJAW.findHyponyms(s1, language);
		Set<Sense> hypernyms2 = JAWJAW.findHypernyms(s2, language);

		return haveCommonElements(synonyms1, hypernyms2) || haveCommonElements(hyponyms1, synonyms2);
		// return hypernyms2.contains(s1) || hyponyms1.contains(s2);
	}

	public static boolean areHyponym(Sense s1, Sense s2, Lang language) {
		Set<Sense> synonyms1 = JAWJAW.findSynonyms(s1, language);
		Set<Sense> synonyms2 = JAWJAW.findSynonyms(s2, language);
		Set<Sense> hypernyms1 = JAWJAW.findHypernyms(s1, language);
		Set<Sense> hyponyms2 = JAWJAW.findHyponyms(s2, language);

		return haveCommonElements(synonyms1, hyponyms2) || haveCommonElements(hypernyms1, synonyms2);
		// return hyponyms2.contains(s1) || hypernyms1.contains(s2);
	}

	public static boolean areSiblings(Sense s1, Sense s2, Lang language) {
		Set<Sense> synonyms1 = JAWJAW.findSynonyms(s1, language);
		Set<Sense> synonyms2 = JAWJAW.findSynonyms(s2, language);
		Set<Sense> siblingsSet1 = getSiblingSet(s1, language);
		Set<Sense> siblingsSet2 = getSiblingSet(s2, language);

		return haveCommonElements(synonyms1, siblingsSet2) || haveCommonElements(siblingsSet1, synonyms2);
		// return siblingsSet1.contains(s2) || siblingsSet2.contains(s1);
	}

	private static Set<Sense> getSiblingSet(Sense sense, Lang language) {
		Set<Sense> siblingsSet = new TreeSet<Sense>();
		Set<Sense> hypernyms = JAWJAW.findHypernyms(sense, language);
		if (hypernyms != null && hypernyms.size() > 0) {
			for (Sense hypernym : hypernyms) {
				Set<Sense> hyponyms = JAWJAW.findHyponyms(hypernym, language);
				if (hyponyms != null) {
					siblingsSet.addAll(hyponyms);
				}
			}
		}

		return siblingsSet;
	}

	public static boolean exists(String word, String pos, Lang lang) {
		List<WordJAW> words = WordDAO.findWordsByLemmaAndPos(word, getPOS(pos), lang);
		return (words != null && words.size() > 0);
	}

	public static TreeMap<Word, Double> getSimilarConcepts(Word word) {
		if (word.getPOS() == null)
			return null;
		TreeMap<Word, Double> results = new TreeMap<Word, Double>();
		Set<String> synonyms = JAWJAW.findSynonyms(word.getLemma(), getPOS(word.getPOS()), word.getLanguage());
		int no = 0;
		for (String s : synonyms) {
			if (!StopWords.isStopWord(s, word.getLanguage()) && Dictionary.isDictionaryWord(s, word.getLanguage())) {
				results.put(new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
						word.getLanguage()), SYNONYM_WEIGHT);
				no++;
			}
			if (no >= MAX_NO_SYNONYMS)
				break;
		}
		no = 0;
		Set<String> hypernyms = JAWJAW.findHypernyms(word.getLemma(), getPOS(word.getPOS()), word.getLanguage());
		for (String s : hypernyms) {
			Word newWord = new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
					word.getLanguage());
			if (results.containsKey(newWord) && !StopWords.isStopWord(s, word.getLanguage())
					&& Dictionary.isDictionaryWord(s, word.getLanguage())) {
				results.put(newWord, HYPERNYM_WEIGHT);
				no++;
			}
			if (no >= MAX_NO_HYPERNYMS)
				break;
		}

		return results;
	}

	public static Set<Sense> getWordSenses(Word word) {
		List<WordJAW> words = null;
		if (getPOS(word.getPOS()) != null) {
			words = WordDAO.findWordsByLemmaAndPos(word.getLemma(), getPOS(word.getPOS()), word.getLanguage());
		} else {
			words = WordDAO.findWordsByLemma(word.getLemma(), word.getLanguage());
		}
		if (words == null || words.size() == 0)
			return null;
		Set<Sense> results = new TreeSet<Sense>();
		for (WordJAW w : words) {
			List<Sense> senses = SenseDAO.findSensesByWordid(w.getWordid(), w.getLang());
			if (senses != null && senses.size() > 0)
				results.addAll(senses);
		}
		return results;
	}

	public static void main(String[] args) {
		System.out.println(semanticSimilarity(Word.getWordFromConcept("dog_NN", Lang.eng),
				Word.getWordFromConcept("cat_NN", Lang.eng), OntologySupport.PATH_SIM));
	}
}
