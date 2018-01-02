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
package com.readerbench.services.semanticSearch;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.discourse.SemanticCohesion;
import com.readerbench.services.semanticModels.ISemanticModel;
import com.readerbench.services.semanticModels.SimilarityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class SemanticSearch {

    public static List<SemanticSearchResult> search(AbstractDocument query, List<AbstractDocument> docs,
            double threshold, int noResults) {
        List<SemanticSearchResult> results = new ArrayList<>();
        for (AbstractDocument d : docs) {
            // difference between documents
            EnumMap<SimilarityType, Double> similarities = new EnumMap<>(SimilarityType.class);
            for (ISemanticModel model : query.getSemanticModels()) {
                similarities.put(model.getType(), model.getSimilarity(query.getModelVectors().get(model.getType()), d.getModelVectors().get(model.getType())));
            }

            double sim = SemanticCohesion.getAggregatedSemanticMeasure(similarities);

            if (sim >= threshold) {
                results.add(new SemanticSearchResult(d, sim));
            }
        }
        Collections.sort(results);
        return results.subList(0, Math.min(results.size(), noResults));
    }
}
