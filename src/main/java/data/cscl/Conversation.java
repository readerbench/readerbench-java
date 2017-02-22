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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Block;
import data.discourse.SemanticChain;
import data.Lang;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import services.commons.VectorAlgebra;
import services.complexity.ComputeBalancedMeasure;
import services.discourse.CSCL.Collaboration;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.dialogism.DialogismComputations;
import services.discourse.dialogism.DialogismMeasures;
import services.discourse.keywordMining.KeywordModeling;
import services.nlp.parsing.Parsing;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

/**
 * @author Mihai Dascalu
 *
 */
public class Conversation extends AbstractDocument {

    private static final long serialVersionUID = 2096182930189552475L;

    private List<Participant> participants;
    private double[][] participantContributions;
    private List<CollaborationZone> intenseCollabZonesSocialKB;
    private List<CollaborationZone> intenseCollabZonesVoice;
    private List<CollaborationZone> annotatedCollabZones;
    private double quantCollabPercentage;
    private double socialKBPercentage;
    private double socialKBvsScore;
    // determine the distribution throughout the conversation of social KB
    private double[] socialKBEvolution;
    // determine the distribution throughout the conversation of voice PMI
    private double[] voicePMIEvolution;
    // determine the distribution of collaboration from annotations throughout the conversation
    private double[] annotatedCollabEvolution;

    /**
     * @param path
     * @param models
     * @param lang
     */
    public Conversation(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
        participants = new ArrayList<>();
        intenseCollabZonesSocialKB = new ArrayList<>();
        intenseCollabZonesVoice = new ArrayList<>();
        annotatedCollabZones = new ArrayList<>();
    }

    /**
     * @param path
     * @param contents
     * @param models
     * @param lang
     * @param usePOSTagging
     */
    public Conversation(String path, AbstractDocumentTemplate contents, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        this(path, models, lang);
        this.setText(contents.getText());
        setDocTmp(contents);
        Parsing.getParser(lang).parseDoc(contents, this, usePOSTagging);
        this.determineParticipantContributions();
    }

    /**
     * @param pathToDoc
     * @param modelPaths
     * @param lang
     * @param usePOSTagging
     * @return
     */
    public static Conversation load(String pathToDoc, Map<SimilarityType, String> modelPaths, Lang lang, boolean usePOSTagging) {
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);
        return load(new File(pathToDoc), models, lang, usePOSTagging);
    }

    /**
     * Load a conversation
     *
     * @param docFile
     * @param models
     * @param lang
     * @param usePOSTagging
     * @return
     */
    public static Conversation load(File docFile, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Conversation c = null;
        // determine contents
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        List<BlockTemplate> blocks = new ArrayList<>();

        try {
            InputSource input = new InputSource(new FileInputStream(docFile));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            Element turn, el;
            NodeList nl1, nl2;

            // reformat input accordingly to evaluation model
            nl1 = doc.getElementsByTagName("Turn");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    turn = (Element) nl1.item(i);
                    nl2 = turn.getElementsByTagName("Utterance");
                    if (nl2 != null && nl2.getLength() > 0) {
                        for (int j = 0; j < nl2.getLength(); j++) {
                            BlockTemplate block = contents.new BlockTemplate();
                            if (turn.hasAttribute("nickname") && turn.getAttribute("nickname").trim().length() > 0) {
                                block.setSpeaker(turn.getAttribute("nickname").trim());
                            } else {
                                block.setSpeaker("unregistered member");
                            }
                            el = (Element) nl2.item(j);
                            if (el.getFirstChild() != null) {
                                if (el.hasAttribute("time")) {
                                    block.setTime(el.getAttribute("time"));
                                }
                                if (el.hasAttribute("genid")) {
                                    block.setId(Integer.parseInt(el.getAttribute("genid")));
                                }
                                if (el.hasAttribute("ref")) {
                                    if (el.getAttribute("ref").isEmpty()) {
                                        block.setRefId(0);
                                    } else {
                                        try {
                                            block.setRefId(Integer.parseInt(el.getAttribute("ref")));
                                        } catch (Exception e) {
                                            block.setRefId(0);
                                        }
                                    }
                                }
                                String text = el.getFirstChild().getNodeValue();
                                block.setContent(text);
                                if (text.length() > 0
                                        && !el.getFirstChild().getNodeValue().trim().equals("joins the room")
                                        && !el.getFirstChild().getNodeValue().trim().equals("leaves the room")) {
                                    blocks.add(block);
                                }
                            }
                        }
                    }
                }
            }

            ConversationRestructuringSupport support = new ConversationRestructuringSupport();
            support.mergeAdjacentContributions(blocks);
            contents.setBlocks(support.newBlocks);
            c = new Conversation(docFile.getAbsolutePath(), contents, models, lang, usePOSTagging);
            // set title as a concatenation of topics
            String title = "";
            nl1 = doc.getElementsByTagName("Topic");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    title += el.getFirstChild().getNodeValue() + " ";
                }
                c.setDocumentTitle(title, models, lang, usePOSTagging);
            }

            if (title.length() == 0) {
                c.setDocumentTitle(docFile.getName(), models, lang, usePOSTagging);
            }

            // obtain annotator grades
            nl1 = doc.getElementsByTagName("Grades");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    nl1 = el.getElementsByTagName("General_grade");
                    if (nl1 != null && nl1.getLength() > 0) {
                        for (int j = 0; j < nl1.getLength(); j++) {
                            el = (Element) nl1.item(j);
                            if (!el.getAttribute("nickname").equals("")) {
                                double nr;
                                try {
                                    nr = Double.valueOf(el.getAttribute("value"));
                                } catch (NumberFormatException e) {
                                    nr = 0;
                                }
                                for (Participant p : c.getParticipants()) {
                                    if (p.getName().equals(el.getAttribute("nickname"))) {
                                        p.setGradeAnnotator(nr);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            double[] collabEv = new double[c.getBlocks().size()];
            nl1 = doc.getElementsByTagName("Collab_regions");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    nl1 = el.getElementsByTagName("Collab_regions_annotation");
                    if (nl1 != null && nl1.getLength() > 0) {
                        for (int j = 0; j < nl1.getLength(); j++) {
                            el = (Element) nl1.item(j);
                            String text = el.getFirstChild().getNodeValue();
                            // split annotated intense collaboration zones
                            StringTokenizer stZones = new StringTokenizer(text, ",");
                            while (stZones.hasMoreTokens()) {
                                StringTokenizer stZone = new StringTokenizer(
                                        stZones.nextToken().replaceAll("\\[", "").replaceAll("\\]", ""), ";");
                                try {
                                    int start = Integer.valueOf(stZone.nextToken());
                                    int end = Integer.valueOf(stZone.nextToken());
                                    if (support.initialMapping.containsKey(start)) {
                                        start = support.initialMapping.get(start);
                                    } else {
                                        start = -1;
                                    }

                                    if (support.initialMapping.containsKey(end)) {
                                        end = Math.min(support.initialMapping.get(end), c.getBlocks().size() - 1);
                                    } else {
                                        end = -1;
                                    }
                                    // increment accordingly the intense collaboration zones distribution
                                    if (start >= 0 && start < end) {
                                        for (int k = start; k <= end; k++) {
                                            collabEv[k]++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    LOGGER.log(Level.WARNING, "Incorrect annotated collaboration zone format...");
                                }
                            }
                        }
                        c.setAnnotatedCollabZones(Collaboration.getCollaborationZones(collabEv));
                    }
                }
            }
            c.setAnnotatedCollabEvolution(collabEv);
        } catch (FileNotFoundException | ParserConfigurationException | NumberFormatException | DOMException ex) {
            System.err.print("Error evaluating input file " + docFile.getPath() + "!");
            Exceptions.printStackTrace(ex);
        } catch (SAXException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return c;
    }

    /**
     *
     */
    private void determineParticipantContributions() {
        if (getParticipants().size() > 0) {
            for (Participant p : getParticipants()) {
                p.setContributions(new Conversation(null, getSemanticModels(), getLanguage()));
                p.setSignificantContributions(new Conversation(null, getSemanticModels(), getLanguage()));
            }
            for (Block b : getBlocks()) {
                if (b != null && ((Utterance) b).getParticipant() != null) {
                    Block.addBlock(((Utterance) b).getParticipant().getContributions(), b);
                    if (b.isSignificant()) {
                        Block.addBlock(((Utterance) b).getParticipant().getSignificantContributions(), b);
                    }
                }
            }
            for (Participant p : getParticipants()) {
                p.getContributions().determineWordOccurences(p.getContributions().getBlocks());
            }
        }
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getParticipantBlockDistribution(SemanticChain voice, Participant p) {
        double[] distribution = new double[voice.getBlockDistribution().length];
        for (int i = 0; i < getBlocks().size(); i++) {
            if (getBlocks().get(i) != null && ((Utterance) getBlocks().get(i)).getParticipant().equals(p)) {
                distribution[i] = voice.getBlockDistribution()[i];
            }
        }
        return distribution;
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getParticipantBlockMovingAverage(SemanticChain voice, Participant p) {
        double[] distribution = getParticipantBlockDistribution(voice, p);

        return VectorAlgebra.movingAverage(distribution, DialogismComputations.WINDOW_SIZE, getBlockOccurrencePattern(), DialogismComputations.MAXIMUM_INTERVAL);
    }

    /**
     * @param computeDialogism
     */
    @Override
    public void computeAll(boolean computeDialogism) {
        super.computeAll(computeDialogism);

        this.getParticipants().stream().forEach((p) -> {
            KeywordModeling.determineKeywords(p.getContributions());
        });

        Collaboration.evaluateSocialKB(this);
        setVoicePMIEvolution(DialogismMeasures.getCollaborationEvolution(this));
        // Collaboration.printIntenseCollabZones(this);

        DialogismComputations.determineParticipantInterAnimation(this);

        // evaluate participants
        ParticipantEvaluation.evaluateInteraction(this);
        ParticipantEvaluation.evaluateInvolvement(this);
        ParticipantEvaluation.performSNA(this);
        ParticipantEvaluation.evaluateUsedConcepts(this);
    }

    public void predictComplexity(String pathToComplexityModel, int[] selectedComplexityFactors) {
        if (pathToComplexityModel != null && selectedComplexityFactors != null) {
            ComputeBalancedMeasure.evaluateTextualComplexityParticipants(this, pathToComplexityModel, selectedComplexityFactors);
        }
    }

    public void exportIM() {
        LOGGER.info("Writing document export in IM format");
        File output = new File(getPath().replace(".xml", "_IM.txt"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("ID\tReference ID\tName\tTime\tText\n");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm");
            for (Block b : getBlocks()) {
                if (b != null) {
                    out.write(b.getIndex() + "\t" + ((Utterance) b).getRefBlock().getIndex() + "\t"
                            + ((Utterance) b).getParticipant().getName() + "\t" + df.format(((Utterance) b).getTime())
                            + "\t" + b.getText() + "\n");
                }
            }
        } catch (Exception ex) {
            LOGGER.severe(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    private static class ConversationRestructuringSupport {

        private Map<Integer, Integer> initialMapping;
        private List<BlockTemplate> newBlocks;

        public void mergeAdjacentContributions(List<BlockTemplate> blocks) {
            //initialization: create mapping between block IDs and initial index positions in array
            initialMapping = new TreeMap<>();
            for (int i = 0; i < blocks.size(); i++) {
                initialMapping.put(blocks.get(i).getId(), i);
            }

            //first iteration: merge contributions which have same speaker and timeframe <= 1 minute and no explicit ref other than previous contribution
            for (int i = blocks.size() - 1; i > 0; i--) {
                if (blocks.get(i) != null && blocks.get(i - 1) != null) {
                    BlockTemplate crt = blocks.get(i);
                    BlockTemplate prev = blocks.get(i - 1);
                    long diffMinutes = (crt.getTime().getTime() - prev.getTime().getTime()) / (60 * 1000);

                    //check if an explicit ref exists; in that case, perform merge only if link is between crt and previous contribution
                    boolean explicitRefCriterion = true;
                    if (crt.getRefId() != 0 && (!crt.getRefId().equals(prev.getId()))) {
                        explicitRefCriterion = false;
                    }
                    if (crt.getSpeaker().equals(prev.getSpeaker()) && diffMinutes <= 1 && explicitRefCriterion) {
                        LOGGER.log(Level.INFO, "Merging contributions with IDs {0} and {1}", new Object[]{prev.getId(), crt.getId()});
                        prev.setContent(prev.getContent() + ". " + crt.getContent());
                        blocks.set(i, null);
                    }
                }
            }

            //update refId
            for (BlockTemplate b : blocks) {
                if (b != null) {
                    if (b.getRefId() > 0) {
                        b.setRefId(initialMapping.get(b.getRefId()));
                    } else {
                        b.setRefId(null);
                    }
                }
            }

            //second iteration: fix explicit links that point now to null blocks
            for (BlockTemplate b : blocks) {
                if (b != null && b.getRefId() != null && blocks.get(b.getRefId()) == null) {
                    //determine first block which is not null above the referenced block
                    int index = b.getRefId() - 1;
                    while (blocks.get(index) == null && index >= 0) {
                        index--;
                    }
                    if (index >= 0) {
                        b.setRefId(index);
                    } else {
                        b.setRefId(null);
                    }
                }
            }

            //third iteration: remove null blocks and perform a compacting operation on the whole conversation
            newBlocks = new ArrayList<>();
            Map<Integer, Integer> newMapping = new TreeMap<>();
            int noCrt = 0;
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i) != null) {
                    newMapping.put(i, noCrt);
                    BlockTemplate crt = blocks.get(i);
                    crt.setId(noCrt);
                    if (crt.getRefId() != null) {
                        crt.setRefId(newMapping.get(crt.getRefId()));
                    }
                    newBlocks.add(crt);
                    noCrt++;
                }
            }

            //update mappings
            for (Entry<Integer, Integer> e : initialMapping.entrySet()) {
                if (newMapping.containsKey(e.getValue())) {
                    initialMapping.put(e.getKey(), newMapping.get(e.getValue()));
                } else {
                    //search for closest match
                    int index = e.getValue() - 1;
                    while (!newMapping.containsKey(index) && index >= 0) {
                        index--;
                    }
                    initialMapping.put(e.getKey(), index);
                }
            }
        }
    }

    /**
     * @return
     */
    public List<Participant> getParticipants() {
        return participants;
    }

    /**
     * @param participants
     */
    public void setParticipants(ArrayList<Participant> participants) {
        this.participants = participants;
    }

    /**
     * @return
     */
    public double[][] getParticipantContributions() {
        return participantContributions;
    }

    /**
     * @param participantContributions
     */
    public void setParticipantContributions(double[][] participantContributions) {
        this.participantContributions = participantContributions;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getIntenseCollabZonesSocialKB() {
        return intenseCollabZonesSocialKB;
    }

    /**
     * @param intenseCollabZonesSocialKB
     */
    public void setIntenseCollabZonesSocialKB(List<CollaborationZone> intenseCollabZonesSocialKB) {
        this.intenseCollabZonesSocialKB = intenseCollabZonesSocialKB;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getIntenseCollabZonesVoice() {
        return intenseCollabZonesVoice;
    }

    /**
     * @param intenseCollabZonesVoice
     */
    public void setIntenseCollabZonesVoice(List<CollaborationZone> intenseCollabZonesVoice) {
        this.intenseCollabZonesVoice = intenseCollabZonesVoice;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getAnnotatedCollabZones() {
        return annotatedCollabZones;
    }

    /**
     * @param annotatedCollabZones
     */
    public void setAnnotatedCollabZones(List<CollaborationZone> annotatedCollabZones) {
        this.annotatedCollabZones = annotatedCollabZones;
    }

    /**
     * @return
     */
    public double getQuantCollabPercentage() {
        return quantCollabPercentage;
    }

    /**
     * @param quantCollabPercentage
     */
    public void setQuantCollabPercentage(double quantCollabPercentage) {
        this.quantCollabPercentage = quantCollabPercentage;
    }

    /**
     * @return
     */
    public double getSocialKBPercentage() {
        return socialKBPercentage;
    }

    /**
     * @param socialKBPercentage
     */
    public void setSocialKBPercentage(double socialKBPercentage) {
        this.socialKBPercentage = socialKBPercentage;
    }

    /**
     * @return
     */
    public double getSocialKBvsScore() {
        return socialKBvsScore;
    }

    /**
     * @param socialKBvsScore
     */
    public void setSocialKBvsScore(double socialKBvsScore) {
        this.socialKBvsScore = socialKBvsScore;
    }

    /**
     * @return
     */
    public double[] getSocialKBEvolution() {
        return socialKBEvolution;
    }

    /**
     * @param socialKBEvolution
     */
    public void setSocialKBEvolution(double[] socialKBEvolution) {
        this.socialKBEvolution = socialKBEvolution;
    }

    /**
     * @return
     */
    public double[] getVoicePMIEvolution() {
        return voicePMIEvolution;
    }

    /**
     * @param voicePMIEvolution
     */
    public void setVoicePMIEvolution(double[] voicePMIEvolution) {
        this.voicePMIEvolution = voicePMIEvolution;
    }

    /**
     * @return
     */
    public double[] getAnnotatedCollabEvolution() {
        return annotatedCollabEvolution;
    }

    /**
     * @param annotatedCollabEvolution
     */
    public void setAnnotatedCollabEvolution(double[] annotatedCollabEvolution) {
        this.annotatedCollabEvolution = annotatedCollabEvolution;
    }
}
