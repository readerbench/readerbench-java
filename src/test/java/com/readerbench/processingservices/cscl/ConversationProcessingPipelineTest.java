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
package com.readerbench.processingservices.cscl;

import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.processingservices.document.DocumentProcessingPipelineTest;
import com.readerbench.processingservice.cscl.ConversationProcessingPipeline;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ReaderBench
 */
public class ConversationProcessingPipelineTest extends DocumentProcessingPipelineTest {

    @Test
    public void createConversationXMLTest() {
        ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(lang, models, annotators);
        Conversation c = pipeline.createConversationFromXML("srcs/test/resources/sample_chat_en.xml");
        pipeline.processDocument(c);
        Assert.assertEquals(300, c.getNoBlocks());
    }
}
