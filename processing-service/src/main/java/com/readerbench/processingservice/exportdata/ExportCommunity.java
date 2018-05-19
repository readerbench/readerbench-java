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
package com.readerbench.processingservice.exportdata;

import com.readerbench.coreservices.data.cscl.CSCLIndices;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.CSCLCriteria;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.keywordmining.Keyword;
import com.readerbench.coreservices.keywordmining.KeywordModeling;
import com.readerbench.datasourceprovider.commons.Formatting;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ExportCommunity {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportCommunity.class);

    private final Community community;

    public ExportCommunity(Community community) {
        this.community = community;
    }

    /**
     * Export Individual Stats and Initiation
     *
     * @param pathToFileIndividualStats - path to file with Individual State
     * @param pathToFileInitiation - path to file with Initiation
     */
    public void exportIndividualStatsAndInitiation(String pathToFileIndividualStats, String pathToFileInitiation) {
        LOGGER.info("Writing Individual Stats and Initiation export");
        // print participant statistics
        try (BufferedWriter outIndividualStats = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pathToFileIndividualStats)), "UTF-8"), 32768);
                BufferedWriter outInitiation = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pathToFileInitiation)), "UTF-8"), 32768)) {

            // print participant statistics
            if (community.getParticipants().size() > 0) {
                outIndividualStats.write("Individual stats\n");
                outIndividualStats.write("Participant name,Anonymized name");

                outInitiation.write("Invocation\n");
                outInitiation.write("Participant name,Anonymized name");
                for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                    if (CSCLindex.isIndividualStatsIndex()) {
                        outIndividualStats.write("," + CSCLindex.getDescription() + "(" + CSCLindex.getAcronym() + ")");
                    } else {
                        outInitiation.write("," + CSCLindex.getDescription() + "(" + CSCLindex.getAcronym() + ")");
                    }
                }
                outIndividualStats.write("\n");
                outInitiation.write("\n");
                for (int index = 0; index < community.getParticipants().size(); index++) {
                    Participant p = community.getParticipants().get(index);
                    outIndividualStats.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);
                    outInitiation.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);
                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        if (CSCLindex.isIndividualStatsIndex()) {
                            outIndividualStats.write("," + Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                        } else {
                            outInitiation.write("," + Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                        }
                    }
                    outIndividualStats.write("\n");
                    outInitiation.write("\n");
                }
            }
            LOGGER.info("Successfully finished writing Individual Stats and Initiation export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Export Textual Complexity
     *
     * @param pathToFileTextualComplexity - path to file
     */
    public void exportTextualComplexity(String pathToFileTextualComplexity) {
        LOGGER.info("Writing Textual Complexity export");
        try (BufferedWriter outTextualComplexity = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFileTextualComplexity)), "UTF-8"), 32768)) {

            // print participant statistics
            if (community.getParticipants().size() > 0) {
                outTextualComplexity.write("Textual Complexity\n");
                outTextualComplexity.write("Participant name,Anonymized name");

                List<ComplexityIndex> factors = ComplexityIndices.getIndices(community.getLanguage());
                for (ComplexityIndex factor : factors) {
                    outTextualComplexity.write("," + factor.getAcronym());
                }
                outTextualComplexity.write("\n");

                for (int index = 0; index < community.getParticipants().size(); index++) {
                    Participant p = community.getParticipants().get(index);
                    outTextualComplexity.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);

                    for (ComplexityIndex factor : factors) {
                        if (p.getSignificantContributions().getComplexityIndices() != null) {
                            outTextualComplexity.write("," + Formatting.formatNumber(p.getSignificantContributions().getComplexityIndices().get(factor)));
                        }
                    }
                    outTextualComplexity.write("\n");
                }
            }
            LOGGER.info("Successfully finished writing Textual Complexity export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Export Time Analysis
     *
     * @param pathToFileTimeAnalysis
     */
    public void exportTimeAnalysis(String pathToFileTimeAnalysis) {
        LOGGER.info("Writing Time Analysis export");
        try (BufferedWriter outTimeAnalysis = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFileTimeAnalysis)), "UTF-8"), 32768)) {

            // print participant statistics
            if (community.getParticipants().size() > 0) {
                outTimeAnalysis.write("Time Analysis\n");
                outTimeAnalysis.write("Participant name,Anonymized name");

                for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                    if (CSCLindex.isUsedForTimeModeling()) {
                        for (CSCLCriteria crit : CSCLCriteria.values()) {
                            outTimeAnalysis.write("," + crit.getDescription() + "(" + CSCLindex.getAcronym() + ")");
                        }
                    }
                }
                outTimeAnalysis.write("\n");

                for (int index = 0; index < community.getParticipants().size(); index++) {
                    Participant p = community.getParticipants().get(index);
                    outTimeAnalysis.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);

                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        if (CSCLindex.isUsedForTimeModeling()) {
                            for (CSCLCriteria crit : CSCLCriteria.values()) {
                                outTimeAnalysis.write("," + p.getLongitudinalIndices().get(new AbstractMap.SimpleEntry<>(CSCLindex, crit)));
                            }
                        }
                    }
                    outTimeAnalysis.write("\n");
                }
            }
            LOGGER.info("Successfully finished writing Time Analysis export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Export discussed topics
     *
     * @param pathToFileDiscussedTopics - path to file
     */
    public void exportDiscussedTopics(String pathToFileDiscussedTopics) {
        LOGGER.info("Writing Discussed Topics export");
        try (BufferedWriter outDiscussedTopics = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFileDiscussedTopics)), "UTF-8"), 32768)) {

            if (community.getParticipants().size() > 0) {
                // print discussed topics
                outDiscussedTopics.write("\nDiscussed topics\n");
                outDiscussedTopics.write("Concept,Relevance\n");
                List<Keyword> topicL = KeywordModeling.getCollectionTopics(community.getConversations());
                for (Keyword t : topicL) {
                    outDiscussedTopics.write(t.getWord().getLemma() + "," + t.getRelevance() + "\n");
                }
            }

            LOGGER.info("Successfully finished writing Discussed Topics export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * // * Export individual threads statistics // * // * @param pathToFile
     * -path to file //
     *
     * @param pathToFile
     */
    public void exportIndividualThreadStatistics(String pathToFile) {
        LOGGER.info("Writing Individual Threads Statistics export");
        try (BufferedWriter outIndividualThreadsStatistics = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFile)), "UTF-8"), 32768)) {

            if (community.getParticipants().size() > 0) {
                // print general statistic per thread
                outIndividualThreadsStatistics.write("\nIndividual thread statistics\n");
                outIndividualThreadsStatistics.write("Thread path,No. contributions,No. involved paticipants,"
                        + "Overall score,Cummulative inter-animation,Cummulative social knowledge-building\n");
                for (Conversation c : community.getConversations()) {
                    int noBlocks = 0;
                    noBlocks = c.getBlocks().stream().filter((b) -> (b != null)).map((_item) -> 1).reduce(noBlocks, Integer::sum);

                    outIndividualThreadsStatistics.write(new File(c.getPath()).getName() + "," + noBlocks + ","
                            + ((Conversation) c).getParticipants().size() + ","
                            + Formatting.formatNumber(c.getScore()) + ","
                            + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) c).getVoicePMIEvolution()))
                            + ","
                            + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) c).getSocialKBEvolution()))
                            + "\n");
                }
            }

            LOGGER.info("Successfully finished writing Individual Threads Statistics export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
