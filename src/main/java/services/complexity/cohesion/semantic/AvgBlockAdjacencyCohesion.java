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
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.WordNet.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgBlockAdjacencyCohesion extends ComplexityIndex{

    public AvgBlockAdjacencyCohesion(SimilarityType simType) {
        super(ComplexityIndecesEnum.AVERAGE_BLOCK_ADJACENCY_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        int no = 0;
		double sum = 0;
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				if (d.getPrunnedBlockDistances() != null && d.getPrunnedBlockDistances()[i][i + 1] != null
						&& d.getPrunnedBlockDistances()[i][i + 1].getCohesion() > 0) {
					sum += d.getPrunnedBlockDistances()[i][i + 1].getSemanticSimilarities().get(simType);
					no++;
				}
			}
		}
		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
    }
}
