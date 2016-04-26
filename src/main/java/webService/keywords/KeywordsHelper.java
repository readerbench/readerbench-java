package webService.keywords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import webService.query.QueryHelper;
import webService.result.ResultKeyword;

public class KeywordsHelper {
	
	public static List<ResultKeyword> getKeywords(String documentKeywords, String documentContent, String pathToLSA,
			String pathToLDA, Lang lang, boolean usePOSTagging, boolean computeDialogism, double threshold) {
		
		ArrayList<ResultKeyword> resultKeywords = new ArrayList<ResultKeyword>();

		AbstractDocument queryDoc = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging,
				computeDialogism);
		AbstractDocument queryKey = QueryHelper.processQuery(documentKeywords, pathToLSA, pathToLDA, lang, usePOSTagging,
				computeDialogism);
		queryKey.computeAll(computeDialogism, null, null);

		for (Word keyword : queryKey.getWordOccurences().keySet()) {
			AbstractDocument queryKeyword = QueryHelper.processQuery(keyword.getLemma(), pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			SemanticCohesion sc = new SemanticCohesion(queryKeyword, queryDoc);
			int occ = 0;
			if (queryDoc.getWordOccurences().containsKey(keyword)) {
				occ = queryDoc.getWordOccurences().get(keyword).intValue();
			}
			resultKeywords.add(new ResultKeyword(keyword.getLemma(), occ, Formatting.formatNumber(sc.getCohesion())));
		}

		Collections.sort(resultKeywords, ResultKeyword.ResultKeywordRelevanceComparator);
		return resultKeywords;
	}

}
