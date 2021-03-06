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
package com.readerbench.processingservice.document;

import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.Summary;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.readingstrategies.ReadingStrategies;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class SummaryProcessingPipeline extends MetacognitionProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetacognitionProcessingPipeline.class);

    public SummaryProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    public Summary createSummaryFromTemplate(AbstractDocumentTemplate docTmp, Document initialReadingMaterial) {
        LOGGER.info("Building internal representation ...");
        Summary s = new Summary(null, initialReadingMaterial);
        Parsing.parseDoc(docTmp, s, getAnnotators().contains(Annotators.NLP_PREPROCESSING), getLanguage());
        s.setCohesion(new SemanticCohesion(s, initialReadingMaterial));
        return s;
    }

    public Summary createSummaryFromXML(String path, Document initialReadingMaterial) {
        AbstractDocumentTemplate docTmp = extractDocumentContent(path, "p");
        Summary s = createSummaryFromTemplate(docTmp, initialReadingMaterial);
        s.setPath(path);
        addInformationFromXML(path, s);
        return s;
    }

    public void processMetacognition(Summary s) {
        processDocument(s);
        ReadingStrategies.detReadingStrategies(s);

        determineCohesion(s);
        LOGGER.info("Finished processing self-explanations ...");
    }
}
