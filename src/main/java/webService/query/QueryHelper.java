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
package webService.query;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndices;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;

public class QueryHelper {

    private static final Logger LOGGER = Logger.getLogger("");
    private static final String SEMANTIC_MODELS_ROOT = "resources/config/";

    public static List<ISemanticModel> getSemanticModels(Map<String, String> hm) {
        Lang lang = Lang.getLang(hm.get("language"));
        String semanticModelsPath = SEMANTIC_MODELS_ROOT + '/' + lang.toString().toUpperCase();
        List<ISemanticModel> models = new ArrayList<>();
        if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
            models.add(LSA.loadLSA(semanticModelsPath + '/' + "LSA" + '/' + hm.get("lsa"), lang));
        }
        if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
            models.add(LDA.loadLDA(semanticModelsPath + '/' + "LDA" + '/' + hm.get("lda"), lang));
        }
        if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
            models.add(Word2VecModel.loadWord2Vec(semanticModelsPath + '/' + "word2vec" + '/' + hm.get("w2v"), lang));
        }
        return models;
    }

    public static AbstractDocument processQuery(Map<String, String> hm) {
        LOGGER.info("Processign query ...");
        Lang lang = Lang.getLang(hm.get("language"));
        AbstractDocumentTemplate template;
        try {
            template = AbstractDocumentTemplate.getDocumentModel(URLDecoder.decode(hm.get("text"), "UTF-8"));

            List<ISemanticModel> models = getSemanticModels(hm);
            AbstractDocument document = new Document(
                    null,
                    template,
                    models,
                    lang,
                    Boolean.parseBoolean(hm.get("pos-tagging"))
            );
            LOGGER.log(Level.INFO, "Built document has {0} blocks.", document.getBlocks().size());
            document.computeAll(Boolean.parseBoolean(hm.get("dialogism")));
            ComplexityIndices.computeComplexityFactors(document);
            return document;
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
