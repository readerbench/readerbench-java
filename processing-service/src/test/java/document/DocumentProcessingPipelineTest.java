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
package document;

import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;

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
    protected List<SemanticModel> models;
    protected List<Annotators> annotators;

    @Before
    public void initialize() {
        //todo - the paths to files from resources need to be write in a .properties file
        lang = Lang.en;
        models = SemanticModel.loadModels("tasa", lang);

        annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
    }

    @Test
    public void createDocumentTextTest() {
        try{
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        Document d = pipeline.createDocumentFromTemplate(AbstractDocumentTemplate.getDocumentModel("In this chapter, I shall investigate Wittgenstein's private language argument, that is, the argument to be found in Philosophical Investigations. Roughly, this argument is intended to show that a language knowable to one person and only that person is impossible; in other words, a \"language\" which another person cannot understand isn't a language. Given the prolonged debate sparked by these passages, one must have good reason to bring it up again. I have: Wittgenstein's attack on private languages has regularly been misinterpreted. Moreover, it has been misinterpreted in a way that draws attention away from the real force of his arguments and so undercuts the philosophical significance of these passages.\n"
                + "What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition."));
        pipeline.processDocument(d);
        Assert.assertEquals(2, d.getNoBlocks());
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void createDocumentXMLTest() {
        models = SemanticModel.loadModels("coca", lang);
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        Document d = pipeline.createDocumentFromXML("src/test/resources/reading_material_en.xml");
        pipeline.processDocument(d);
        Assert.assertEquals(9, d.getNoBlocks());
    }
}
