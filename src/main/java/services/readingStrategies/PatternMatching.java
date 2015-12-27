package services.readingStrategies;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import data.AnalysisElement;
import data.Sentence;
import services.nlp.listOfWords.ListOfWords;

/**
 * 
 * @author Mihai Dascalu
 */
public class PatternMatching {
	static Logger logger = Logger.getLogger(PatternMatching.class);

	public static final ListOfWords PATTERNS_CAUSALITY_FR = new ListOfWords(
			"resources/config/ReadingStrategies/causality_fr.txt");
	public static final ListOfWords PATTERNS_METACOGNITION_FR = new ListOfWords(
			"resources/config/ReadingStrategies/metacognition_fr.txt");
	public static final ListOfWords PATTERNS_CAUSALITY_EN = new ListOfWords(
			"resources/config/ReadingStrategies/causality_en.txt");
	public static final ListOfWords PATTERNS_METACOGNITION_EN = new ListOfWords(
			"resources/config/ReadingStrategies/metacognition_en.txt");
	private static final Color COLOR_CAUSALITY = new Color(255, 0, 255);
	private static final Color COLOR_METACOGNITION = new Color(0, 203, 255);

	// returns the number of occurrences
	public static int containsStrategy(List<Sentence> sentences, AnalysisElement el, int strategy,
			boolean alreadyExistentCheck) {
		String text = " " + el.getAlternateText() + " ";
		int no_occurences = 0;
		ListOfWords usedList = null;
		String usedColor = null;
		switch (strategy) {
		case ReadingStrategies.CAUSALITY:
			usedColor = Integer.toHexString(COLOR_CAUSALITY.getRGB());
			usedColor = usedColor.substring(2, usedColor.length());
			switch (el.getLanguage()) {
			case eng:
				usedList = PATTERNS_CAUSALITY_EN;
				break;
			case fr:
				usedList = PATTERNS_CAUSALITY_FR;
				break;
			default:
				break;
			}
			break;
		case ReadingStrategies.META_COGNITION:
			usedColor = Integer.toHexString(COLOR_METACOGNITION.getRGB());
			usedColor = usedColor.substring(2, usedColor.length());
			switch (el.getLanguage()) {
			case eng:
				usedList = PATTERNS_METACOGNITION_EN;
				break;
			case fr:
				usedList = PATTERNS_METACOGNITION_FR;
				break;
			default:
				break;
			}
			break;
		}

		if (usedList != null && strategy == ReadingStrategies.META_COGNITION) {
			for (String pattern : usedList.getWords()) {
				// check that the pattern does not exist in any of the previous
				// sentences
				boolean exists = false;
				if (alreadyExistentCheck) {
					for (Sentence s : sentences) {
						Pattern javaPattern = Pattern.compile(" " + pattern + " ");
						Matcher matcher = javaPattern.matcher(" " + s.getText() + " ");
						if (matcher.find()) {
							exists = true;
							break;
						}
					}
				}
				if (!exists) {
					Pattern javaPattern = Pattern.compile(" " + pattern + " ");
					Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
					while (matcher.find())
						no_occurences++;
					text = colorText(text.trim(), pattern, usedColor);
				}
			}
		}
		if (usedList != null && strategy == ReadingStrategies.CAUSALITY) {
			for (String pattern : usedList.getWords()) {
				if (text.trim().startsWith(pattern + " ")) {
					Pattern javaPattern = Pattern.compile(" " + pattern + " ");
					Matcher matcher = javaPattern
							.matcher(" " + text.trim().substring(pattern.length() + 1).trim() + " ");
					while (matcher.find())
						no_occurences++;
					text = pattern + " "
							+ colorText(text.trim().substring(pattern.length() + 1).trim(), pattern, usedColor);
				} else {
					Pattern javaPattern = Pattern.compile(" " + pattern + " ");
					Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
					while (matcher.find())
						no_occurences++;
					text = colorText(text.trim(), pattern, usedColor);
				}
				// recheck just to be sure
				Pattern javaPattern = Pattern.compile(" " + pattern + " ");
				Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
				while (matcher.find())
					no_occurences++;
				text = colorText(text.trim(), pattern, usedColor);
			}
		}
		el.setAlternateText(text.trim());
		return no_occurences;
	}

	public static String colorText(String text, String pattern, String color) {
		String phrase = " " + text + " ";
		phrase = phrase.replaceAll(" " + pattern + " ",
				" <font color=\"" + color + "\"><b>" + pattern + "</b></font> ");
		return phrase.trim();
	}

	public static String underlineIntalicsText(String text, String color) {
		return " <font color=\"" + color + "\" style=\"text-decoration: underline, italics;\">" + text.trim()
				+ "</font>";
	}

	public static String colorTextIndex(String text, String pattern, String color, int index) {
		String phrase = " " + text + " ";
		String replacement = " <font color=\"" + color + "\"><b>" + pattern + "[" + index + "]</b></font> ";
		phrase = phrase.replaceAll(" " + pattern + " ", replacement);
		return phrase.trim();
	}

	public static String colorTextStar(String text, String pattern, String color, String annotationText) {
		String phrase = " " + text + " ";
		String replacement = " <font color=\"" + color + "\"><b>" + pattern + "[" + annotationText + "*]</b></font> ";
		phrase = phrase.replaceAll(" " + pattern + " ", replacement);
		return phrase.trim();
	}
}
