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

import com.readerbench.coreservices.cna.extendedcna.distancestrategies.AuthorDistanceStrategyType;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.cscl.CSCLIndices;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.keywordmining.Keyword;
import com.readerbench.coreservices.keywordmining.KeywordModeling;
import com.readerbench.datasourceprovider.commons.Formatting;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.HdrHistogram.DoubleLinearIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ExportCommunityToES {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportCommunityToES.class);

    private final Community community;

    public ExportCommunityToES(Community community) {
        this.community = community;
    }

    /**
     * Generate json file with all participants for graph representation (using
     * d3.js)
     *
     * @param week
     * @return
     */
    public JSONObject generateParticipantViewD3(Integer week) {

        JSONObject jsonObject = new JSONObject();

        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();
        List<String> names = new ArrayList<>();

        for (int row = 0; row < community.getParticipantContributions().length; row++) {
            for (int col = 0; col < community.getParticipantContributions()[row].length; col++) {
                if (community.getParticipantContributions()[row][col] > 0 && community.getParticipants().get(row).getParticipantGroup() != null
                        && community.getParticipants().get(col).getParticipantGroup() != null) {
                    JSONObject link = new JSONObject();
                    link.put("source", row);
                    link.put("target", col);
                    link.put("score", community.getParticipantContributions()[row][col]);
                    links.add(link);

                    if (!names.contains(community.getParticipants().get(row).getName())) {
                        names.add(community.getParticipants().get(row).getName());
                        JSONObject rowP = new JSONObject();
                        rowP.put("name", community.getParticipants().get(row).getName());
                        rowP.put("id", row);

                        rowP.put("value", (community.getParticipants().get(row).getIndices().get(CSCLIndices.INDEGREE)
                                + community.getParticipants().get(row).getIndices().get(CSCLIndices.OUTDEGREE)) / 2);
                        rowP.put("group", community.getParticipants().get(row).getParticipantGroup().getClusterNo());
                        nodes.add(rowP);
                    }

                    if (!names.contains(community.getParticipants().get(col).getName())) {
                        names.add(community.getParticipants().get(col).getName());
                        JSONObject colP = new JSONObject();
                        colP.put("name", community.getParticipants().get(col).getName());
                        colP.put("id", col);
                        colP.put("value", (community.getParticipants().get(col).getIndices().get(CSCLIndices.INDEGREE)
                                + community.getParticipants().get(col).getIndices().get(CSCLIndices.OUTDEGREE)) / 2);
                        colP.put("group", community.getParticipants().get(col).getParticipantGroup().getClusterNo());
                        nodes.add(colP);
                    }
                }
            }
        }

        jsonObject.put("nodes", nodes);
        jsonObject.put("links", links);

        jsonObject.put("communityName", community.getName());
        jsonObject.put("week", week);

        Date sDate = community.getStartDate() != null ? community.getStartDate() : community.getFistContributionDate();
        Date lDate = community.getEndDate() != null ? community.getEndDate() : community.getLastContributionDate();
        jsonObject.put("startDate", sDate.getTime());
        jsonObject.put("endDate", lDate.getTime());

        return jsonObject;
    }

    /**
     * Create json object for hierarchical edge bundling
     *
     * @param week - week
     * @return
     */
    public JSONObject generateHierarchicalEdgeBundling(Integer week) {
        JSONObject finalResult = new JSONObject();
        JSONArray edgeBundling = new JSONArray();

        try {
            for (int row = 0; row < community.getParticipantContributions().length; row++) {
                JSONObject participantObject = new JSONObject();
                JSONArray participantJsonArray = new JSONArray();
                for (int col = 0; col < community.getParticipantContributions()[row].length; col++) {
                    if (community.getParticipantContributions()[row][col] > 0 && community.getParticipants().get(row).getParticipantGroup() != null) {
                        String cluster = community.getParticipants().get(row).getParticipantGroup().name();
                        participantObject.put("name", cluster + "/" + community.getParticipants().get(row).getName());
                        participantObject.put("size", community.getParticipants().get(row).getContributions().getBlocks().size());
                        participantObject.put("group", cluster);

                        if (community.getParticipants().get(col).getParticipantGroup() != null) {
                            String cluster1 = community.getParticipants().get(col).getParticipantGroup().name();
                            participantJsonArray.add(cluster1 + "/" + community.getParticipants().get(col).getName());
                        }

                    }
                }
                if (!participantJsonArray.isEmpty()) {
                    participantObject.put("imports", participantJsonArray);
                }

                if (!participantObject.isEmpty()) {
                    edgeBundling.add(participantObject);
                }

            }

            finalResult.put("data", edgeBundling);
            finalResult.put("communityName", community.getName());
            finalResult.put("week", week);

            Date sDate = community.getStartDate() != null ? community.getStartDate() : community.getFistContributionDate();
            Date lDate = community.getEndDate() != null ? community.getEndDate() : community.getLastContributionDate();
            finalResult.put("startDate", sDate.getTime());
            finalResult.put("endDate", lDate.getTime());
        } catch (Exception e) {
            LOGGER.error("Cannot create json array ...");
            throw new RuntimeException(e);
        }
        return finalResult;
    }

    /**
     * Write individual stats to Elasticsearch
     *
     * @param week
     * @return
     */
    public List<Map<String, Object>> writeIndividualStatsToElasticsearch(Integer week) {
        LOGGER.info("Writing Individual Stats to Elasticsearch");
        List<Map<String, Object>> participantsStats = new ArrayList<>();

        // write participant statistics
        for (int index = 0; index < community.getParticipants().size(); index++) {
            Participant p = community.getParticipants().get(index);
            if (p.getParticipantGroup() != null) {
                Map<String, Object> participantStats = new HashMap<>();
                for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                    if (CSCLindex.isIndividualStatsIndex()) {
                        participantStats.put(CSCLindex.getAcronym(), Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                    }
                }
                participantStats.put("participantName", p.getName());
                participantStats.put("participantNickname", "Member " + index);

                Date sDate = community.getStartDate() != null ? community.getStartDate() : community.getFistContributionDate();
                Date lDate = community.getEndDate() != null ? community.getEndDate() : community.getLastContributionDate();
                participantStats.put("startDate", sDate.getTime());
                participantStats.put("endDate", lDate.getTime());
                participantStats.put("communityName", community.getName());
                participantStats.put("week", week);
                participantStats.put("group", p.getParticipantGroup().name());

                participantsStats.add(participantStats);
            }
        }

        LOGGER.info("Successfully finished writing Individual Stats in Elasticsearch ...");

        return participantsStats;
    }

    /**
     * Get data fro trend chart for entire community
     *
     * @return
     */
    public List<Map<String, Object>> getContributionsForTrend() {
        List<Map<String, Object>> communityResult = new ArrayList<>();

        for (Community subCommunity : community.getTimeframeSubCommunities()) {
            List<Double> subcommunityValues = new ArrayList<>();
            for (int index = 0; index < subCommunity.getParticipants().size(); index++) {
                Participant p = subCommunity.getParticipants().get(index);

                if (p.getContributions().getNoBlocks() > 0) {
                    Double score = p.getIndices().get(CSCLIndices.SCORE);
                    subcommunityValues.add(score);
                }

            }

            subcommunityValues.sort(Comparator.naturalOrder());

            Map<String, Object> subcommunityResult = new HashMap<>();
            Date startDate = subCommunity.getStartDate() != null ? subCommunity.getStartDate() : subCommunity.getFistContributionDate();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            subcommunityResult.put("date", dateFormat.format(startDate));

            DecimalFormat df = new DecimalFormat(".##");
            subcommunityResult.put("pct05", Double.valueOf(df.format(subcommunityValues.get(Math.min((int) Math.round(.05 * subcommunityValues.size()),subcommunityValues.size()-1)))));
            subcommunityResult.put("pct25", Double.valueOf(df.format(subcommunityValues.get(Math.min((int) Math.round(.25 * subcommunityValues.size()),subcommunityValues.size()-1)))));
            subcommunityResult.put("pct50", Double.valueOf(df.format(subcommunityValues.get(Math.min((int) Math.round(.50 * subcommunityValues.size()),subcommunityValues.size()-1)))));
            subcommunityResult.put("pct75", Double.valueOf(df.format(subcommunityValues.get(Math.min((int) Math.round(.75 * subcommunityValues.size()),subcommunityValues.size()-1)))));
            subcommunityResult.put("pct95", Double.valueOf(df.format(subcommunityValues.get(Math.min((int) Math.round(.95 * subcommunityValues.size()),subcommunityValues.size()-1)))));
            communityResult.add(subcommunityResult);
        }

        return communityResult;
    }

    /**
     * Get data for timeline evolution of global participation
     * @return
     */
    public List<Map<String, Object>> getGlobalTimelineEvolution() {

        List<Map<String, Object>> communityResult = new ArrayList<>();
        for (Community subCommunity : community.getTimeframeSubCommunities()) {
            Double density = 0d;
            int noParticipants = 0;
            if (subCommunity.getEligibleContributions().getNoBlocks() > 0) {
                for (Participant participant : subCommunity.getParticipants()) {
                    if (participant.getContributions().getNoBlocks() > 0) {
                        noParticipants ++;
                    }
                }
                for (int row = 0; row < subCommunity.getParticipantContributions().length; row++) {
                    for (int col = row; col < subCommunity.getParticipantContributions()[row].length; col++) {
                        if (row!=col && subCommunity.getParticipantContributions()[row][col] > 0) {
                            density ++;
                        }
                    }
                }

                if (subCommunity.getEligibleContributions().getNoBlocks() == 1  && noParticipants == 1) {
                    density = 0.0;
                } else {
                    density = density / (noParticipants * (noParticipants - 1));
                }

            }


            Map<String, Object> subcommunityResult = new HashMap<>();

            Date startDate = subCommunity.getStartDate() != null ? subCommunity.getStartDate() : subCommunity.getFistContributionDate();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            subcommunityResult.put("date", dateFormat.format(startDate));

            subcommunityResult.put("participants", noParticipants);
            subcommunityResult.put("contributions", subCommunity.getEligibleContributions().getNoBlocks());
            subcommunityResult.put("density", density);

            communityResult.add(subcommunityResult);
        }

        return communityResult;

    }


    public Map<String, List<Integer>> getKeywordsSimilarity(double threshold, int maxTimeframeTopics) {
        AbstractDocument eligibleContributions = community.getEligibleContributions();
        eligibleContributions.determineWordOccurences(eligibleContributions.getBlocks());
        eligibleContributions.determineSemanticDimensions();
        KeywordModeling.determineKeywords(eligibleContributions, false);
        //determine maximum #maxTimeframeTopics topics that are only nouns and verbs
        List<Keyword> keywords = KeywordModeling.getSublist(eligibleContributions.getTopics(),
                maxTimeframeTopics,
                true,
                true);

        Map<String, List<Integer>> keywordsResult = new HashMap<>();
        for (Keyword t1 : keywords) {
            List<Integer> row = new ArrayList<>();
            System.out.print(t1.getWord().getLemma() + " : ");
            for (Keyword t2 : keywords) {
                int value = 0;
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getElement(), t2.getElement());
                    if (sim > threshold) {
                        value = (int) Math.ceil((sim - threshold) * 10);
                        row.add(value);
                    } else {
                        row.add(0);
                    }
                } else {
                    row.add(0);
                }
                System.out.print(value + ", ");
            }
            System.out.println("\n");
            keywordsResult.put(t1.getWord().getLemma(), row);
        }

        return keywordsResult;
    }


    /**
     * Builds keywords for heap map. Only for the entire community (week = 0).
     * @param week
     * @return
     */
    public JSONObject buildKeywordsForHeapMap(Integer week, Community community) {
        LOGGER.info("Write keywords to Elasticsearch");
        JSONObject result = new JSONObject();

        result.put("communityName", community.getName());
        result.put("week", week);
        Date startDate = community.getStartDate() != null ? community.getStartDate() : community.getFistContributionDate();
        Date endDate = community.getEndDate() != null ? community.getEndDate() : community.getLastContributionDate();
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        Map<String, double[]> keywords = exportDiscussedTopicsPerTimeframe(10, community);

        Map<String, List<Double>> finalResult = new HashMap<>();
        keywords.forEach((k,v)->{
            List<Double> values = new ArrayList<>();
            for(int i = 0; i < v.length; i ++) {
                values.add(v[i]);
            }
            finalResult.put(k, values);
        });

        result.put("data", finalResult);
        return result;
    }

    /**
     * Export discussed topics
     *
     * @param maxTimeframeTopics
     * @return
     */
    private Map<String, double[]> exportDiscussedTopicsPerTimeframe(int maxTimeframeTopics, Community community) {
        LOGGER.info("Determining discussed topics withing each timeframe  ");
        Map<String, double[]> topicsEvolution = new TreeMap<>();

        if (community.getTimeframeSubCommunities() == null || community.getTimeframeSubCommunities().isEmpty()) {
            return topicsEvolution;
        }

        for (int i = 0; i < community.getTimeframeSubCommunities().size(); i++) {
            AbstractDocument eligibleContributions = community.getTimeframeSubCommunities().get(i).getEligibleContributions();
            eligibleContributions.determineWordOccurences(eligibleContributions.getBlocks());
            eligibleContributions.determineSemanticDimensions();
            KeywordModeling.determineKeywords(eligibleContributions, false);
            //determine maximum #maxTimeframeTopics topics that are only nouns and verbs
            List<Keyword> keywords = KeywordModeling.getSublist(eligibleContributions.getTopics(),
                    maxTimeframeTopics,
                    true,
                    true);
            for (Keyword k : keywords) {
                if (!topicsEvolution.containsKey(k.getWord().getLemma())) {
                    topicsEvolution.put(k.getWord().getLemma(), new double[community.getTimeframeSubCommunities().size()]);
                }
                topicsEvolution.get(k.getWord().getLemma())[i] = k.getRelevance();
            }
        }
        return topicsEvolution;
    }
}
