package services.readingStrategies;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import services.nlp.listOfWords.ListOfWords;
import DAO.AnalysisElement;
import DAO.Sentence;

/**
 * 
 * @author Mihai Dascalu
 */
public class PatternMatching {
	static Logger logger = Logger.getLogger(PatternMatching.class);

	public static final ListOfWords PATTERNS_CAUSALITY_FR = new ListOfWords(
			"resources/config/ReadingStrategies/causality_fr.txt");
	public static final ListOfWords PATTERNS_CONTROL_FR = new ListOfWords(
			"resources/config/ReadingStrategies/control_fr.txt");
	private static final Color COLOR_CAUSALITY = new Color(255, 0, 255);
	private static final Color COLOR_CONTROL = new Color(0, 203, 255);

	public static enum Strategy {
		CAUSALITY, CONTROL
	};

	// returns the number of occurrences
	public static int containsStrategy(List<Sentence> sentences,
			AnalysisElement el, Strategy strategy, boolean alreadyExistentCheck) {
		String text = " " + el.getAlternateText() + " ";
		int no_occurences = 0;
		ListOfWords usedList = null;
		String usedColor = null;
		switch (strategy) {
		case CAUSALITY:
			usedColor = Integer.toHexString(COLOR_CAUSALITY.getRGB());
			usedColor = usedColor.substring(2, usedColor.length());
			switch (el.getLanguage()) {
			case fr:
				usedList = PATTERNS_CAUSALITY_FR;
				break;
			default:
				break;
			}
			break;
		case CONTROL:
			usedColor = Integer.toHexString(COLOR_CONTROL.getRGB());
			usedColor = usedColor.substring(2, usedColor.length());
			switch (el.getLanguage()) {
			case fr:
				usedList = PATTERNS_CONTROL_FR;
				break;
			default:
				break;
			}
			break;
		}

		if (usedList != null && strategy.equals(Strategy.CONTROL)) {
			for (String pattern : usedList.getWords()) {
				// check that the pattern does not exist in any of the previous
				// sentences
				boolean exists = false;
				if (alreadyExistentCheck) {
					for (Sentence s : sentences) {
						if (s.getText().contains(" " + text.trim() + " ")) {
							exists = true;
							break;
						}
					}
				}
				if (!exists) {
					no_occurences += StringUtils.countMatches(" " + text.trim()
							+ " ", " " + pattern + " ");
					text = colorText(text.trim(), pattern, usedColor);
				}
			}
		}
		if (usedList != null && strategy.equals(Strategy.CAUSALITY)) {
			for (String pattern : usedList.getWords()) {
				// check that the pattern does not exist in any of the previous
				// sentences
				boolean exists = false;
				if (alreadyExistentCheck) {
					for (Sentence s : sentences) {
						if (s.getText().contains(" " + text.trim() + " ")) {
							exists = true;
							break;
						}
					}
				}
				if (!exists) {
					if (text.trim().startsWith(pattern + " ")) {
						no_occurences += StringUtils.countMatches(" "
								+ text.trim().substring(pattern.length() + 1)
										.trim() + " ", " " + pattern + " ");
						text = pattern
								+ " "
								+ colorText(
										text.trim()
												.substring(pattern.length() + 1)
												.trim(), pattern, usedColor);
					} else {
						no_occurences += StringUtils.countMatches(
								" " + text.trim() + " ", " " + pattern + " ");
						text = colorText(text.trim(), pattern, usedColor);
					}
					// recheck just to be sure
					no_occurences += StringUtils.countMatches(" " + text.trim()
							+ " ", " " + pattern + " ");
					text = colorText(text.trim(), pattern, usedColor);
				}
			}
		}
		el.setAlternateText(text.trim());
		return no_occurences;
	}

	public static String colorText(String text, String pattern, String color) {
		String phrase = " " + text + " ";
		phrase = phrase.replaceAll(" " + pattern + " ", " <font color=\""
				+ color + "\"><b>" + pattern + "</b></font> ");
		return phrase.trim();
	}

	public static String underlineIntalicsText(String text, String color) {
		return " <font color=\"" + color
				+ "\" style=\"text-decoration: underline, italics;\">"
				+ text.trim() + "</font>";
	}

	public static String colorTextIndex(String text, String pattern,
			String color, int index) {
		String phrase = " " + text + " ";
		String replacement = " <font color=\"" + color + "\"><b>" + pattern
				+ "[" + index + "]</b></font> ";
		phrase = phrase.replaceAll(" " + pattern + " ", replacement);
		return phrase.trim();
	}

	public static String colorTextStar(String text, String pattern,
			String color, String annotationText) {
		String phrase = " " + text + " ";
		String replacement = " <font color=\"" + color + "\"><b>" + pattern
				+ "[" + annotationText + "*]</b></font> ";
		phrase = phrase.replaceAll(" " + pattern + " ", replacement);
		return phrase.trim();
	}
}
