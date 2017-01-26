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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.complexity.ComplexityIndices;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;

public class QueryHelper {

    private static Logger LOGGER = Logger.getLogger("");

    public static AbstractDocument processQuery(Map<String, String> hm) {
        LOGGER.info("Processign query ...");
        Lang lang = Lang.getLang(hm.get("lang"));
        AbstractDocumentTemplate template = AbstractDocumentTemplate.getDocumentModel(hm.get("text"));
        List<ISemanticModel> models = new ArrayList<>();
        models.add(LSA.loadLSA(hm.get("lsa"), lang));
        models.add(LDA.loadLDA(hm.get("lda"), lang));
        if (hm.get("word2vec") != null) models.add(Word2VecModel.loadWord2Vec(hm.get("word2vec"), lang));
        AbstractDocument document = new Document(
                null,
                template,
                models,
                lang,
                Boolean.parseBoolean(hm.get("postagging"))
        );
        LOGGER.log(Level.INFO, "Built document has {0} blocks.", document.getBlocks().size());
        document.computeAll(Boolean.parseBoolean(hm.get("dialogism")));
        ComplexityIndices.computeComplexityFactors(document);
        return document;
    }
}
