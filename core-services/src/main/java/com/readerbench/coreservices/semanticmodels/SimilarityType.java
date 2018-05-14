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
package com.readerbench.coreservices.semanticmodels;

import cc.mallet.util.Maths;
import com.readerbench.coreservices.commons.VectorAlgebra;
import static com.readerbench.coreservices.semanticmodels.SimilarityType.LDA;
import static com.readerbench.coreservices.semanticmodels.SimilarityType.LSA;
import com.readerbench.datasourceprovider.pojo.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.spark.ml.feature.Word2VecModel;

/**
 *
 * @author Stefan
 */
public enum SimilarityType {
    LEACOCK_CHODOROW("LeackockChodorow", "Leackock-Chodorow semantic distance in WordNet", null, false),
    WU_PALMER("WuPalmer", "Wu-Palmer semantic distance in WordNet", null, false),
    PATH_SIM("Path", "Inverse path length in WordNet", null, false),
    LSA("LSA", "Cosine similarity in LSA vector space", VectorAlgebra::cosineSimilarity, true),
    LDA("LDA", "Inverse JSH in LDA probability distribution", (v1, v2) -> 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(v1), VectorAlgebra.normalize(v2)), true),
    WORD2VEC("word2vec", "Cosine similarity in word2vec space", VectorAlgebra::cosineSimilarity, true);

    private final String acronym;
    private final String name;
    private final BiFunction<double[], double[], Double> similarityFuction;
    private final boolean loadable;

    private SimilarityType(String acronym, String name, BiFunction<double[], double[], Double> similarityFuction, boolean loadable) {
        this.acronym = acronym;
        this.name = name;
        this.similarityFuction = similarityFuction;
        this.loadable = loadable;
    }

    public String getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }

    public boolean isLoadable() {
        return loadable;
    }

    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return similarityFuction;
    }

    public static List<SemanticModel> loadVectorModels(Map<SimilarityType, String> modelPaths, Lang lang) {
        // load also LSA vector space and LDA model
        List<SemanticModel> models = new ArrayList<>();
        if (modelPaths != null) {
            modelPaths.entrySet().stream().map((e) -> SemanticModel.loadModel(e.getValue(), lang, e.getKey())).filter((model) -> (model != null)).forEachOrdered((model) -> {
                models.add(model);
            });
        }
        return models;
    }
}
