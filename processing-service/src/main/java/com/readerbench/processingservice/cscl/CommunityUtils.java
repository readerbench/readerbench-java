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

import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.data.cscl.CSCLIndices;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.ParticipantNormalized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityUtils.class);

    public static List<Participant> filterParticipants(Community community) {
        return community.getParticipants()
                .parallelStream()
                .filter(p -> p.getIndices().get(CSCLIndices.OUTDEGREE) != 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void hierarchicalClustering(Community community, String pathToFile) {
        List<Participant> filteredParticipants = CommunityUtils.filterParticipants(community);
        ClusterCommunity.performAglomerativeClusteringForCSCL(filteredParticipants, pathToFile);
        LOGGER.info("Clustering finished");
    }

    public static List<ParticipantNormalized> normalizeParticipantsData(List<Participant> participants) {
        double[] indegree = new double[participants.size()];
        double[] outdegree = new double[participants.size()];

        for (int i = 0; i < participants.size(); i++) {
            indegree[i] = participants.get(i).getIndices().get(CSCLIndices.INDEGREE);
            outdegree[i] = participants.get(i).getIndices().get(CSCLIndices.OUTDEGREE);
        }

        double[] normalizedIndegree = VectorAlgebra.softmax(indegree);
        double[] normalizedOutdegree = VectorAlgebra.softmax(outdegree);

        List<ParticipantNormalized> normalizeds = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            ParticipantNormalized p = new ParticipantNormalized(normalizedIndegree[i], normalizedOutdegree[i]);
            p.setName(participants.get(i).getName());
            normalizeds.add(p);
        }

        return normalizeds;
    }
}