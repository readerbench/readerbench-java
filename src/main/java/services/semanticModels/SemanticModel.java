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
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author Stefan Ruseti
 */
public enum SemanticModel {
    LSA, LDA, Word2Vec;
    
    public static List<ISemanticModel> loadModels(Map<SemanticModel, String> modelPaths, Lang lang) {
        // load also LSA vector space and LDA model
        List<ISemanticModel> models = new ArrayList<>();
        if (modelPaths != null) {
            for (Map.Entry<SemanticModel, String> e : modelPaths.entrySet()) {
                switch (e.getKey()) {
                    case LDA:
                        LDA lda = services.semanticModels.LDA.LDA.loadLDA(e.getValue(), lang);
                        models.add(lda);
                        break;
                    case LSA:
                        LSA lsa = services.semanticModels.LSA.LSA.loadLSA(e.getValue(), lang);
                        models.add(lsa);
                        break;
                }
            }
        }
        return models;
    }
}
