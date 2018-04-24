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
package com.readerbench.processingservice;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ImportDocument {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDocument.class);

    public AbstractDocument importSerializedDocument(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
        LOGGER.info("Loading serialized document {} ...", path);
        ObjectInputStream oIn = new ObjectInputStream(new FileInputStream(new File(path)));
        AbstractDocument d = (AbstractDocument) oIn.readObject();
        Map<SimilarityType, String> modelPaths = (Map<SimilarityType, String>) oIn.readObject();
        rebuildSemanticSpaces(d, SimilarityType.loadVectorModels(modelPaths, d.getLanguage()));
        return d;
    }

    public void rebuildSemanticSpaces(AbstractDocument abstractDocument, List<ISemanticModel> models) {
        abstractDocument.setSemanticModels(models);
        for (Block b : abstractDocument.getBlocks()) {
            if (b != null) {
                b.setSemanticModels(models);
                if (b.getSentences() != null) {
                    for (Sentence s : b.getSentences()) {
                        s.setSemanticModels(models);
                        for (Word w : s.getAllWords()) {
                            w.setSemanticModels(models);
                        }
                    }
                }
            }
        }
        if (abstractDocument.getVoices() != null) {
            for (SemanticChain chain : abstractDocument.getVoices()) {
                chain.setSemanticModels(models);
            }
        }
    }
}
