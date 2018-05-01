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
package com.readerbench.processingservice.cscl;

import com.readerbench.coreservices.cscl.data.Conversation;
import com.readerbench.processingservice.document.DocumentProcessingPipelineTest;
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
        Conversation c = pipeline.createConversationFromXML("src/test/resources/sample_chat_en.xml");
        pipeline.processConversation(c);
        Assert.assertEquals(304, c.getNoBlocks());
    }
}
