package services.discourse.topicMining;

import org.apache.log4j.Logger;

import data.AnalysisElement;
import data.Word;
import data.Lang;
import services.nlp.listOfWords.MapOfWordWeights;
import services.readingStrategies.PatternMatching;

public class TopicCoverage {

	static Logger logger = Logger.getLogger(PatternMatching.class);

	public static final MapOfWordWeights WORDS_ACADEMIC_ADMINISTRATION_EN = new MapOfWordWeights(
			"resources/config/Topics/academic administration_en.txt", Lang.eng);
	public static final MapOfWordWeights WORDS_EDUCATIONAL_SCIENCES_EN = new MapOfWordWeights(
			"resources/config/Topics/educational sciences_en.txt", Lang.eng);

	// returns the coverage with the predefined topic class
	public static double coverage(MapOfWordWeights usedMap, AnalysisElement el) {
		double coverage = 0;

		if (usedMap != null && el != null) {
			int noOccurrences = 0, totalOccurrences = 0;
			for (Word w : el.getWordOccurences().keySet()) {
				totalOccurrences += el.getWordOccurences().get(w);
				if (usedMap.getWords().containsKey(w.getLemma())) {
					noOccurrences += usedMap.getWords().get(w.getLemma()) * el.getWordOccurences().get(w);
				}
			}
			if (totalOccurrences != 0) {
				coverage = ((double) noOccurrences) / totalOccurrences;
			}
		}
		return coverage;
	}
}
