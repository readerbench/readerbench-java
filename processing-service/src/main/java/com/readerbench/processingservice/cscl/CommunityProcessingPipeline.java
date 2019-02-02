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

import com.readerbench.coreservices.cna.CohesionGraph;
import com.readerbench.coreservices.cscl.CommunityProcessing;
import com.readerbench.coreservices.cscl.CommunityTimeProcessing;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.readerbench.textualcomplexity.ComplexityIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class CommunityProcessingPipeline extends ConversationProcessingPipeline {

    private static final int MIN_NO_CONTRIBUTIONS = 3;
    private static final int MIN_NO_CONTENT_WORDS = 50;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityProcessingPipeline.class);

    public CommunityProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    public Community createCommunityFromConversations(String name, List<Conversation> conversations, List<SemanticModel> models, Date startDate, Date endDate) {
        Community community = new Community(name, getLanguage(), models, startDate, endDate);
        community.setEligibleContributions(new Conversation(null, community.getSemanticModelsAsList(), community.getLanguage()));

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
            computeParticipantTextualComplexity(community);
        }
    }

    public void processTimeSeries(Community community, int monthIncrement, int dayIncrement) {
        CommunityTimeProcessing ctp = new CommunityTimeProcessing();
        ctp.determineSubCommunities(community, monthIncrement, dayIncrement);
        ctp.modelTimeEvolution(community);
    }

    public void computeParticipantTextualComplexity(Community community) {
        // determine complexity indices
        for (Participant p : community.getParticipants()) {
            // establish minimum criteria
            int noContentWords = 0;
            for (Block b : p.getSignificantContributions().getBlocks()) {
                if (b != null) {
                    for (Map.Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
                        noContentWords += entry.getValue();
                    }
                }
            }

            if (p.getSignificantContributions().getBlocks().size() >= MIN_NO_CONTRIBUTIONS && noContentWords >= MIN_NO_CONTENT_WORDS) {
                // build cohesion graph for additional indices
                CohesionGraph.buildCohesionGraph(p.getSignificantContributions());
                ComplexityIndices.computeComplexityFactors(p.getSignificantContributions());
            }
        }
    }
}
