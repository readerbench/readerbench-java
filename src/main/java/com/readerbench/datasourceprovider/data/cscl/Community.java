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
package com.readerbench.datasourceprovider.data.cscl;

import com.readerbench.datasourceprovider.data.AnalysisElement;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.importdata.ImportDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Community extends AnalysisElement {

    private static final long serialVersionUID = 2836361816092262953L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Community.class);

    private String path;
    private String name;
    private final boolean needsAnonymization;
    private List<Participant> participants;
    private List<Conversation> conversations;
    private List<Community> timeframeSubCommunities;
    private double[][] participantContributions;
    private final Date startDate, endDate;
    private Date firstContributionDate, lastContributionDate;

    public Community(String path, String name, Lang lang, boolean needsAnonymization, Date startDate, Date endDate) {
        super(null, 0, null, null, lang);
        this.path = path;
        this.name = name;
        this.needsAnonymization = needsAnonymization;
        this.startDate = startDate;
        this.endDate = endDate;
        participants = new ArrayList<>();
        conversations = new ArrayList<>();
        timeframeSubCommunities = new ArrayList<>();
    }

    public boolean isNeedsAnonymization() {
        return needsAnonymization;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    CommunityTimeProcessing
    private static Community getSubCommunity(Community community, Date startSubCommunities, Date endSubCommunities) {
        Community subCommunity = new Community(community.getPath(), community.getLanguage(), community.needsAnonymization(), startSubCommunities, endSubCommunities);
        for (Conversation c : community.getConversations()) {
            subCommunity.getConversations().add(c);
        }
        subCommunity.updateParticipantContributions();
        subCommunity.computeMetrics(false, false, false);
        return subCommunity;
    }

    public static Community loadMultipleConversations(String path, Lang lang, boolean needsAnonymization, Date startDate,
            Date endDate, int monthIncrement, int dayIncrement) {
        LOGGER.info("Loading all files in {}", path);

        FileFilter filter = (File f) -> f.getName().endsWith(".ser");
        Community community = new Community(path, lang, needsAnonymization, startDate, endDate);
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] filesTODO = dir.listFiles(filter);
        ImportDocument id = new ImportDocument();
        for (File f : filesTODO) {
            Conversation c;
            try {
                c = (Conversation) id.importSerializedDocument(f.getPath());
                community.getConversations().add(c);
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

        community.updateParticipantContributions();
        // create corresponding sub-communities
        Calendar cal = Calendar.getInstance();
        if (community.getFistContributionDate() == null) {
            LOGGER.error("first contribution date not existing");
            return null;
        }
        Date startSubCommunities = community.getFistContributionDate();
        cal.setTime(startSubCommunities);
        cal.add(Calendar.MONTH, monthIncrement);
        cal.add(Calendar.DATE, dayIncrement);
        Date endSubCommunities = cal.getTime();

        while (endSubCommunities.before(community.getLastContributionDate())) {
            community.getTimeframeSubCommunities()
                    .add(getSubCommunity(community, startSubCommunities, endSubCommunities));

            // update timeStamps
            startSubCommunities = endSubCommunities;
            cal.add(Calendar.MONTH, monthIncrement);
            cal.add(Calendar.DATE, dayIncrement);
            endSubCommunities = cal.getTime();
        }
        // create partial community with remaining contributions
        community.getTimeframeSubCommunities()
                .add(getSubCommunity(community, startSubCommunities, community.getLastContributionDate()));

        LOGGER.info("Finished creating {} timeframe sub-communities spanning from {} to {}", new Object[]{community.getTimeframeSubCommunities().size(), community.getFistContributionDate(), community.getLastContributionDate()});

        return community;
    }

    public Community loadMultipleConversations(List<Conversation> abstractDocumentList, Lang lang,
            boolean needsAnonymization, Date startDate, Date endDate,
            int monthIncrement, int dayIncrement, String path) {
        Community community = new Community(path, lang, needsAnonymization, startDate, endDate);
        community.setPath(path);
        for (Conversation abstractDocument : abstractDocumentList) {
            community.getConversations().add(abstractDocument);
        }

        community.updateParticipantContributions();

        // create corresponding sub-communities
        Calendar cal = Calendar.getInstance();
        Date startSubCommunities = community.getFistContributionDate();
        cal.setTime(startSubCommunities);
        cal.add(Calendar.MONTH, monthIncrement);
        cal.add(Calendar.DATE, dayIncrement);
        Date endSubCommunities = cal.getTime();

        while (endSubCommunities.before(community.getLastContributionDate())) {
            community.getTimeframeSubCommunities()
                    .add(getSubCommunity(community, startSubCommunities, endSubCommunities));

            // update timeStamps
            startSubCommunities = endSubCommunities;
            cal.add(Calendar.MONTH, monthIncrement);
            cal.add(Calendar.DATE, dayIncrement);
            endSubCommunities = cal.getTime();
        }
        // create partial community with remaining contributions
        community.getTimeframeSubCommunities()
                .add(getSubCommunity(community, startSubCommunities, community.getLastContributionDate()));

        LOGGER.info("Finished creating {} timeframe sub-communities spanning from {} to {}", new Object[]{community.getTimeframeSubCommunities().size(), community.getFistContributionDate(), community.getLastContributionDate()});

        return community;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean needsAnonymization() {
        return needsAnonymization;
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
