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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
import services.discourse.topicMining.TopicModeling;
import services.nlp.parsing.Parsing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 * @author Mihai Dascalu
 *
 */
public class Conversation extends AbstractDocument {

    private static final long serialVersionUID = 2096182930189552475L;

    private Set<Participant> participants;
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
     * @param lsa
     * @param lda
     * @param lang
     */
    public Conversation(String path, LSA lsa, LDA lda, Lang lang) {
        super(path, lsa, lda, lang);
        participants = new TreeSet<>();
        intenseCollabZonesSocialKB = new ArrayList<>();
        intenseCollabZonesVoice = new ArrayList<>();
        annotatedCollabZones = new ArrayList<>();
    }

    /**
     * @param path
     * @param contents
     * @param lsa
     * @param lda
     * @param lang
     * @param usePOSTagging
     */
    public Conversation(String path, AbstractDocumentTemplate contents, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging) {
        this(path, lsa, lda, lang);
        this.setText(contents.getText());
        setDocTmp(contents);
        Parsing.getParser(lang).parseDoc(contents, this, usePOSTagging);
        this.determineParticipantInterventions();
    }

    /**
     * @param pathToDoc
     * @param pathToLSA
     * @param pathToLDA
     * @param lang
     * @param usePOSTagging
     * @return
     */
    public static Conversation load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging) {
        // load also LSA vector space and LDA model
        LSA lsa = LSA.loadLSA(pathToLSA, lang);
        LDA lda = LDA.loadLDA(pathToLDA, lang);
        return load(new File(pathToDoc), lsa, lda, lang, usePOSTagging);
    }

    /**
     * Load a conversation
     *
     * @param docFile
     * @param lsa
     * @param lda
     * @param lang
     * @param usePOSTagging
     * @return
     */
    public static Conversation load(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging) {
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
            org.w3c.dom.Document dom = null;
            try {
                dom = db.parse(input);
            } catch (SAXException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            Element doc = dom.getDocumentElement();
            Element el = null;
            NodeList nl1 = null, nl2 = null;

            // reformat input accordingly to evaluation model
            nl1 = doc.getElementsByTagName("Turn");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    BlockTemplate block = contents.new BlockTemplate();
                    if (el.hasAttribute("nickname") && el.getAttribute("nickname").trim().length() > 0) {
                        block.setSpeaker(el.getAttribute("nickname").trim());
                    } else {
                        block.setSpeaker("unregistered member");
                    }

                    nl2 = el.getElementsByTagName("Utterance");
                    if (nl2 != null && nl2.getLength() > 0) {
                        for (int j = 0; j < nl2.getLength(); j++) {
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
                                // String text = StringEscapeUtils.escapeXml(el
                                // .getFirstChild().getNodeValue());
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

            contents.setBlocks(blocks);
            c = new Conversation(docFile.getAbsolutePath(), contents, lsa, lda, lang, usePOSTagging);
            // set title as a concatenation of topics
            String title = "";
            nl1 = doc.getElementsByTagName("Topic");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    title += el.getFirstChild().getNodeValue() + " ";
                }
                c.setDocumentTitle(title, lsa, lda, lang, usePOSTagging);
            }

            if (title.length() == 0) {
                c.setDocumentTitle(docFile.getName(), lsa, lda, lang, usePOSTagging);
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
                                double nr = 0;
                                try {
                                    nr = Double.valueOf(el.getAttribute("value"));
                                } catch (Exception e) {
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

            // obtain annotated collaboration zones
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
                                    // increment accordingly the intense
                                    // collaboration zones distribution
                                    if (start >= 0 && end <= c.getBlocks().size() & start < end) {
                                        for (int k = start; k <= end; k++) {
                                            collabEv[k]++;
                                        }
                                    }
                                } catch (Exception e) {
                                    LOGGER.info("Incorrect annotated collaboration zone format");
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
        }
        return c;
    }

    /**
     *
     */
    private void determineParticipantInterventions() {
        if (getParticipants().size() > 0) {
            for (Participant p : getParticipants()) {
                p.setInterventions(new Conversation(null, getLSA(), getLDA(), getLanguage()));
                p.setSignificantInterventions(new Conversation(null, getLSA(), getLDA(), getLanguage()));
            }
            for (Block b : getBlocks()) {
                if (b != null && ((Utterance) b).getParticipant() != null) {
                    Block.addBlock(((Utterance) b).getParticipant().getInterventions(), b);
                    if (b.isSignificant()) {
                        Block.addBlock(((Utterance) b).getParticipant().getSignificantInterventions(), b);
                    }
                }
            }
            for (Participant p : getParticipants()) {
                p.getInterventions().determineWordOccurences(p.getInterventions().getBlocks());
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

        return VectorAlgebra.movingAverage(distribution, DialogismComputations.WINDOW_SIZE, getBlockOccurrencePattern(),
                DialogismComputations.MAXIMUM_INTERVAL);
    }

    /**
     * @param computeDialogism
     */
    @Override
    public void computeAll(boolean computeDialogism) {
        super.computeAll(computeDialogism);

        this.getParticipants().stream().forEach((p) -> {
            TopicModeling.determineTopics(p.getInterventions());
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
            LOGGER.error(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * @param stream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        // save serialized object - only path for LSA / LDA
        stream.defaultWriteObject();
        if (getLSA() == null) {
            stream.writeObject("");
        } else {
            stream.writeObject(getLSA().getPath());
        }
        if (getLDA() == null) {
            stream.writeObject("");
        } else {
            stream.writeObject(getLDA().getPath());
        }
    }

    /**
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        // load serialized object - and rebuild LSA / LDA
        stream.defaultReadObject();
        String lsaPath = (String) stream.readObject();
        String ldaPath = (String) stream.readObject();
        LSA lsa = null;
        LDA lda = null;
        if (lsaPath != null && lsaPath.length() > 0) {
            lsa = LSA.loadLSA(lsaPath, this.getLanguage());
        }
        if (ldaPath != null && ldaPath.length() > 0) {
            lda = LDA.loadLDA(ldaPath, this.getLanguage());
        }
        // rebuild LSA / LDA
        rebuildSemanticSpaces(lsa, lda);

        // rebuild interventions document for each participant
        determineParticipantInterventions();
        // determine topics for each participant
        for (Participant p : participants) {
            TopicModeling.determineTopics(p.getInterventions());
        }

        for (Participant p : getParticipants()) {
            p.getInterventions().rebuildSemanticSpaces(getLSA(), getLDA());
            p.getSignificantInterventions().rebuildSemanticSpaces(getLSA(), getLDA());
        }
    }

    /**
     * @return
     */
    public Set<Participant> getParticipants() {
        return participants;
    }

    /**
     * @param participants
     */
    public void setParticipants(TreeSet<Participant> participants) {
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
