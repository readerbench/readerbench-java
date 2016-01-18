package webService.services;

import java.util.ArrayList;
import java.util.List;

import data.AbstractDocument;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import webService.result.*;

public class TextualComplexity {
	
	/**
	 * Get values for all textual complexity indices applied on the entire
	 * document
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	public static List<ResultSentiment> getComplexityIndices(AbstractDocument queryDoc) {
		List<ResultSentiment> resultsComplexity = new ArrayList<ResultSentiment>();

		List<ResultValence> localResults;
		for (IComplexityFactors complexityClass : ComplexityIndices.TEXTUAL_COMPLEXITY_FACTORS) {
			localResults = new ArrayList<ResultValence>();
			for (int id : complexityClass.getIDs()) {
				localResults.add(new ResultValence(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[id],
						Formatting.formatNumber(queryDoc.getComplexityIndices()[id])));
			}
			resultsComplexity.add(new ResultSentiment(complexityClass.getClassName(), localResults, null));
		}

		return resultsComplexity;
	}

}
