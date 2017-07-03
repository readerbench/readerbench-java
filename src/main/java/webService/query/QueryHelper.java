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
import data.SemanticCorpora;
import data.document.Document;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;

public class QueryHelper {

    private static final Logger LOGGER = Logger.getLogger("");

    public static List<ISemanticModel> loadSemanticModels(Lang lang, String lsaCorpora, String ldaCorpora, String w2vCorpora) {
        List<ISemanticModel> models = new ArrayList<>();
        if (lsaCorpora != null && (lsaCorpora.compareTo("") != 0)) {
            models.add(LSA.loadLSA(SemanticCorpora.getSemanticCorpora(lsaCorpora, lang, SimilarityType.LSA).getFullPath(), lang));
        }
        if (ldaCorpora != null && (ldaCorpora.compareTo("") != 0)) {
            models.add(LDA.loadLDA(SemanticCorpora.getSemanticCorpora(ldaCorpora, lang, SimilarityType.LDA).getFullPath(), lang));
        }
        if (w2vCorpora != null && (w2vCorpora.compareTo("") != 0)) {
            models.add(Word2VecModel.loadWord2Vec(SemanticCorpora.getSemanticCorpora(w2vCorpora, lang, SimilarityType.WORD2VEC).getFullPath(), lang));
        }
        return models;
    }

    public static String textToUTF8(String text) {
        try {
            text = text.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            return URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static AbstractDocument generateDocument(String text, Lang lang, List<ISemanticModel> models, Boolean usePosTagging, Boolean computeDialogism) {
        LOGGER.info("Generating document...");
        AbstractDocumentTemplate template = AbstractDocumentTemplate.getDocumentModel(textToUTF8(text));
        AbstractDocument document = new Document(null, template, models, lang, usePosTagging);
        LOGGER.log(Level.INFO, "Generated document has {0} blocks.", document.getBlocks().size());
        document.computeAll(computeDialogism);
        return document;
    }
}
