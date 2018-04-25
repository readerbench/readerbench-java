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
package com.readerbench.processingservices.document;

import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.coreservices.semanticModels.LSA.LSA;
import com.readerbench.coreservices.semanticModels.word2vec.Word2VecModel;
import com.readerbench.datasourceprovider.dao.hibernate.SQLiteDatabase;
import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ReaderBench
 */
public class DocumentProcessingPipelineTest {

    protected Lang lang;
    protected List<ISemanticModel> models;
    protected List<Annotators> annotators;

    @Before
    public void initialize() {
        SQLiteDatabase.initializeDB();

        lang = Lang.en;
        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);

        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", Lang.en);
        models = new ArrayList<>();
        models.add(lsa);
        models.add(lda);
        models.add(w2v);

        annotators = new ArrayList<>(Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY));
    }

    @Test
    public void createDocumentTextTest() {
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        Document d = pipeline.createDocumentFromTemplate(AbstractDocumentTemplate.getDocumentModel("In this chapter, I shall investigate Wittgenstein's private language argument, that is, the argument to be found in Philosophical Investigations. Roughly, this argument is intended to show that a language knowable to one person and only that person is impossible; in other words, a \"language\" which another person cannot understand isn't a language. Given the prolonged debate sparked by these passages, one must have good reason to bring it up again. I have: Wittgenstein's attack on private languages has regularly been misinterpreted. Moreover, it has been misinterpreted in a way that draws attention away from the real force of his arguments and so undercuts the philosophical significance of these passages.\n"
                + "What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition."));
        pipeline.processDocument(d);
        Assert.assertEquals(2, d.getNoBlocks());
    }

    @Test
    public void createDocumentXMLTest() {
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        Document d = pipeline.createDocumentFromXML("srcs/test/resources/reading_material_en.xml");
        pipeline.processDocument(d);
        Assert.assertEquals(9, d.getNoBlocks());
    }
}
