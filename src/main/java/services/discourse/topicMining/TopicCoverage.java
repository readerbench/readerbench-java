package services.discourse.topicMining;

import org.apache.log4j.Logger;

import services.nlp.listOfWords.MapOfWordWeights;
import services.readingStrategies.PatternMatching;
import DAO.AnalysisElement;
import DAO.Word;
import DAO.discourse.SemanticChain;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class TopicCoverage {

	static Logger logger = Logger.getLogger(PatternMatching.class);

	public static final MapOfWordWeights WORDS_ACADEMIC_ADMINISTRATION_EN = new MapOfWordWeights(
			"config/Topics/academic administration_en.txt", Lang.eng);
	public static final MapOfWordWeights WORDS_EDUCATIONAL_SCIENCES_EN = new MapOfWordWeights(
			"config/Topics/educational sciences_en.txt", Lang.eng);

	public static enum TopicClass {
		ACADEMIC_ADMINISTRATION, EDUCATIONAL_SCIENCES
	};

	// returns the coverage with the predefined topic class
	public static double coverage(AnalysisElement el, TopicClass strategy) {
		if (strategy == null) {
			return 1.0;
		}
		double coverage = 0;
		MapOfWordWeights usedMap = null;
		Lang usedLang = null;
		switch (strategy) {
		case ACADEMIC_ADMINISTRATION:
			switch (el.getLanguage()) {
			case eng:
				usedMap = WORDS_ACADEMIC_ADMINISTRATION_EN;
				usedLang = Lang.eng;
				break;
			default:
				break;
			}
			break;
		case EDUCATIONAL_SCIENCES:
			switch (el.getLanguage()) {
			case eng:
				usedMap = WORDS_EDUCATIONAL_SCIENCES_EN;
				usedLang = Lang.eng;
				break;
			default:
				break;
			}
			break;
		}

		if (usedMap != null && el != null && usedLang.equals(el.getLanguage())) {
			int noOccurrences = 0, totalOccurrences = 0;
			for (Word w : el.getWordOccurences().keySet()) {
				totalOccurrences += el.getWordOccurences().get(w);
				if (usedMap.getWords().containsKey(w.getLemma())) {
					noOccurrences += usedMap.getWords().get(w.getLemma())
							* el.getWordOccurences().get(w);
				}
			}
			if (totalOccurrences != 0) {
				coverage = ((double) noOccurrences) / totalOccurrences;
			}
		}
		return coverage;
	}

	public static double coverage(SemanticChain semChain, TopicClass strategy) {
		if (strategy == null) {
			return 1.0;
		}
		double coverage = 0;
		MapOfWordWeights usedMap = null;
		Lang usedLang = null;
		switch (strategy) {
		case ACADEMIC_ADMINISTRATION:
			switch (semChain.getLSA().getLanguage()) {
			case eng:
				usedMap = WORDS_ACADEMIC_ADMINISTRATION_EN;
				usedLang = Lang.eng;
				break;
			default:
				break;
			}
			break;
		case EDUCATIONAL_SCIENCES:
			switch (semChain.getLSA().getLanguage()) {
			case eng:
				usedMap = WORDS_EDUCATIONAL_SCIENCES_EN;
				usedLang = Lang.eng;
				break;
			default:
				break;
			}
			break;
		}

		if (usedMap != null && semChain != null
				&& usedLang.equals(semChain.getLSA().getLanguage())
				&& usedLang.equals(semChain.getLDA().getLanguage())) {
			int noOccurrences = 0, totalOccurrences = 0;
			for (Word w : semChain.getWords()) {
				totalOccurrences++;
				if (usedMap.getWords().containsKey(w.getLemma())) {
					noOccurrences += usedMap.getWords().get(w.getLemma());
				}
			}
			if (totalOccurrences != 0) {
				coverage = ((double) noOccurrences) / totalOccurrences;
			}
		}
		return coverage;
	}
}
