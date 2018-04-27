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

import com.readerbench.coreservices.cscl.CommunityProcessing;
import com.readerbench.coreservices.cscl.CommunityTimeProcessing;
import com.readerbench.datasourceprovider.data.cscl.Community;
import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class CommunityProcessingPipeline extends ConversationProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(Community.class);

    public CommunityProcessingPipeline(Lang lang, List<ISemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    public Community createCommunityFromConversations(String name, List<Conversation> conversations, Date startDate, Date endDate) {
        Community community = new Community(name, getLanguage(), startDate, endDate);

        community.setName(name);
        for (Conversation c : conversations) {
            community.getConversations().add(c);
        }

        CommunityProcessing cp = new CommunityProcessing();
        cp.determineParticipantContributions(community);
        cp.determineParticipantion(community);

        return community;
    }

    public void processCommunity(Community community) {
        CommunityProcessing cp = new CommunityProcessing();
        cp.computeSNAMetrics(community);
        if (getAnnotators().contains(Annotators.TEXTUAL_COMPLEXITY)) {
            cp.computeParticipantTextualComplexity(community);
        }
    }

    public void processTimeSeries(Community community, int monthIncrement, int dayIncrement) {
        CommunityTimeProcessing ctp = new CommunityTimeProcessing();
        ctp.determineSubCommunities(community, monthIncrement, dayIncrement);
        ctp.modelTimeEvolution(community);
    }
}
