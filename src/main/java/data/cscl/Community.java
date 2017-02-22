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
package data.cscl;

import java.awt.EventQueue;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import data.AbstractDocument;
import data.AbstractDocument.SaveType;
import data.AnalysisElement;
import data.Block;
import data.Lang;
import data.Word;
import data.discourse.Keyword;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.keywordMining.KeywordModeling;
import services.processing.SerialProcessing;
import view.widgets.cscl.ParticipantInteractionView;
import view.widgets.document.corpora.PaperConceptView;
import webService.result.ResultvCoP;
import webService.services.vCoP.CommunityInteraction;

public class Community extends AnalysisElement {

    private static final long serialVersionUID = 2836361816092262953L;

    protected static final Logger LOGGER = Logger.getLogger("");

    private static final int MIN_NO_CONTRIBUTIONS = 3;
    private static final int MIN_NO_CONTENT_WORDS = 50;

    private String path;
    private final boolean needsAnonymization;
    private List<Participant> participants;
    private List<Conversation> documents;
    private List<Community> timeframeSubCommunities;
    private double[][] participantContributions;
    private final Date startDate, endDate;
    private Date fistContributionDate, lastContributionDate;

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
                        if (u != null && u.isEligible(startDate, endDate)) {
                            // determine first timestamp of considered contributions
                            if (fistContributionDate == null) {
                                fistContributionDate = u.getTime();
                                LOGGER.log(Level.SEVERE, "Please check first contribution");
                            }
                            if (u.getTime().before(fistContributionDate)) {
                                fistContributionDate = u.getTime();
                            }
                            Calendar date = new GregorianCalendar(2010, Calendar.JANUARY, 1);
                            if (u.getTime().before(date.getTime())) {
                                LOGGER.log(Level.SEVERE, "Incorrect time! {0} / {1} : {2}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }
                            if (u.getTime().after(new Date())) {
                                LOGGER.log(Level.SEVERE, "Incorrect time! {0} / {1} : {2}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }

                            if (lastContributionDate == null) {
                                lastContributionDate = u.getTime();
                            }
                            if (u.getTime().after(lastContributionDate)) {
                                lastContributionDate = u.getTime();
                            }
                            b.setIndex(-1);
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
                if (u != null && u.isEligible(startDate, endDate)) {
                    Participant p1 = u.getParticipant();
                    int index1 = participants.indexOf(p1);
                    if (index1 >= 0) {
                        // participantContributions[index1][index1] += d
                        // .getBlocks().get(i).getCombinedScore();
                        Participant participantToUpdate = participants.get(index1);
                        participantToUpdate.getIndices().put(CSCLIndices.SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.SCORE) + u.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.PERSONAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.PERSONAL_KB) + u.getPersonalKB());
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
            LOGGER.log(Level.INFO, "Processing timeframe between {0} and {1} having {2} participants ...", new Object[]{dateFormat.format(startDate), dateFormat.format(endDate), participants.size()});
        }

        String fileName;
        if (startDate != null && endDate != null) {
            fileName = path + "/graph_" + dateFormat.format(startDate) + "_" + dateFormat.format(endDate);
        } else {
            fileName = path + "/graph_" + System.currentTimeMillis();
        }

        ParticipantEvaluation.performSNA(participants, participantContributions, true, fileName + ".pdf");
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

        export(fileName + ".csv", modelTimeEvolution, additionalInfo);

        if (useTextualComplexity) {

            // determine complexity indices
            for (Participant p : participants) {
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
                    ComplexityIndices.computeComplexityFactors(p.getSignificantContributions());
                }
            }
        }

        if (modelTimeEvolution) {
            modelEvolution();
        }
    }

    public void modelEvolution() {
        LOGGER.log(Level.INFO, "Modeling time evolution for {0} participants ...", participants.size());
        for (CSCLIndices index : CSCLIndices.values()) {
            if (index.isUsedForTimeModeling()) {
                LOGGER.log(Level.INFO, "Modeling based on {0}", index.getDescription());
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
                        LOGGER.log(Level.INFO, "Finished evaluating the time evolution of {0} participants", no);
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
        subCommunity.computeMetrics(false, false, false);
        return subCommunity;
    }

    public static Community loadMultipleConversations(String path, Lang lang, boolean needsAnonymization, Date startDate,
            Date endDate, int monthIncrement, int dayIncrement) {
        LOGGER.log(Level.INFO, "Loading all files in {0}", path);

        FileFilter filter = (File f) -> f.getName().endsWith(".ser");
        Community community = new Community(path, lang, needsAnonymization, startDate, endDate);
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] filesTODO = dir.listFiles(filter);
        for (File f : filesTODO) {
            Conversation c;
            try {
                c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
                community.getDocuments().add(c);
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
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

        LOGGER.log(Level.INFO, "Finished creating {0} timeframe sub-communities spanning from {1} to {2}", new Object[]{community.getTimeframeSubCommunities().size(), community.getFistContributionDate(), community.getLastContributionDate()});

        return community;
    }

    public void generateParticipantView(String path) {
        EventQueue.invokeLater(() -> {
            ParticipantInteractionView view = new ParticipantInteractionView(path, participants,
                    participantContributions, true, needsAnonymization);
            view.setVisible(true);
        });
    }

    /**
     * Generate participants view for communities
     *
     * @param path - location where .json files will be saved
     */
    public JSONArray generateParticipantViewSubCommunities(String path) {
        int i = 1;
        JSONArray participantsSubCommunities = new JSONArray();
        for (Community subCommunity : timeframeSubCommunities) {
            JSONObject participantSubCommunity = subCommunity.generateParticipantViewD3(path + i + ".json");

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
     * @param path - the path where json file will be saved
     */
    public JSONObject generateParticipantViewD3(String path) {

        JSONObject jsonObject = new JSONObject();

        JSONArray nodes = new JSONArray();
        for (int i = 0; i < participants.size(); i++) {
            JSONObject participant = new JSONObject();
            participant.put("name", participants.get(i).getName());
            participant.put("id", i);
            participant.put("value", participants.get(i).getGradeAnnotator());
            nodes.add(participant);
        }

        JSONArray links = new JSONArray();

        for (int row = 0; row < participantContributions.length; row++) {
            for (int col = 0; col < participantContributions[row].length; col++) {
                if (participantContributions[row][col] > 0) {
                    JSONObject link = new JSONObject();
                    link.put("source", row);
                    link.put("target", col);
                    link.put("score", participantContributions[row][col]);
                    links.add(link);
                }
            }
        }

        jsonObject.put("nodes", nodes);
        jsonObject.put("links", links);

        try {

            FileWriter file = new FileWriter(path);
            file.write(jsonObject.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }

        return jsonObject;

    }

    public void generateConceptView(String path) {
        EventQueue.invokeLater(() -> {
            PaperConceptView conceptView = new PaperConceptView(KeywordModeling.getCollectionTopics(documents), path);
            conceptView.setVisible(true);
        });
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
                    List<Keyword> topicL = new ArrayList<>();
                    Iterator<Map.Entry<Word, Double>> mapIter = KeywordModeling.getCollectionTopics(documents).entrySet()
                            .iterator();
                    while (mapIter.hasNext()) {
                        Map.Entry<Word, Double> entry = mapIter.next();
                        topicL.add(new Keyword(entry.getKey(), entry.getValue()));
                    }
                    Collections.sort(topicL);
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
            LOGGER.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public static void processDocumentCollection(String rootPath, Lang lang, boolean needsAnonymization, boolean useTextualComplexity, Date startDate, Date endDate, int monthIncrement, int dayIncrement) {
        Community dc = Community.loadMultipleConversations(rootPath, lang, needsAnonymization, startDate, endDate, monthIncrement, dayIncrement);
        if (dc != null) {
            dc.computeMetrics(useTextualComplexity, true, true);
            File f = new File(rootPath);
            dc.export(rootPath + "/" + f.getName() + ".csv", true, true);
            dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
            //dc.generateParticipantViewD3(rootPath + "/" + f.getName() + "_d3.json");
            //dc.generateParticipantViewSubCommunities(rootPath + "/" + f.getName() + "_d3_");
            dc.generateConceptView(rootPath + "/" + f.getName() + "_concepts.pdf");
        }
    }

    public static void processAllFolders(String folder, Lang lang, String prefix, boolean needsAnonymization,
            boolean restartProcessing, String pathToLSA, String pathToLDA, boolean usePOSTagging,
            boolean useTextualComplexity, Date startDate, Date endDate, int monthIncrement, int dayIncrement) {
        File dir = new File(folder);

        if (dir.isDirectory()) {
            File[] communityFolder = dir.listFiles();
            for (File f : communityFolder) {
                if (f.isDirectory() && f.getName().startsWith(prefix)) {
                    if (restartProcessing) {
                        // remove checkpoint file
                        File checkpoint = new File(f.getPath() + "/checkpoint.xml");
                        if (checkpoint.exists()) {
                            checkpoint.delete();
                        }
                    }
                    SerialProcessing.processCorpus(f.getAbsolutePath(), pathToLSA, pathToLDA, lang, usePOSTagging,
                            true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);
                    Community.processDocumentCollection(f.getAbsolutePath(), lang, needsAnonymization, useTextualComplexity,
                            startDate, endDate, monthIncrement, dayIncrement);
                }
            }
        }
        LOGGER.info("Finished processsing all files...");
    }

    public static ResultvCoP getAll(Community communityInTimeFrame, Community allCommunities) {
        return new ResultvCoP(CommunityInteraction.buildParticipantGraph(communityInTimeFrame, true),
                CommunityInteraction.buildParticipantGraph(allCommunities, true), null, null);

    }

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
        return fistContributionDate;
    }

    public void setFistContributionDate(Date fistContributionDate) {
        this.fistContributionDate = fistContributionDate;
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
