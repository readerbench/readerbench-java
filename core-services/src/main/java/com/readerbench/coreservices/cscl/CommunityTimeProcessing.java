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
package com.readerbench.coreservices.cscl;

import com.readerbench.coreservices.data.cscl.CSCLCriteria;
import com.readerbench.coreservices.data.cscl.CSCLIndices;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class CommunityTimeProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityTimeProcessing.class);

    public void determineSubCommunities(Community community, int monthIncrement, int dayIncrement) {
        // create corresponding sub-communities
        Calendar cal = Calendar.getInstance();
        if (community.getFistContributionDate() == null) {
            LOGGER.error("first contribution date not existing");
            return;
        }
        Date startSubCommunities = community.getFistContributionDate();
        cal.setTime(startSubCommunities);
        cal.add(Calendar.MONTH, monthIncrement);
        cal.add(Calendar.DATE, dayIncrement);
        Date endSubCommunities = cal.getTime();

        while (endSubCommunities.before(community.getLastContributionDate())) {
            community.getTimeframeSubCommunities().add(extractSubCommunity(community, startSubCommunities, endSubCommunities));

            // update timeStamps
            startSubCommunities = endSubCommunities;
            cal.add(Calendar.MONTH, monthIncrement);
            cal.add(Calendar.DATE, dayIncrement);
            endSubCommunities = cal.getTime();
        }
        // create partial community with remaining contributions
        community.getTimeframeSubCommunities().add(extractSubCommunity(community, startSubCommunities, community.getLastContributionDate()));

        LOGGER.info("Finished creating {} timeframe sub-communities spanning from {} to {}", new Object[]{community.getTimeframeSubCommunities().size(), community.getFistContributionDate(), community.getLastContributionDate()});
    }

    private Community extractSubCommunity(Community community, Date startSubCommunities, Date endSubCommunities) {
        Community subCommunity = new Community(community.getName(), community.getLanguage(), community.getSemanticModelsAsList(), startSubCommunities, endSubCommunities);
        subCommunity.setEligibleContributions(new Conversation(null, community.getSemanticModelsAsList(), community.getLanguage()));

        for (Conversation c : community.getConversations()) {
            subCommunity.getConversations().add(c);
        }
        CommunityProcessing cp = new CommunityProcessing();
        cp.determineParticipantContributions(subCommunity);
        cp.determineParticipantion(subCommunity);
        cp.computeSNAMetrics(subCommunity);
        return subCommunity;
    }

    public void modelTimeEvolution(Community community) {
        LOGGER.info("Modeling time evolution for {} participants ...", community.getParticipants().size());
        for (CSCLIndices index : CSCLIndices.values()) {
            if (index.isUsedForTimeModeling()) {
                LOGGER.info("Modeling based on {}", index.getDescription(community.getLanguage()));
                int no = 0;
                for (Participant p : community.getParticipants()) {
                    // model time evolution of each participant
                    double[] values = new double[community.getTimeframeSubCommunities().size()];
                    for (int i = 0; i < community.getTimeframeSubCommunities().size(); i++) {
                        int localParticipantIndex = community.getTimeframeSubCommunities().get(i).getParticipants().indexOf(p);
                        if (localParticipantIndex != -1) {
                            values[i] = community.getTimeframeSubCommunities().get(i).getParticipants().get(localParticipantIndex).getIndices().get(index);
                        }
                    }
                    if (++no % 100 == 0) {
                        LOGGER.info("Finished evaluating the time evolution of {} participants", no);
                    }
                    for (CSCLCriteria crit : CSCLCriteria.values()) {
                        p.getLongitudinalIndices().put(new AbstractMap.SimpleEntry<>(index, crit), CSCLCriteria.getValue(crit, values));
                    }
                }
            }
        }
    }
}
