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
package services.semanticModels;

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.paragraphVectors.ParagraphVectorsModel;
import services.semanticModels.word2vec.Word2VecModel;

/**
 *
 * @author Stefan
 */
public enum SimilarityType {
    LEACOCK_CHODOROW("LeackockChodorow", "Leackock-Chodorow semantic distance in WordNet", OntologySupport::getAvailableLanguages, false),
    WU_PALMER("WuPalmer", "Wu-Palmer semantic distance in WordNet", OntologySupport::getAvailableLanguages, false),
    PATH_SIM("Path", "Inverse path length in WordNet", OntologySupport::getAvailableLanguages, false),
    LSA("LSA", "Cosine similarity in LSA vector space", services.semanticModels.LSA.LSA::getAvailableLanguages, true),
    LDA("LDA", "Inverse JSH in LDA probability distribution", services.semanticModels.LDA.LDA::getAvailableLanguages, true),
    WORD2VEC("word2vec", "Cosine similarity in word2vec space", Word2VecModel::getAvailableLanguages, true),
    PARAGRAPHVECTORS("ParagraphVectors", "Cosine similarity in ParagraphVectors space", ParagraphVectorsModel::getAvailableLanguages, true);

    private final String acronym;
    private final String name;
    private final Supplier<Set<Lang>> supplier;
    private final boolean loadable;
    
    private SimilarityType(String acronym, String name, Supplier<Set<Lang>> supplier, boolean loadable) {
        this.acronym = acronym;
        this.name = name;
        this.supplier = supplier;
        this.loadable = loadable;
    }

    public String getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }

    public Set<Lang> getAvailableLanguages() {
        return supplier.get();
    }

    public boolean isLoadable() {
        return loadable;
    }
    
    public static List<ISemanticModel> loadVectorModels(Map<SimilarityType, String> modelPaths, Lang lang) {
        // load also LSA vector space and LDA model
        List<ISemanticModel> models = new ArrayList<>();
        if (modelPaths != null) {
            for (Map.Entry<SimilarityType, String> e : modelPaths.entrySet()) {
                switch (e.getKey()) {
                    case LDA:
                        LDA lda = services.semanticModels.LDA.LDA.loadLDA(e.getValue(), lang);
                        models.add(lda);
                        break;
                    case LSA:
                        LSA lsa = services.semanticModels.LSA.LSA.loadLSA(e.getValue(), lang);
                        models.add(lsa);
                        break;
                    case WORD2VEC:
                        Word2VecModel w2v = services.semanticModels.word2vec.Word2VecModel.loadWord2Vec(e.getValue(), lang);
                        models.add(w2v);
                        break;
                    case PARAGRAPHVECTORS:
                        ParagraphVectorsModel pv = services.semanticModels.paragraphVectors.ParagraphVectorsModel.loadParagraphVectors(e.getValue(), lang);
                        models.add(pv);
                        break;
                }
            }
        }
        return models;
    }
}
