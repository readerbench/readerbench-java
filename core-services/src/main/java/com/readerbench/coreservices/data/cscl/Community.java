/*
 * Copyright 2016 ReaderBench.
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
package com.readerbench.coreservices.data.cscl;

import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Community extends AnalysisElement {

    private static final long serialVersionUID = 2836361816092262953L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Community.class);

    private String name;
    private List<Participant> participants;
    private List<Conversation> conversations;
    private List<Community> timeframeSubCommunities;
    private double[][] participantContributions;
    private Date startDate, endDate;
    private Date firstContributionDate, lastContributionDate;

    public Community(String name, Lang lang, Date startDate, Date endDate) {
        super(null, 0, null, null, lang);
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        participants = new ArrayList<>();
        conversations = new ArrayList<>();
        timeframeSubCommunities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> community) {
        this.participants = community;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setDocuments(List<Conversation> documents) {
        this.conversations = documents;
    }

    public Date getFistContributionDate() {
        return firstContributionDate;
    }

    public void setFistContributionDate(Date fistContributionDate) {
        this.firstContributionDate = fistContributionDate;
    }

    public Date getLastContributionDate() {
        return lastContributionDate;
    }

    public void setLastContributionDate(Date lastContributionDate) {
        this.lastContributionDate = lastContributionDate;
    }

    public List<Community> getTimeframeSubCommunities() {
        return timeframeSubCommunities;
    }

    public void setTimeframeSubCommunities(List<Community> timeframeSubCommunities) {
        this.timeframeSubCommunities = timeframeSubCommunities;
    }

    public double[][] getParticipantContributions() {
        return participantContributions;
    }

    public void setParticipantContributions(double[][] participantContributions) {
        this.participantContributions = participantContributions;
    }
}
