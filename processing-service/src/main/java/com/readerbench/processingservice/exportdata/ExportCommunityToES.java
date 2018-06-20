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
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.datasourceprovider.commons.Formatting;

import java.util.*;

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
}
