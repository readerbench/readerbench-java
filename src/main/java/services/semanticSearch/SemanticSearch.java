package services.semanticSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.AbstractDocument;
import DAO.discourse.SemanticCohesion;
import cc.mallet.util.Maths;
import services.commons.VectorAlgebra;

public class SemanticSearch {

	public static List<SemanticSearchResult> search(AbstractDocument query, List<AbstractDocument> docs,
			double threshold, int noResults) {
		List<SemanticSearchResult> results = new ArrayList<SemanticSearchResult>();
		for (AbstractDocument d : docs) {
			// difference between documents
			double lsaSim = 0;
			double ldaSim = 0;
			if (query.getLSA() != null && d.getLSA() != null)
				lsaSim = VectorAlgebra.cosineSimilarity(query.getLSAVector(), d.getLSAVector());
			if (query.getLDA() != null && d.getLDA() != null)
				ldaSim = 1 - Maths.jensenShannonDivergence(query.getLDAProbDistribution(), d.getLDAProbDistribution());
			double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);

			if (sim >= threshold) {
				results.add(new SemanticSearchResult(d, sim));
			}
		}
		Collections.sort(results);
		return results.subList(0, Math.min(results.size(), noResults));
	}
}
