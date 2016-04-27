package webService.keywords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import services.nlp.listOfWords.ListOfWords;
import services.readingStrategies.PatternMatching;
import services.readingStrategies.ReadingStrategies;
import webService.query.QueryHelper;
import webService.result.ResultKeyword;

public class KeywordsHelper {
	
	public static List<ResultKeyword> getKeywords(
			AbstractDocument document,  AbstractDocument keywordsDocument, Set<String> keywords,
			String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging, boolean computeDialogism, double threshold) {
		
		ArrayList<ResultKeyword> resultKeywords = new ArrayList<ResultKeyword>();
		
		ListOfWords usedList = new ListOfWords();
		usedList.setWords(keywords);
		
		for (String pattern : usedList.getWords()) {
			int occ = 0;
			Pattern javaPattern = Pattern.compile(" " + pattern + " ");
			Matcher matcher = javaPattern.matcher(" " + document.getText().trim() + " ");
			SemanticCohesion sc = new SemanticCohesion(keywordsDocument, document);
			while (matcher.find())
				occ++;
			resultKeywords.add(new ResultKeyword(pattern, occ, Formatting.formatNumber(sc.getCohesion())));
		}

		Collections.sort(resultKeywords, ResultKeyword.ResultKeywordRelevanceComparator);
		return resultKeywords;
	}

}
