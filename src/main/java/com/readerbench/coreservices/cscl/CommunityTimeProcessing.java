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

import com.readerbench.datasourceprovider.data.cscl.CSCLCriteria;
import com.readerbench.datasourceprovider.data.cscl.CSCLIndices;
import com.readerbench.datasourceprovider.data.cscl.Community;
import com.readerbench.datasourceprovider.data.cscl.Participant;
import java.util.AbstractMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class CommunityTimeProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityTimeProcessing.class);

    public void modelTimeEvolution(Community community) {
        LOGGER.info("Modeling time evolution for {} participants ...", community.getParticipants().size());
        for (CSCLIndices index : CSCLIndices.values()) {
            if (index.isUsedForTimeModeling()) {
                LOGGER.info("Modeling based on {}", index.getDescription());
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
