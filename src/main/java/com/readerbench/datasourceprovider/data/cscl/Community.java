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

import com.readerbench.coreservices.cna.CohesionGraph;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.cscl.ParticipantEvaluation;
import com.readerbench.coreservices.keywordMining.KeywordModeling;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.AnalysisElement;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Formatting;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.keywordmining.Keyword;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.ImportDocument;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class Community extends AnalysisElement {

    private static final long serialVersionUID = 2836361816092262953L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Community.class);

    private static final int MIN_NO_CONTRIBUTIONS = 3;
    private static final int MIN_NO_CONTENT_WORDS = 50;

    private String path;
    private final boolean needsAnonymization;
    private List<Participant> participants;
    private List<Conversation> documents;
    private List<Community> timeframeSubCommunities;
    private double[][] participantContributions;
    private final Date startDate, endDate;
    private Date firstContributionDate, lastContributionDate;

    public Community(String path, Lang lang, boolean needsAnonymization, Date startDate, Date endDate) {
        super(null, 0, null, null, lang);
        this.path = path;
        this.needsAnonymization = needsAnonymization;
        this.startDate = startDate;
        this.endDate = endDate;
        participants = new ArrayList<>();
        documents = new ArrayList<>();
        timeframeSubCommunities = new ArrayList<>();
    }

    public static int getMinNoContributions() {
        return MIN_NO_CONTRIBUTIONS;
    }

    public static int getMinNoContentWords() {
        return MIN_NO_CONTENT_WORDS;
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

    private void updateParticipantContributions() {
        for (Conversation c : documents) {
            // update the community correspondingly
            for (Participant p : c.getParticipants()) {
                if (p.getContributions().getBlocks() != null && !p.getContributions().getBlocks().isEmpty()) {
                    int index = participants.indexOf(p);
                    Participant participantToUpdate;
                    if (index >= 0) {
                        participantToUpdate = participants.get(index);
                    } else {
                        participantToUpdate = new Participant(p.getName(), c);
                        participants.add(participantToUpdate);
                    }

                    for (Block b : p.getContributions().getBlocks()) {
                        Utterance u = (Utterance) b;
                        // select contributions in imposed timeframe
                        if (u != null && u.getTime() != null && u.isEligible(startDate, endDate)) {
                            // determine first timestamp of considered contributions
                            if (firstContributionDate == null) {
                                firstContributionDate = u.getTime();
                                LOGGER.error("Please check first contribution");
                            }
                            if (u.getTime().before(firstContributionDate)) {
                                firstContributionDate = u.getTime();
                            }
                            Calendar date = new GregorianCalendar(2010, Calendar.JANUARY, 1);
                            if (u.getTime().before(date.getTime())) {
                                LOGGER.error("Incorrect time! {} / {} : {}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }
                            if (u.getTime().after(new Date())) {
                                LOGGER.error("Incorrect time! {} / {} : {}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }

                            if (lastContributionDate == null) {
                                lastContributionDate = u.getTime();
                            }
                            if (u.getTime().after(lastContributionDate)) {
                                lastContributionDate = u.getTime();
                            }
                            Block.addBlock(participantToUpdate.getContributions(), b);
                            if (b.isSignificant()) {
                                Block.addBlock(participantToUpdate.getSignificantContributions(), b);
                            }

                            participantToUpdate.getIndices().put(CSCLIndices.NO_CONTRIBUTION,
                                    participantToUpdate.getIndices().get(CSCLIndices.NO_CONTRIBUTION) + 1);

                            for (Entry<Word, Integer> entry : u.getWordOccurences().entrySet()) {
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

        participantContributions = new double[participants.size()][participants.size()];

        for (Conversation d : documents) {
            // determine strength of links
            for (int i = 0; i < d.getBlocks().size(); i++) {
                Utterance u = (Utterance) d.getBlocks().get(i);
                // select contributions in imposed timeframe
                if (u != null && u.getTime() != null && u.isEligible(startDate, endDate)) {
                    Participant p1 = u.getParticipant();
                    int index1 = participants.indexOf(p1);
                    if (index1 >= 0) {
                        participantContributions[index1][index1] += u.getScore();
                        Participant participantToUpdate = participants.get(index1);
                        participantToUpdate.getIndices().put(CSCLIndices.SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.SCORE) + u.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.SOCIAL_KB) + u.getSocialKB());

                        for (int j = 0; j < i; j++) {
                            if (d.getPrunnedBlockDistances()[i][j] != null) {
                                Participant p2 = ((Utterance) d.getBlocks().get(j)).getParticipant();
                                int index2 = participants.indexOf(p2);
                                if (index2 >= 0) {
                                    // model knowledge building effect
                                    double addedKB = d.getBlocks().get(i).getScore() * d.getPrunnedBlockDistances()[i][j].getCohesion();
                                    participantContributions[index1][index2] += addedKB;
                                }
                            }
                        }
                    }
                }
            }
            for (Participant p : d.getParticipants()) {
                if (participants.contains(p)) {
                    Participant participantToUpdate = participants.get(participants.indexOf(p));
                    participantToUpdate.getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
                            participantToUpdate.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
                            + p.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE));
                }
            }
        }
    }

    protected Set<Participant> extractArrayListfromSet(List<Participant> community) {
        Set<Participant> ls = new TreeSet<>();
        community.stream().forEach((p) -> {
            ls.add(p);
        });
        return ls;
    }

    public void computeMetrics(boolean useTextualComplexity, boolean modelTimeEvolution, boolean additionalInfo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        if (startDate != null && endDate != null && participants != null && participants.size() > 0) {
            LOGGER.info("Processing timeframe between {} and {} having {} participants ...", new Object[]{dateFormat.format(startDate), dateFormat.format(endDate), participants.size()});
        }

        String fileName;
        if (startDate != null && endDate != null) {
            fileName = path + "/graph_" + dateFormat.format(startDate) + "_" + dateFormat.format(endDate);
        } else {
            fileName = path + "/graph_" + System.currentTimeMillis();
        }

        //ParticipantEvaluation.performSNA(participants, participantContributions, true, fileName + ".pdf");
        ParticipantEvaluation.performSNA(participants, participantContributions, true, null);

        // update surface statistics
        for (AbstractDocument d : documents) {
            Participant p = null;
            for (int i = 0; i < d.getBlocks().size(); i++) {
                if (d.getBlocks().get(i) != null) {
                    if (p == null) {
                        p = ((Utterance) d.getBlocks().get(i)).getParticipant();
                        Participant participantToUpdate = participants.get(participants.indexOf(p));
                        participantToUpdate.getIndices().put(CSCLIndices.NO_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.NO_NEW_THREADS) + 1);
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_OVERALL_SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_OVERALL_SCORE)
                                + d.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB)
                                + VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE)
                                + VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                                + d.getBlocks().get(i).getText().length());
                        break;
                    }
                }
            }
        }

        participants.stream().filter((p) -> (p.getIndices().get(CSCLIndices.NO_NEW_THREADS) != 0)).forEach((p) -> {
            p.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                    p.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                    / p.getIndices().get(CSCLIndices.NO_NEW_THREADS));
        });

        //export(fileName + ".csv", modelTimeEvolution, additionalInfo);
        if (useTextualComplexity) {
            LOGGER.info(participants.toString());
            // determine complexity indices
            for (Participant p : participants) {
                LOGGER.info(p.toString());
                LOGGER.info(p.getSignificantContributions().toString());

                // establish minimum criteria
                int noContentWords = 0;
                for (Block b : p.getSignificantContributions().getBlocks()) {
                    if (b != null) {
                        for (Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
                            noContentWords += entry.getValue();
                        }
                    }
                }

                if (p.getSignificantContributions().getBlocks().size() >= MIN_NO_CONTRIBUTIONS && noContentWords >= MIN_NO_CONTENT_WORDS) {
                    // build cohesion graph for additional indices
                    CohesionGraph.buildCohesionGraph(p.getSignificantContributions());
                    //ComplexityIndices.computeComplexityFactors(p.getSignificantContributions());
                }
            }
        }

        if (modelTimeEvolution) {
            modelEvolution();
        }
    }
    
    public void modelEvolution() {
        LOGGER.info("Modeling time evolution for {} participants ...", participants.size());
        for (CSCLIndices index : CSCLIndices.values()) {
            if (index.isUsedForTimeModeling()) {
                LOGGER.info("Modeling based on {}", index.getDescription());
                int no = 0;
                for (Participant p : participants) {
                    // model time evolution of each participant
                    double[] values = new double[timeframeSubCommunities.size()];
                    for (int i = 0; i < timeframeSubCommunities.size(); i++) {
                        int localParticipantIndex = timeframeSubCommunities.get(i).getParticipants().indexOf(p);
                        if (localParticipantIndex != -1) {
                            values[i] = timeframeSubCommunities.get(i).getParticipants().get(localParticipantIndex)
                                    .getIndices().get(index);
                        }
                    }
                    if (++no % 100 == 0) {
                        LOGGER.info("Finished evaluating the time evolution of {} participants", no);
                    }
                    for (CSCLCriteria crit : CSCLCriteria.values()) {
                        p.getLongitudinalIndices().put(
                                new AbstractMap.SimpleEntry<>(index, crit),
                                CSCLCriteria.getValue(crit, values));
                    }
                }
            }
        }
    }

    private static Community getSubCommunity(Community community, Date startSubCommunities, Date endSubCommunities) {
        Community subCommunity = new Community(community.getPath(), community.getLanguage(), community.needsAnonymization(), startSubCommunities, endSubCommunities);
        for (Conversation c : community.getDocuments()) {
            subCommunity.getDocuments().add(c);
        }
        subCommunity.updateParticipantContributions();
        //todo - to be reviewed
        //subCommunity.computeMetrics(false, false, false);
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
                community.getDocuments().add(c);
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

    public com.readerbench.datasourceprovider.data.cscl.Community loadMultipleConversations(List<AbstractDocument> abstractDocumentList, Lang lang,
            boolean needsAnonymization, Date startDate, Date endDate,
            int monthIncrement, int dayIncrement, String path) {
        com.readerbench.datasourceprovider.data.cscl.Community community = new com.readerbench.datasourceprovider.data.cscl.Community(path, lang, needsAnonymization, startDate, endDate);
        community.setPath(path);
        for (AbstractDocument abstractDocument : abstractDocumentList) {
            community.getDocuments().add((com.readerbench.datasourceprovider.data.cscl.Conversation) abstractDocument);
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

    /**
     * Generate participants view for communities
     *
     * @param communityName
     * @param week
     * @return
     */
    public JSONArray generateParticipantViewSubCommunities(String communityName, Integer week) {
        int i = 1;
        JSONArray participantsSubCommunities = new JSONArray();
        for (Community subCommunity : timeframeSubCommunities) {
            JSONObject participantSubCommunity = subCommunity.generateParticipantViewD3(communityName, week);

            JSONObject subCommunityJson = new JSONObject();
            subCommunityJson.put("week", i);
            subCommunityJson.put("participants", participantSubCommunity);

            participantsSubCommunities.add(subCommunityJson);

            i++;
        }

        return participantsSubCommunities;
    }

    /**
     * Generate json file with all participants for graph representation (using
     * d3.js)
     *
     * @param communityName
     * @param week
     * @return
     */
    public JSONObject generateParticipantViewD3(String communityName, Integer week) {

        JSONObject jsonObject = new JSONObject();

        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();
        List<String> names = new ArrayList<>();

        System.out.println("participantContributions generateParticipantViewD3: ");

        for (int row = 0; row < this.participantContributions.length; row++) {
            for (int col = 0; col < this.participantContributions[row].length; col++) {
                if (this.participantContributions[row][col] > 0 && this.participants.get(row).getParticipantGroup() != null
                        && this.participants.get(col).getParticipantGroup() != null) {
                    JSONObject link = new JSONObject();
                    link.put("source", row);
                    link.put("target", col);
                    link.put("score", this.participantContributions[row][col]);
                    links.add(link);

                    if (!names.contains(this.participants.get(row).getName())) {
                        names.add(this.participants.get(row).getName());
                        JSONObject rowP = new JSONObject();
                        rowP.put("name", this.participants.get(row).getName());
                        rowP.put("id", row);

                        rowP.put("value", (this.participants.get(row).getIndices().get(CSCLIndices.INDEGREE)
                                + this.participants.get(row).getIndices().get(CSCLIndices.OUTDEGREE)) / 2);
                        rowP.put("group", this.participants.get(row).getParticipantGroup().getClusterNo());
                        nodes.add(rowP);
                    }

                    if (!names.contains(this.participants.get(col).getName())) {
                        names.add(this.participants.get(col).getName());
                        JSONObject colP = new JSONObject();
                        colP.put("name", this.participants.get(col).getName());
                        colP.put("id", col);
                        colP.put("value", (this.participants.get(col).getIndices().get(CSCLIndices.INDEGREE)
                                + this.participants.get(col).getIndices().get(CSCLIndices.OUTDEGREE)) / 2);
                        colP.put("group", this.participants.get(col).getParticipantGroup().getClusterNo());
                        nodes.add(colP);
                    }
                }
            }
        }

        jsonObject.put("nodes", nodes);
        jsonObject.put("links", links);

        jsonObject.put("communityName", communityName);
        jsonObject.put("week", week);
        jsonObject.put("startDate", getFistContributionDate().getTime());
        jsonObject.put("endDate", getLastContributionDate().getTime());

        return jsonObject;
    }

    /**
     * Create json object for hierarchical edge bundling
     *
     * @param communityName - community name
     * @param week - week
     * @return
     */
    public JSONObject generateHierarchicalEdgeBundling(String communityName, Integer week) {
        JSONObject finalResult = new JSONObject();
        JSONArray edgeBundling = new JSONArray();

        try {
            for (int row = 0; row < this.participantContributions.length; row++) {
                JSONObject participantObject = new JSONObject();
                JSONArray participantJsonArray = new JSONArray();
                for (int col = 0; col < this.participantContributions[row].length; col++) {
                    if (this.participantContributions[row][col] > 0 && this.participants.get(row).getParticipantGroup() != null) {
                        String cluster = this.participants.get(row).getParticipantGroup().name();
                        participantObject.put("name", cluster + "/" + this.participants.get(row).getName());
                        participantObject.put("size", this.participants.get(row).getContributions().getBlocks().size());
                        participantObject.put("group", cluster);

                        if (this.participants.get(col).getParticipantGroup() != null) {
                            String cluster1 = this.participants.get(col).getParticipantGroup().name();
                            participantJsonArray.add(cluster1 + "/" + this.participants.get(col).getName());
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
            finalResult.put("communityName", communityName);
            finalResult.put("week", week);
            finalResult.put("startDate", getFistContributionDate().getTime());
            finalResult.put("endDate", getLastContributionDate().getTime());
        } catch (Exception e) {
            LOGGER.error("Cannot create json array ...");
            throw new RuntimeException(e);
        }
        return finalResult;
    }

    public void export(String pathToFile, boolean modelTimeEvolution, boolean additionalInfo) {
        LOGGER.info("Writing document collection export");
        // print participant statistics
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pathToFile)), "UTF-8"), 32768)) {
            // print participant statistics
            if (participants.size() > 0) {
                out.write("Participant involvement and interaction\n");
                out.write("Participant name,Anonymized name");
                for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                    out.write("," + CSCLindex.getDescription() + "(" + CSCLindex.getAcronym() + ")");
                }
                if (modelTimeEvolution) {
                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        if (CSCLindex.isUsedForTimeModeling()) {
                            for (CSCLCriteria crit : CSCLCriteria.values()) {
                                out.write("," + crit.getDescription() + "(" + CSCLindex.getAcronym() + ")");
                            }
                        }
                    }
                }
                List<ComplexityIndex> factors = ComplexityIndices.getIndices(getLanguage());
                for (ComplexityIndex factor : factors) {
                    out.write("," + factor.getAcronym());
                }
                out.write("\n");

                for (int index = 0; index < participants.size(); index++) {
                    Participant p = participants.get(index);
                    out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);
                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        out.write("," + Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                    }
                    if (modelTimeEvolution) {
                        for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                            if (CSCLindex.isUsedForTimeModeling()) {
                                for (CSCLCriteria crit : CSCLCriteria.values()) {
                                    out.write("," + p.getLongitudinalIndices().get(new AbstractMap.SimpleEntry<>(CSCLindex, crit)));
                                }
                            }
                        }
                    }
                    for (ComplexityIndex factor : factors) {
                        if (p.getSignificantContributions().getComplexityIndices() != null) {
                            out.write("," + Formatting.formatNumber(p.getSignificantContributions().getComplexityIndices().get(factor)));
                        }
                    }
                    out.write("\n");
                }

                if (additionalInfo) {
                    // print discussed topics
                    out.write("\nDiscussed topics\n");
                    out.write("Concept,Relevance\n");
                    List<Keyword> topicL = KeywordModeling.getCollectionTopics(documents);
                    for (Keyword t : topicL) {
                        out.write(t.getWord().getLemma() + "," + t.getRelevance() + "\n");
                    }

                    // print general statistic per thread
                    out.write("\nIndividual thread statistics\n");
                    out.write("Thread path,No. contributions,No. involved paticipants,Overall score,Cummulative inter-animation,Cummulative social knowledge-building\n");
                    for (AbstractDocument d : documents) {
                        int noBlocks = 0;
                        noBlocks = d.getBlocks().stream().filter((b) -> (b != null)).map((_item) -> 1).reduce(noBlocks, Integer::sum);

                        out.write(
                                new File(d.getPath()).getName() + "," + noBlocks + ","
                                + ((Conversation) d).getParticipants().size() + ","
                                + Formatting.formatNumber(d.getScore()) + ","
                                + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()))
                                + ","
                                + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()))
                                + "\n");
                    }
                }

                // print interaction matrix
                out.write("\nInteraction matrix\n");
                for (Participant p : participants) {
                    out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                }
                out.write("\n");
                for (int i = 0; i < participants.size(); i++) {
                    out.write(participants.get(i).getName().replaceAll(",", "").replaceAll("\\s+", " "));
                    for (int j = 0; j < participants.size(); j++) {
                        out.write("," + Formatting.formatNumber(participantContributions[i][j]));
                    }
                    out.write("\n");
                }
            }
            LOGGER.info("Successfully finished writing document collection export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
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
        try (BufferedWriter outIndividualStats = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFileIndividualStats)), "UTF-8"), 32768);
                BufferedWriter outInitiation = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                        new File(pathToFileInitiation)), "UTF-8"), 32768)) {

            // print participant statistics
            if (participants.size() > 0) {
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
                for (int index = 0; index < participants.size(); index++) {
                    Participant p = participants.get(index);
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
            if (participants.size() > 0) {
                outTextualComplexity.write("Textual Complexity\n");
                outTextualComplexity.write("Participant name,Anonymized name");

                List<ComplexityIndex> factors = ComplexityIndices.getIndices(getLanguage());
                for (ComplexityIndex factor : factors) {
                    outTextualComplexity.write("," + factor.getAcronym());
                }
                outTextualComplexity.write("\n");

                for (int index = 0; index < participants.size(); index++) {
                    Participant p = participants.get(index);
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
            if (participants.size() > 0) {
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

                for (int index = 0; index < participants.size(); index++) {
                    Participant p = participants.get(index);
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

            if (participants.size() > 0) {
                // print discussed topics
                outDiscussedTopics.write("\nDiscussed topics\n");
                outDiscussedTopics.write("Concept,Relevance\n");
                List<Keyword> topicL = KeywordModeling.getCollectionTopics(documents);
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
     * // * Export individual threads statistics // * // * @param pathToFile -
     * path to file //
     * @param pathToFile
     */
    public void exportIndividualThreadStatistics(String pathToFile) {
        LOGGER.info("Writing Individual Threads Statistics export");
        try (BufferedWriter outIndividualThreadsStatistics = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(pathToFile)), "UTF-8"), 32768)) {

            if (participants.size() > 0) {
                // print general statistic per thread
                outIndividualThreadsStatistics.write("\nIndividual thread statistics\n");
                outIndividualThreadsStatistics.write("Thread path,No. contributions,No. involved paticipants,"
                        + "Overall score,Cummulative inter-animation,Cummulative social knowledge-building\n");
                for (AbstractDocument d : documents) {
                    int noBlocks = 0;
                    noBlocks = d.getBlocks().stream().filter((b) -> (b != null)).map((_item) -> 1).reduce(noBlocks, Integer::sum);

                    outIndividualThreadsStatistics.write(
                            new File(d.getPath()).getName() + "," + noBlocks + ","
                            + ((Conversation) d).getParticipants().size() + ","
                            + Formatting.formatNumber(d.getScore()) + ","
                            + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()))
                            + ","
                            + Formatting.formatNumber(VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()))
                            + "\n");
                }
            }

            LOGGER.info("Successfully finished writing Individual Threads Statistics export ...");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Write individual stats to Elasticsearch
     * @param communityName
     * @param week
     * @return
     */
    public List<Map<String, Object>> writeIndividualStatsToElasticsearch(String communityName, Integer week) {
        LOGGER.info("Writing Individual Stats to Elasticsearch");
        List<Map<String, Object>> participantsStats = new ArrayList<>();

        // write participant statistics
        for (int index = 0; index < participants.size(); index++) {
            Participant p = participants.get(index);
            if (p.getParticipantGroup() != null) {
                Map<String, Object> participantStats = new HashMap<>();
                for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                    if (CSCLindex.isIndividualStatsIndex()) {
                        participantStats.put(CSCLindex.getAcronym(), Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                    }
                }
                participantStats.put("participantName", p.getName());
                participantStats.put("participantNickname", "Member " + index);
                participantStats.put("startDate", getFistContributionDate().getTime());
                participantStats.put("endDate", getLastContributionDate().getTime());
                participantStats.put("communityName", communityName);
                participantStats.put("week", week);
                participantStats.put("group", p.getParticipantGroup().name());

                participantsStats.add(participantStats);
            }
        }

        LOGGER.info("Successfully finished writing Individual Stats in Elasticsearch ...");

        return participantsStats;
    }

//    public static void processAllFolders(String folder, Lang lang, String prefix, boolean needsAnonymization,
//            boolean restartProcessing, String pathToLSA, String pathToLDA, String pathToWord2Vec, boolean usePOSTagging,
//            boolean useTextualComplexity, boolean exportIntoCsv, boolean generateParticipantView, boolean generateParticipantViewD3, boolean generateParticipantViewSubCommunities,
//            boolean generateConceptView, Date startDate, Date endDate, int monthIncrement, int dayIncrement) {
//        File dir = new File(folder);
//
//        if (dir.isDirectory()) {
//            File[] communityFolder = dir.listFiles();
//            for (File f : communityFolder) {
//                if (f.isDirectory() && f.getName().startsWith(prefix)) {
//                    if (restartProcessing) {
//                        // remove checkpoint file
//                        File checkpoint = new File(f.getPath() + "/checkpoint.xml");
//                        if (checkpoint.exists()) {
//                            checkpoint.delete();
//                        }
//                    }
//                    SerialProcessing.processCorpus(f.getAbsolutePath(), pathToLSA, pathToLDA, pathToWord2Vec, lang, usePOSTagging,
//                            true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);
//                    Community.processDocumentCollection(f.getAbsolutePath(), lang, needsAnonymization, useTextualComplexity,
//                            exportIntoCsv, generateParticipantView, generateParticipantViewD3, generateParticipantViewSubCommunities, generateConceptView,
//                            startDate, endDate, monthIncrement, dayIncrement);
//                }
//            }
//        }
//        LOGGER.info("Finished processsing all files...");
//    }
//    public static ResultvCoP getAll(Community communityInTimeFrame, Community allCommunities) {
//        return new ResultvCoP(CommunityInteraction.buildParticipantGraph(communityInTimeFrame, true),
//                CommunityInteraction.buildParticipantGraph(allCommunities, true), null, null);
//
//    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public List<Conversation> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Conversation> documents) {
        this.documents = documents;
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
