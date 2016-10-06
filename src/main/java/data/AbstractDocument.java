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
package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import data.cscl.CSCLIndices;
import data.cscl.CollaborationZone;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.discourse.SemanticCohesion;
import data.discourse.SemanticRelatedness;
import data.discourse.Keyword;
import data.document.Document;
import data.lexicalChains.DisambiguationGraph;
import data.lexicalChains.LexicalChain;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import java.io.FileNotFoundException;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.Collaboration;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.cohesion.DisambiguisationGraphAndLexicalChains;
import services.discourse.cohesion.SentimentAnalysis;
import services.discourse.dialogism.DialogismComputations;
import services.discourse.keywordMining.Scoring;
import services.discourse.keywordMining.KeywordModeling;
import services.nlp.parsing.Parsing;
import services.nlp.parsing.SimpleParsing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author Mihai Dascalu
 */
public abstract class AbstractDocument extends AnalysisElement {

    private static final long serialVersionUID = -6173684658096015060L;
    public static final int MIN_PERCENTAGE_CONTENT_WORDS = 2;

    public static enum DocumentType {
        DOCUMENT, CONVERSATION, ESSAY_CREATOR, METACOGNITION, SUMMARY
    };

    public static enum SaveType {
        NONE, SERIALIZED, SERIALIZED_AND_CSV_EXPORT, FULL
    };

    private String path;
    private String titleText;
    private Sentence title;
    private List<Block> blocks;
    // cohesion between a block and the overall document
    private SemanticCohesion[] blockDocDistances;
    // inter-block cohesion values
    private SemanticCohesion[][] blockDistances;
    private SemanticCohesion[][] prunnedBlockDistances;

    // semantic relatdness between a block and the overall document
    private SemanticRelatedness[] blockDocRelatedness;
    // inter-block semantic relatedness values
    private SemanticRelatedness[][] blockRelatedness;
    private SemanticRelatedness[][] prunnedBlockRelatedness;

    private AbstractDocumentTemplate docTmp;
    private String genre;
    // useful for time series analysis - 0 for documents and the difference in
    // - measures the distance between the current & the previous utterance, in
    // ms
    private long[] blockOccurrencePattern;

    private List<LexicalChain> lexicalChains;
    private DisambiguationGraph disambiguationGraph;

    protected Map<ComplexityIndex, Double> complexityIndices;

    private List<SemanticChain> voices;
    private transient List<SemanticChain> selectedVoices;

    public AbstractDocument() {
        super();
        this.blocks = new ArrayList<>();
        this.lexicalChains = new LinkedList<>();
    }

    public AbstractDocument(String path, LSA lsa, LDA lda, Lang lang) {
        this();
        this.path = path;
        setLanguage(lang);
        this.disambiguationGraph = new DisambiguationGraph(lang);
        setLSA(lsa);
        setLDA(lda);
    }

    public void rebuildSemanticSpaces(LSA lsa, LDA lda) {
        this.setLSA(lsa);
        this.setLDA(lda);
        for (Block b : getBlocks()) {
            if (b != null) {
                b.setLSA(lsa);
                b.setLDA(lda);
                if (b.getSentences() != null) {
                    for (Sentence s : b.getSentences()) {
                        s.setLSA(lsa);
                        s.setLDA(lda);
                        for (Word w : s.getAllWords()) {
                            w.setLSA(lsa);
                            w.setLDA(lda);
                        }
                    }
                }
            }
        }
        if (voices != null) {
            for (SemanticChain chain : voices) {
                chain.setLSA(lsa);
                chain.setLDA(lda);
            }
        }
    }

    public void computeAll(boolean computeDialogism) {
        computeDiscourseAnalysis(computeDialogism);
        ComplexityIndices.computeComplexityFactors(this);
    }

    public void save(SaveType saveOutput) {
        switch (saveOutput) {
            case SERIALIZED:
                saveSerializedDocument();
                break;
            case SERIALIZED_AND_CSV_EXPORT:
                saveSerializedDocument();
                exportDocument();
                break;
            case FULL:
                exportDocument();
                exportDocumentAdvanced();
                saveSerializedDocument();
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param computeDialogism
     */
    public void computeDiscourseAnalysis(boolean computeDialogism) {
        if (computeDialogism) {
            // build disambiguisation graph and lexical chains
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(this);
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(this);
            // System.out.println(d.disambiguationGraph);

            DisambiguisationGraphAndLexicalChains.buildLexicalChains(this);
            // for (LexicalChain chain : lexicalChains) {
            // System.out.println(chain);
            // }

            DisambiguisationGraphAndLexicalChains.computeWordDistances(this);
            // System.out.println(LexicalCohesion.getDocumentCohesion(this));

            // determine semantic chains / voices
            DialogismComputations.determineVoices(this);

            // determine voice distributions & importance
            DialogismComputations.determineVoiceDistributions(this);
        }

        // build coherence graph
        CohesionGraph.buildCohesionGraph(this);

        // determine topics
        KeywordModeling.determineTopics(this);
        // TopicModel.determineTopicsLDA(this);

        Scoring.score(this);
        // assign sentiment values
        SentimentAnalysis.weightSemanticValences(this);

        LOGGER.info("Finished all discourse analysis processes ...");
    }

    public void setDocumentTitle(String title, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging) {
        this.titleText = title;
        Annotation document;
        String processedText = title.replaceAll("\\s+", " ");

        if (processedText.length() > 0) {
            if (usePOSTagging) {
                // create an empty Annotation just with the given text
                document = new Annotation(processedText.replaceAll("[\\.\\!\\?\n]", ""));
                // run all Annotators on this text
                Parsing.getParser(lang).getPipeline().annotate(document);
                CoreMap sentence = document.get(SentencesAnnotation.class).get(0);

                // add corresponding block
                setTitle(Parsing.getParser(lang).processSentence(new Block(null, 0, "", lsa, lda, lang), 0, sentence));
            } else {
                setTitle(SimpleParsing.processSentence(new Block(null, 0, "", lsa, lda, lang), 0, processedText));
            }
        }
    }

    public static AbstractDocument loadGenericDocument(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang,
            boolean usePOSTagging, boolean computeDialogism, String pathToComplexityModel,
            int[] selectedComplexityFactors, boolean cleanInput, SaveType saveOutput) {
        // load also LSA vector space and LDA model
        LSA lsa = null;
        LDA lda = null;
        if (pathToLSA != null && pathToLSA.length() > 0 && new File(pathToLSA).isDirectory()) {
            lsa = LSA.loadLSA(pathToLSA, lang);
        }
        if (pathToLDA != null && pathToLDA.length() > 0 && new File(pathToLDA).isDirectory()) {
            lda = LDA.loadLDA(pathToLDA, lang);
        }
        return loadGenericDocument(new File(pathToDoc), lsa, lda, lang, usePOSTagging, computeDialogism,
                pathToComplexityModel, selectedComplexityFactors, cleanInput, saveOutput);
    }

    public static boolean checkTagsDocument(File f, String tag) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        InputSource input;
        try {
            input = new InputSource(new FileInputStream(f));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();

            // determine whether the document is a document or a chat
            NodeList nl;
            nl = doc.getElementsByTagName(tag);
            if (nl.getLength() > 0) {
                return true;
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static AbstractDocument loadGenericDocument(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging,
            boolean computeDialogism, String pathToComplexityModel, int[] selectedComplexityFactors, boolean cleanInput,
            SaveType saveOutput) {
        // parse the XML file
        LOGGER.info("Loading " + docFile.getPath() + " file for processing");
        boolean isDocument = checkTagsDocument(docFile, "p");

        boolean isChat = checkTagsDocument(docFile, "Utterance");

        if (isChat && isDocument) {
            throw new RuntimeException("Input file has an innapropriate structure as it contains tags for both documents and chats!");
        }
        if (!isChat && !isDocument) {
            throw new RuntimeException("Input file has an innapropriate structure as it not contains any tags for documents or chats!");
        }

        if (isDocument) {
            Document d = Document.load(docFile, lsa, lda, lang, usePOSTagging);
            d.computeAll(computeDialogism);
            d.save(saveOutput);
            return d;
        }
        if (isChat) {
            Conversation c = Conversation.load(docFile, lsa, lda, lang, usePOSTagging);
            c.computeAll(computeDialogism);
            c.save(saveOutput);
            return c;
        }

        return null;
    }

    public void saveSerializedDocument() {
        LOGGER.info("Saving serialized document ...");
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(new File(getPath().replace(".xml", ".ser")));
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(this);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void saveTxtDocument() {
        LOGGER.info("Saving plain text document ...");
        File output = new File(path.replace(".xml", ".txt"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            for (Block block : this.getBlocks()) {
                if (block != null) {
                    out.write(block.getText() + "\n");
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static AbstractDocument loadSerializedDocument(String path) {
        LOGGER.info("Loading serialized document " + path + " ...");
        AbstractDocument d = null;
        try (FileInputStream fIn = new FileInputStream(new File(path)); ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            d = (AbstractDocument) oIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Exceptions.printStackTrace(e);
        }
        return d;
    }

    @Override
    public String toString() {
        String s = "";
        if (title != null) {
            s += title + "\n";
        }
        s = blocks.stream().filter((b) -> (b != null)).map((b) -> b + "\n").reduce(s, String::concat);
        return s;
    }

    // Get the list of sentences of a document
    public List<Sentence> getSentencesInDocument() {
        List<Sentence> sentences = new ArrayList<>();
        this.getBlocks().stream().filter((block) -> (block != null)).forEach((block) -> {
            sentences.addAll(block.getSentences());
        });
        return sentences;
    }

    public void exportDocument() {
        LOGGER.info("Writing document export");
        File output = new File(path.replace(".xml", ".csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            if (titleText != null) {
                out.write(titleText.replaceAll(",", "").replaceAll("\\s+", " ") + "\n");
            }
            if (getLSA() != null) {
                out.write("LSA space:," + getLSA().getPath() + "\n");
            }
            if (getLDA() != null) {
                out.write("LDA model:," + getLDA().getPath() + "\n");
            }

            out.write("\nBlock Index,Ref Block Index,Participant,Date,Score,Personal Knowledge Building,Social Knowledge Building,Initial Text,Processed Text\n");
            for (Block b : blocks) {
                if (b != null) {
                    out.write(b.getIndex() + ",");
                    if (b.getRefBlock() != null) {
                        out.write(b.getRefBlock().getIndex() + "");
                    }
                    out.write(",");
                    if (b instanceof Utterance) {
                        if (((Utterance) b).getParticipant() != null) {
                            out.write(((Utterance) b).getParticipant().getName().replaceAll(",", "").replaceAll("\\s+",
                                    " "));
                        }
                        out.write(",");
                        if (((Utterance) b).getTime() != null) {
                            out.write(((Utterance) b).getTime() + "");
                        }
                        out.write(",");
                        out.write(b.getOverallScore() + "," + ((Utterance) b).getPersonalKB() + ","
                                + ((Utterance) b).getSocialKB() + "," + b.getText().replaceAll(",", "") + ","
                                + b.getProcessedText() + "\n");
                    } else {
                        out.write(",," + b.getOverallScore() + ",," + b.getText().replaceAll(",", "") + ","
                                + b.getProcessedText() + "\n");
                    }
                }
            }

            // print topics
            out.write("\nTopics - Relevance\n");
            out.write("Keyword, Relevance,Tf,LSA Sim, LDA Sim\n");
            for (Keyword t : this.getTopics()) {
                out.write(t.getWord().getLemma() + " (" + t.getWord().getPOS() + "),"
                        + Formatting.formatNumber(t.getRelevance()) + ","
                        + Formatting.formatNumber(t.getTermFrequency()) + "," + Formatting.formatNumber(t.getLSASim())
                        + "," + Formatting.formatNumber(t.getLDASim()) + "\n");
            }
            out.write("\n");

            if (this.getLDA() != null) {
                out.write("\nTopics - Clusters\n");
                Map<Integer, List<Keyword>> topicClusters = new TreeMap<>();
                this.getTopics().stream().forEach((t) -> {
                    Integer probClass = LDA.findMaxResemblance(t.getWord().getLDAProbDistribution(),
                            this.getLDAProbDistribution());
                    if (!topicClusters.containsKey(probClass)) {
                        topicClusters.put(probClass, new ArrayList<>());
                    }
                    topicClusters.get(probClass).add(t);
                });
                for (Integer cluster : topicClusters.keySet()) {
                    out.write(cluster + ":,");
                    for (Keyword t : topicClusters.get(cluster)) {
                        out.write(t.getWord().getLemma() + " (" + t.getRelevance() + "),");
                    }
                    out.write("\n");
                }
            }

            if (this instanceof Conversation) {
                out.write("\nTopics per Participant\n");
                Conversation c = (Conversation) this;
                if (c.getParticipants().size() > 0) {
                    for (Participant p : c.getParticipants()) {
                        out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ":");
                        for (Keyword t : p.getInterventions().getTopics()) {
                            out.write("," + t.getWord().getLemma() + " (" + t.getWord().getPOS() + ") - "
                                    + Formatting.formatNumber(t.getRelevance()));
                        }
                        out.write("\n");
                    }
                }

                // print participant statistics
                if (c.getParticipants().size() > 0) {
                    out.write("\nParticipant involvement and interaction\n");
                    out.write("Participant name");
                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        out.write("," + CSCLindex.getDescription());
                    }
                    out.write("\n");
                    for (Participant p : c.getParticipants()) {
                        out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                        for (CSCLIndices index : CSCLIndices.values()) {
                            out.write("," + p.getIndices().get(index));
                        }
                        out.write("\n");
                    }
                    // print interaction matrix
                    out.write("Interaction matrix\n");
                    for (Participant p : c.getParticipants()) {
                        out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                    }
                    out.write("\n");
                    Iterator<Participant> it = c.getParticipants().iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        Participant part = it.next();
                        out.write(part.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                        for (int j = 0; j < c.getParticipants().size(); j++) {
                            out.write("," + Formatting.formatNumber(c.getParticipantContributions()[i][j]));
                        }
                        i++;
                        out.write("\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getAnnotatedCollabZones().size() > 0) {
                    out.write("\nIntense collaboration zones - Annotated\n");
                    for (CollaborationZone zone : c.getAnnotatedCollabZones()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getIntenseCollabZonesSocialKB().size() > 0) {
                    out.write("\nIntense collaboration zones - Social Knowledge Building\n");
                    for (CollaborationZone zone : c.getIntenseCollabZonesSocialKB()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getIntenseCollabZonesVoice().size() > 0) {
                    out.write("\nIntense collaboration zones - Voice PMI\n");
                    for (CollaborationZone zone : c.getIntenseCollabZonesVoice()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print statistics
                double[] results = null;
                if (c.getAnnotatedCollabZones() != null && c.getAnnotatedCollabZones().size() > 0) {
                    results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesSocialKB());

                    out.write("\nOverlap between annotated collaboration zones and Social KB model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                            .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getSocialKBEvolution()));

                    results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesVoice());
                    out.write("\nOverlap between annotated collaboration zones and Voice PMI model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                            .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getVoicePMIEvolution()));
                }
                results = Collaboration.overlapCollaborationZones(c, c.getIntenseCollabZonesSocialKB(),
                        c.getIntenseCollabZonesVoice());
                out.write("\nOverlap between Social KB model and Voice PMI model\n" + "P=," + results[0] + "\nR=,"
                        + results[1] + "\nF1 score=," + results[2] + "\nr=,"
                        + VectorAlgebra.pearsonCorrelation(c.getVoicePMIEvolution(), c.getSocialKBEvolution()) + "\n");
            }

            // print lexical chains
            if (lexicalChains.size() > 0) {
                out.write("\nLexical chains\n");
                for (LexicalChain chain : lexicalChains) {
                    out.write(chain.toString() + "\n");
                }
            }

            // print cohesion measurements
            out.write("\nCohesion measurements\n");
            out.write("Items,LSA,LDA,Leacock Chodorow,Wu Palmer,Path Similarity,Distance,Overall\n");
            // block - doc
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i) != null) {
                    SemanticCohesion coh = blockDocDistances[i];
                    out.write("D - B" + blocks.get(i).getIndex() + "," + coh.print() + "\n");
                }
            }
            // pruned block-block
            for (int i = 0; i < blocks.size() - 1; i++) {
                for (int j = i + 1; j < blocks.size(); j++) {
                    if (prunnedBlockDistances[i][j] != null) {
                        SemanticCohesion coh = prunnedBlockDistances[i][j];
                        out.write("B" + i + "-B" + j + "," + coh.print() + "\n");
                    }
                }
            }
            out.write("\n");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public void exportDocumentAdvanced() {
        LOGGER.info("Writing advanced document export");
        File output = new File(path.replace(".xml", "_adv.csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("ID,Block,Text,Score,Cosine Sim LSA,Divergence LDA,Leacok Chodorow,Wu Palmer,Path Sim,Dist,Cohesion\n");
            int globalIndex = 0;
            for (Block b : blocks) {
                if (b != null) {
                    for (int index = 0; index < b.getSentences().size(); index++) {
                        Sentence u = b.getSentences().get(index);

                        out.write(globalIndex++ + ",");
                        out.write(b.getIndex() + ",");
                        out.write(u.getText().replaceAll(",", "") + ",");
                        out.write(Formatting.formatNumber(u.getOverallScore()) + ",");
                        if (index > 0) {
                            SemanticCohesion coh = b.getSentenceDistances()[index - 1][index];
                            out.write(coh.print() + "\n");
                        } else {
                            out.write("0,0,0,0,0,0,0\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Sentence getTitle() {
        return title;
    }

    public String getTitleText() {
        return titleText;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setTitle(Sentence title) {
        this.title = title;
    }

    public SemanticCohesion[][] getBlockDistances() {
        return blockDistances;
    }

    public void setBlockDistances(SemanticCohesion[][] blockDistances) {
        this.blockDistances = blockDistances;
    }

    public SemanticCohesion[][] getPrunnedBlockDistances() {
        return prunnedBlockDistances;
    }

    public void setPrunnedBlockDistances(SemanticCohesion[][] prunnedBlockDistances) {
        this.prunnedBlockDistances = prunnedBlockDistances;
    }

    public SemanticCohesion[] getBlockDocDistances() {
        return blockDocDistances;
    }

    public void setBlockDocDistances(SemanticCohesion[] blockDocDistances) {
        this.blockDocDistances = blockDocDistances;
    }

    public SemanticRelatedness[][] getBlockRelatedness() {
        return blockRelatedness;
    }

    public void setBlockRelatedness(SemanticRelatedness[][] blockRelatedness) {
        this.blockRelatedness = blockRelatedness;
    }

    public SemanticRelatedness[][] getPrunnedBlockRelatedness() {
        return prunnedBlockRelatedness;
    }

    public void setPrunnedBlockRelatedness(SemanticRelatedness[][] prunnedBlockRelatedness) {
        this.prunnedBlockRelatedness = prunnedBlockRelatedness;
    }

    public SemanticRelatedness[] getBlockDocRelatedness() {
        return blockDocRelatedness;
    }

    public void setBlockDocRelatedness(SemanticRelatedness[] blockDocRelatedness) {
        this.blockDocRelatedness = blockDocRelatedness;
    }

    public List<LexicalChain> getLexicalChains() {
        return lexicalChains;
    }

    public void setLexicalChains(List<LexicalChain> lexicalChains) {
        this.lexicalChains = lexicalChains;
    }

    public DisambiguationGraph getDisambiguationGraph() {
        return disambiguationGraph;
    }

    public final void setDisambiguationGraph(DisambiguationGraph disambiguationGraph) {
        this.disambiguationGraph = disambiguationGraph;
    }

    public Map<ComplexityIndex, Double> getComplexityIndices() {
        return complexityIndices;
    }

    public void setComplexityIndices(Map<ComplexityIndex, Double> complexityFactors) {
        this.complexityIndices = complexityFactors;
    }

    public List<SemanticChain> getVoices() {
        return voices;
    }

    public void setVoices(List<SemanticChain> voices) {
        this.voices = voices;
    }

    public List<SemanticChain> getSelectedVoices() {
        return selectedVoices;
    }

    public void setSelectedVoices(List<SemanticChain> selectedVoices) {
        this.selectedVoices = selectedVoices;
    }

    public long[] getBlockOccurrencePattern() {
        return blockOccurrencePattern;
    }

    public void setBlockOccurrencePattern(long[] blockOccurrencePattern) {
        this.blockOccurrencePattern = blockOccurrencePattern;
    }

    public String getDescription() {
        String s = this.getTitleText();
        if (this.getLSA() != null && this.getLDA() != null) {
            s += " [" + this.getLSA().getPath() + ", " + this.getLDA().getPath() + "]";
        }
        return s;
    }

    public AbstractDocumentTemplate getDocTmp() {
        return docTmp;
    }

    public final void setDocTmp(AbstractDocumentTemplate docTmp) {
        this.docTmp = docTmp;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getMinWordCoverage() {
        int noWords = 0;
        noWords = getWordOccurences().entrySet().stream().map((entry) -> entry.getValue()).reduce(noWords, Integer::sum);
        return Math.round(MIN_PERCENTAGE_CONTENT_WORDS / 100f * noWords + 0.5f);
    }

    // get voices with more words than getMinWordCoverage()
    public List<SemanticChain> getSignificantVoices() {
        if (this.getVoices() == null || this.getVoices().isEmpty()) {
            return null;
        }
        List<SemanticChain> importantVoices = new ArrayList<>();
        this.getVoices().stream().filter((chain) -> (chain.getWords().size() >= this.getMinWordCoverage())).forEach((chain) -> {
            importantVoices.add(chain);
        });
        return importantVoices;
    }

    public int getNoBlocks() {
        return (int) getBlocks().stream().filter(b -> b != null).count();
    }

    public int getNoSentences() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToInt(b -> b.getSentences().size())
                .sum();

    }

    public int getNoWords() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream())
                .mapToInt(s -> s.getAllWords().size())
                .sum();
    }

    public int getNoContentWords() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream())
                .flatMap(s -> s.getWordOccurences().values().stream())
                .mapToInt(x -> x)
                .sum();
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
