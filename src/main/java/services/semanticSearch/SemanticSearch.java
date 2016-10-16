/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.semanticSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import cc.mallet.util.Maths;
import services.commons.VectorAlgebra;
import services.semanticModels.SemanticModel;

public class SemanticSearch {

	public static List<SemanticSearchResult> search(AbstractDocument query, List<AbstractDocument> docs,
			double threshold, int noResults) {
		List<SemanticSearchResult> results = new ArrayList<SemanticSearchResult>();
		for (AbstractDocument d : docs) {
			// difference between documents
			double lsaSim = 0;
			double ldaSim = 0;
			if (query.getSemanticModel(SemanticModel.LSA) != null && 
                    d.getSemanticModel(SemanticModel.LSA) != null)
				lsaSim = VectorAlgebra.cosineSimilarity(query.getLSAVector(), d.getLSAVector());
			if (query.getSemanticModel(SemanticModel.LDA) != null && 
                    d.getSemanticModel(SemanticModel.LDA) != null)
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
