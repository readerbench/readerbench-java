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

import com.readerbench.coreservices.cna.CohesionGraph;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.cscl.data.CSCLIndices;
import com.readerbench.coreservices.cscl.data.Community;
import com.readerbench.coreservices.cscl.data.Conversation;
import com.readerbench.coreservices.cscl.data.Participant;
import com.readerbench.coreservices.cscl.data.Utterance;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class CommunityProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityProcessing.class);

    public void determineParticipantContributions(Community community) {
        for (Conversation c : community.getConversations()) {
            // update the community correspondingly
            for (Participant p : c.getParticipants()) {
                if (p.getContributions().getBlocks() != null && !p.getContributions().getBlocks().isEmpty()) {
                    int index = community.getParticipants().indexOf(p);
                    Participant participantToUpdate;
                    if (index >= 0) {
                        participantToUpdate = community.getParticipants().get(index);
                    } else {
                        participantToUpdate = new Participant(p.getName(), c);
                        community.getParticipants().add(participantToUpdate);
                    }

                    for (Block b : p.getContributions().getBlocks()) {
                        Utterance u = (Utterance) b;
                        // select contributions in imposed timeframe
                        if (u != null && u.getTime() != null && u.isEligible(community.getStartDate(), community.getEndDate())) {
                            // determine first timestamp of considered contributions
                            if (community.getFistContributionDate() == null) {
                                community.setFistContributionDate(u.getTime());
                            }
                            if (u.getTime().before(community.getFistContributionDate())) {
                                community.setFistContributionDate(u.getTime());
                            }
                            Calendar date = new GregorianCalendar(2010, Calendar.JANUARY, 1);
                            if (u.getTime().before(date.getTime())) {
                                LOGGER.error("Incorrect time! {} / {} : {}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }
                            if (u.getTime().after(new Date())) {
                                LOGGER.error("Incorrect time! {} / {} : {}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }

                            if (community.getLastContributionDate() == null) {
                                community.setLastContributionDate(u.getTime());
                            }
                            if (u.getTime().after(community.getLastContributionDate())) {
                                community.setLastContributionDate(u.getTime());
                            }
                            Block.addBlock(participantToUpdate.getContributions(), b);
                            if (b.isSignificant()) {
                                Block.addBlock(participantToUpdate.getSignificantContributions(), b);
                            }

                            participantToUpdate.getIndices().put(CSCLIndices.NO_CONTRIBUTION,
                                    participantToUpdate.getIndices().get(CSCLIndices.NO_CONTRIBUTION) + 1);

                            for (Map.Entry<Word, Integer> entry : u.getWordOccurences().entrySet()) {
                                if (entry.getKey().getPOS() != null) {
                                    if (entry.getKey().getPOS().startsWith("N")) {
                                        participantToUpdate.getIndices().put(CSCLIndices.NO_NOUNS,
                                                participantToUpdate.getIndices().get(CSCLIndices.NO_NOUNS)
                                                + entry.getValue());
                                    }
                                    if (entry.getKey().getPOS().startsWith("V")) {
                                        participantToUpdate.getIndices().put(CSCLIndices.NO_VERBS,
                                                participantToUpdate.getIndices().get(CSCLIndices.NO_VERBS)
                                                + entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void determineParticipantion(Community community) {
        community.setParticipantContributions(new double[community.getParticipants().size()][community.getParticipants().size()]);

        for (Conversation c : community.getConversations()) {
            // determine strength of links
            for (int i = 0; i < c.getBlocks().size(); i++) {
                Utterance u = (Utterance) c.getBlocks().get(i);
                // select contributions in imposed timeframe
                if (u != null && u.getTime() != null && u.isEligible(community.getStartDate(), community.getEndDate())) {
                    Participant p1 = u.getParticipant();
                    int index1 = community.getParticipants().indexOf(p1);
                    if (index1 >= 0) {
                        community.getParticipantContributions()[index1][index1] += u.getScore();
                        Participant participantToUpdate = community.getParticipants().get(index1);
                        participantToUpdate.getIndices().put(CSCLIndices.SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.SCORE) + u.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.SOCIAL_KB) + u.getSocialKB());

                        for (int j = 0; j < i; j++) {
                            if (c.getPrunnedBlockDistances()[i][j] != null) {
                                Participant p2 = ((Utterance) c.getBlocks().get(j)).getParticipant();
                                int index2 = community.getParticipants().indexOf(p2);
                                if (index2 >= 0) {
                                    // model knowledge building effect
                                    double addedKB = c.getBlocks().get(i).getScore() * c.getPrunnedBlockDistances()[i][j].getCohesion();
                                    community.getParticipantContributions()[index1][index2] += addedKB;
                                }
                            }
                        }
                    }
                }
            }
            for (Participant p : c.getParticipants()) {
                if (community.getParticipants().contains(p)) {
                    Participant participantToUpdate = community.getParticipants().get(community.getParticipants().indexOf(p));
                    participantToUpdate.getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
                            participantToUpdate.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
                            + p.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE));
                }
            }
        }
    }

    public void computeSNAMetrics(Community community) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        if (community.getStartDate() != null && community.getEndDate() != null && community.getParticipants() != null && community.getParticipants().size() > 0) {
            LOGGER.info("Processing timeframe between {} and {} having {} participants ...", new Object[]{dateFormat.format(community.getStartDate()), dateFormat.format(community.getEndDate()), community.getParticipants().size()});
        }

        ParticipantEvaluation.performSNA(community.getParticipants(), community.getParticipantContributions(), true);

        // update surface statistics
        for (Conversation c : community.getConversations()) {
            Participant p = null;
            for (int i = 0; i < c.getBlocks().size(); i++) {
                if (c.getBlocks().get(i) != null) {
                    if (p == null) {
                        p = ((Utterance) c.getBlocks().get(i)).getParticipant();
                        Participant participantToUpdate = community.getParticipants().get(community.getParticipants().indexOf(p));
                        participantToUpdate.getIndices().put(CSCLIndices.NO_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.NO_NEW_THREADS) + 1);
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_OVERALL_SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_OVERALL_SCORE)
                                + c.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB)
                                + VectorAlgebra.sumElements(((Conversation) c).getSocialKBEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE)
                                + VectorAlgebra.sumElements(((Conversation) c).getVoicePMIEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                                + c.getBlocks().get(i).getText().length());
                        break;
                    }
                }
            }
        }

        community.getParticipants().stream().filter((p) -> (p.getIndices().get(CSCLIndices.NO_NEW_THREADS) != 0)).forEach((p) -> {
            p.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                    p.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                    / p.getIndices().get(CSCLIndices.NO_NEW_THREADS));
        });
    }

}
