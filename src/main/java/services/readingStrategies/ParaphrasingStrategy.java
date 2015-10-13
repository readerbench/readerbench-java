package services.readingStrategies;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

import services.semanticModels.WordNet.OntologySupport;
import DAO.AnalysisElement;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.discourse.SemanticCohesion;

public class ParaphrasingStrategy {
	private static final Color COLOR_PARAPHRASING = new Color(0, 100, 17);
	private static final double SIMILARITY_THRESHOLD_PARAPHRASING = 0.2d;

	private int addAssociations(Word word, AnalysisElement e, String usedColor) {
		word.getReadingStrategies()[ReadingStrategies.PARAPHRASE] = true;
		int noOccurences = StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(PatternMatching.colorText(e.getAlternateText(), word.getText(), usedColor));
		// recheck just to be sure
		noOccurences += StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(PatternMatching.colorText(e.getAlternateText(), word.getText(), usedColor));

		if (noOccurences > 0)
			return 1;
		return noOccurences;
	}

	public void conceptsInCommon(Block v, Sentence s) {
		// if above a minimum threshold
		// determine similarity
		SemanticCohesion sim = new SemanticCohesion(v, s);
		if (sim.getCohesion() < SIMILARITY_THRESHOLD_PARAPHRASING)
			return;

		String usedColor = Integer.toHexString(COLOR_PARAPHRASING.getRGB());
		usedColor = usedColor.substring(2, usedColor.length());

		for (Word w1 : v.getWordOccurences().keySet()) {
			boolean hasAssociations = false;
			for (Word w2 : s.getWordOccurences().keySet()) {
				// check for identical lemmas or synonyms
				if (w1.getLemma().equals(w2.getLemma()) || w1.getStem().equals(w2.getStem())
						|| OntologySupport.areSynonyms(w1, w2, v.getLanguage())) {
					hasAssociations = true;
					addAssociations(w2, s, usedColor);
				}
			}
			if (hasAssociations && !w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]) {
				addAssociations(w1, v, usedColor);
			}
		}
	}
}
