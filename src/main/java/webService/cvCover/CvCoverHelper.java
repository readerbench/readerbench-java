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
package webService.cvCover;

import cc.mallet.util.Maths;
import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import services.commons.VectorAlgebra;

public class CvCoverHelper {

	public static double computeSemanticSimilarity(
			AbstractDocument document1,
			AbstractDocument document2
		) {
		
		double
			lsaSim = 0,
			ldaSim = 0;
		if (document1.getLSA() != null && document2.getLSA() != null)
			lsaSim = VectorAlgebra.cosineSimilarity(document1.getLSAVector(), document2.getLSAVector());
		if (document1.getLDA() != null && document2.getLDA() != null)
			ldaSim = 1 - Maths.jensenShannonDivergence(document1.getLDAProbDistribution(), document2.getLDAProbDistribution());
		return SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
	}
	
}
