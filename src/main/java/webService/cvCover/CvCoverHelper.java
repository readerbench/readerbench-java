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

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import java.util.EnumMap;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

public class CvCoverHelper {

    public static double computeSemanticSimilarity(AbstractDocument document1, AbstractDocument document2) {
        EnumMap<SimilarityType, Double> similarities = new EnumMap<>(SimilarityType.class);
        for (ISemanticModel model : document1.getSemanticModels()) {
            similarities.put(model.getType(), model.getSimilarity(document1.getModelVectors().get(model.getType()), document2.getModelVectors().get(model.getType())));
        }
        return SemanticCohesion.getAggregatedSemanticMeasure(similarities);
    }

}
