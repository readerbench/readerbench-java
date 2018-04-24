/*
 * Copyright 2018 ReaderBench.
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
package com.readerbench.processingservices;

import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.coreservices.semanticModels.LSA.LSA;
import com.readerbench.coreservices.semanticModels.word2vec.Word2VecModel;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.ExportDocument;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ReaderBench
 */
public class DocumentProcessingPipelineTest {

    @Test
    public void createDocument() {
        Lang lang = Lang.en;
        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", Lang.en);

        List<ISemanticModel> models = new ArrayList<>();
        models.add(lsa);
        models.add(lda);
        models.add(w2v);

        List<Annotators> annotators = new ArrayList<>(Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY));
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        ExportDocument ed = new ExportDocument();
        Document d = pipeline.createDocumentFromXML("resources/in/NLP2012/reading_material_en.xml");
        pipeline.processDocument(d);
        System.out.println(d.toString());
    }
}
