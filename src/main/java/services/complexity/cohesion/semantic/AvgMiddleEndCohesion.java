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
package services.complexity.cohesion.semantic;

import data.AbstractDocument;
import data.Block;
import data.discourse.SemanticCohesion;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.WordNet.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgMiddleEndCohesion extends ComplexityIndex{

    public AvgMiddleEndCohesion(SimilarityType simType) {
        super(ComplexityIndecesEnum.AVERAGE_MIDDLE_END_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        double no = 0;
		double sum = 0;
		Block endBlock = null;
		int startIndex = 0;
		int endIndex = d.getBlocks().size() - 1;
		for (; startIndex < d.getBlocks().size(); startIndex++) {
			Block b = d.getBlocks().get(startIndex);
			if (b != null) {
				break;
			}
		}
		for (; endIndex >= 0; endIndex--) {
			Block b = d.getBlocks().get(endIndex);
			if (b != null) {
				endBlock = b;
				break;
			}
		}

		for (int i = startIndex + 1; i < endIndex; i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				SemanticCohesion coh = new SemanticCohesion(b, endBlock);
				sum += coh.getSemanticSimilarities().get(simType) / (endIndex - i);
				no += 1D / (endIndex - i);
			}
		}

		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}
    
}