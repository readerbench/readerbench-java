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
package data.document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.discourse.SemanticCohesion;
import java.util.ArrayList;
import java.util.EnumMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import services.commons.TextPreprocessing;
import services.complexity.ComplexityIndices;
import services.discourse.selfExplanations.VerbalizationAssessment;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.SimilarityType;

/**
 * A generalization of Document in which the block list for meta-cognitions
 * represents the actual list of verbalizations.
 *
 * @author Mihai Dascalu
 */
public class Metacognition extends Document {

    private static final long serialVersionUID = 3740041983851246989L;

    static final Logger LOGGER = Logger.getLogger(Metacognition.class);

    private Document referredDoc; // the initial referred document
    private SemanticCohesion[] blockSimilarities; // similarities with referred document blocks
    private final List<EnumMap<ReadingStrategyType, Integer>> automatedReadingStrategies;
    private final List<EnumMap<ReadingStrategyType, Integer>> annotatedReadingStrategies;
    private final List<SemanticCohesion> avgCohesion;
    private double annotatedFluency;
    private double annotatedComprehensionScore;
    private int comprehensionClass;
    private List<String> tutors = new LinkedList<>();

    public Metacognition(String path, AbstractDocumentTemplate docTmp, Document initialReadingMaterial, boolean usePOSTagging, boolean cleanInput) {
        // build the corresponding structure of verbalizations
        super(path, docTmp, initialReadingMaterial.getLSA(),
                initialReadingMaterial.getLDA(), initialReadingMaterial
                .getLanguage(), usePOSTagging, cleanInput);
        this.referredDoc = initialReadingMaterial;
        automatedReadingStrategies = new ArrayList<>();
        annotatedReadingStrategies = new ArrayList<>();
        avgCohesion = new ArrayList<>();
    }

    public static Metacognition loadVerbalization(String pathToDoc, Document initialReadingMaterial, boolean usePOSTagging, boolean cleanInput) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File toProcess = new File(pathToDoc);
            LOGGER.info("Processing self-explanation " + toProcess.getName() + " ...");
            InputSource input = new InputSource(new FileInputStream(toProcess));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            NodeList nl;
            // determine contents
            AbstractDocumentTemplate tmp = extractDocumentContent(doc, "verbalization");

            LOGGER.info("Building internal representation ...");
            Metacognition meta = new Metacognition(pathToDoc, tmp,
                    initialReadingMaterial, usePOSTagging, cleanInput);
            extractDocumentDescriptors(doc, meta);

            // add corresponding links from verbalizations to initial document
            nl = doc.getElementsByTagName("verbalization");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    meta.getAnnotatedRS().add(new EnumMap<>(ReadingStrategyType.class));
                    Integer after = Integer.valueOf(nl.item(i).getAttributes()
                            .getNamedItem("after_p").getNodeValue());
                    Integer id = Integer.valueOf(nl.item(i).getAttributes()
                            .getNamedItem("id").getNodeValue());
                    meta.getBlocks()
                            .get(id)
                            .setRefBlock(
                                    initialReadingMaterial.getBlocks().get(
                                            after));
                    // add annotated scores
                    try {
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.META_COGNITION, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_control").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.CAUSALITY, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_causality").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.PARAPHRASE, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_paraphrase").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.INFERRED_KNOWLEDGE, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_inferred").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.BRIDGING, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_bridging").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.TEXT_BASED_INFERENCES, meta.getAnnotatedRS().get(id).get(ReadingStrategyType.BRIDGING) + meta.getAnnotatedRS().get(id).get(ReadingStrategyType.CAUSALITY));
                    } catch (DOMException | NumberFormatException e) {
                        LOGGER.info("Verbalization " + id + " has no annotated reading strategies!");
                        Exceptions.printStackTrace(e);
                    }
                }
            }

            return meta;
        } catch (ParserConfigurationException | SAXException | IOException | ParseException | DOMException | NumberFormatException e) {
            LOGGER.error("Error evaluating input file " + pathToDoc + "!");
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    @Override
    public void exportXML(String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = writeDocumentDescriptors(dom);

            Element bodyEl = dom.createElement("self_explanations_body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < getBlocks().size(); i++) {
                if (getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("verbalization");
                    pEl.setAttribute("id", i + "");
                    pEl.setAttribute("after_p", getBlocks().get(i).getRefBlock().getIndex() + "");
                    pEl.setAttribute("no_metacognition", getAnnotatedRS().get(i).get(ReadingStrategyType.META_COGNITION) + "");
                    pEl.setAttribute("no_causality", getAnnotatedRS().get(i).get(ReadingStrategyType.CAUSALITY) + "");
                    pEl.setAttribute("no_paraphrase", getAnnotatedRS().get(i).get(ReadingStrategyType.PARAPHRASE) + "");
                    pEl.setAttribute("no_inferred", getAnnotatedRS().get(i).get(ReadingStrategyType.INFERRED_KNOWLEDGE) + "");
                    pEl.setAttribute("no_bridging", getAnnotatedRS().get(i).get(ReadingStrategyType.BRIDGING) + "");

                    pEl.setTextContent(getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * @param dom
     * @return
     */
    protected Element writeDocumentDescriptors(org.w3c.dom.Document dom) {
        Element docEl = dom.createElement("document");
        if (getLanguage() != null) {
            docEl.setAttribute("language", getLanguage().toString());
        }
        dom.appendChild(docEl);

        Element metaEl = dom.createElement("meta");
        docEl.appendChild(metaEl);

        // set source
        Element sourceEl = dom.createElement("source");
        sourceEl.setTextContent(getSource());
        metaEl.appendChild(sourceEl);

        // set author
        Element authorEl = dom.createElement("author");
        authorEl.setTextContent(getAuthors().toString());
        metaEl.appendChild(authorEl);

        // set teachers
        Element teachersEl = dom.createElement("teachers");
        metaEl.appendChild(teachersEl);
        for (String teacher : getTutors()) {
            Element teacherEl = dom.createElement("teacher");
            teacherEl.setTextContent(teacher);
            teachersEl.appendChild(teacherEl);
        }

        // set uri
        Element uriEl = dom.createElement("uri");
        uriEl.setTextContent(getURI());
        metaEl.appendChild(uriEl);

        // set date
        Element dateEl = dom.createElement("date_of_verbalization");
        DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
        dateEl.setTextContent(formatter.format(getDate()));
        metaEl.appendChild(dateEl);

        // set comprehension score
        Element comprehenstionEl = dom.createElement("comprehension_score");
        comprehenstionEl.setTextContent(getAnnotatedComprehensionScore() + "");
        metaEl.appendChild(comprehenstionEl);

        // set fluency
        Element fluencyEl = dom.createElement("fluency");
        fluencyEl.setTextContent(getAnnotatedFluency() + "");
        metaEl.appendChild(fluencyEl);

        return docEl;
    }

    protected static AbstractDocumentTemplate extractDocumentContent(Element doc, String tag) {
        Element el;
        NodeList nl;
        AbstractDocumentTemplate tmp = new AbstractDocumentTemplate();

        nl = doc.getElementsByTagName(tag);
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                BlockTemplate block = tmp.new BlockTemplate();
                block.setId(Integer.parseInt(el.getAttribute("id")));
                block.setRefId(0);
                block.setContent(TextPreprocessing.doubleCleanVerbalization(el
                        .getFirstChild().getNodeValue()));
                tmp.getBlocks().add(block);
            }
        }
        return tmp;
    }

    /**
     * @param doc
     * @param meta
     * @throws ParseException
     */
    protected static void extractDocumentDescriptors(Element doc, Metacognition meta) throws ParseException {
        Element el;
        NodeList nl;
        // get author
        nl = doc.getElementsByTagName("author");
        if (nl != null && nl.getLength() > 0) {
            el = (Element) nl.item(0);
            meta.getAuthors().add(el.getFirstChild().getNodeValue());
        }

        // get teachers
        nl = doc.getElementsByTagName("teacher");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                if (((Element) nl.item(i)).getFirstChild() != null) {
                    meta.getTutors().add(
                            ((Element) nl.item(i)).getFirstChild()
                            .getNodeValue());
                }
            }
        }

        // get source
        nl = doc.getElementsByTagName("source");
        if (nl != null && nl.getLength() > 0) {
            el = (Element) nl.item(0);
            Node firstChild = el.getFirstChild();
            if (firstChild != null && firstChild.getNodeValue() != null) {
                meta.setSource(firstChild.getNodeValue());
            }
        }

        nl = doc.getElementsByTagName("uri");
        if (nl != null && nl.getLength() > 0
                && ((Element) nl.item(0)).getFirstChild() != null) {
            meta.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }

        nl = doc.getElementsByTagName("comprehension_score");
        if (nl != null && nl.getLength() > 0
                && ((Element) nl.item(0)).getFirstChild() != null) {
            meta.setAnnotatedComprehensionScore(Double.valueOf(((Element) nl
                    .item(0)).getFirstChild().getNodeValue()));
        }

        nl = doc.getElementsByTagName("fluency");
        if (nl != null && nl.getLength() > 0
                && ((Element) nl.item(0)).getFirstChild() != null) {
            meta.setAnnotatedFluency(Double.valueOf(((Element) nl.item(0))
                    .getFirstChild().getNodeValue()));
        }

        // get date
        nl = doc.getElementsByTagName("date_of_transcription");
        if (nl != null && nl.getLength() > 0
                && ((Element) nl.item(0)).getFirstChild() != null) {
            DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
            Date date = (Date) formatter.parse(((Element) nl.item(0))
                    .getFirstChild().getNodeValue());
            meta.setDate(date);
        }
    }

    public void determineCohesion() {
        LOGGER.info("Identyfing average cohesion to previous paragraphs ..");
        // add average cohesion between verbalization and text paragraphs
        int startIndex = 0, endIndex = 0, noBlocks;

        for (int i = 0; i < this.getBlocks().size(); i++) {
            endIndex = this.getBlocks().get(i).getRefBlock().getIndex();
            noBlocks = 0;
            EnumMap<SimilarityType, Double> avgBlockCohesion = new EnumMap<>(SimilarityType.class);
            for (SimilarityType st : SimilarityType.values()) {
                avgBlockCohesion.put(st, 0d);
            }
            for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                SemanticCohesion coh = this.getBlockSimilarities()[refBlockId];
                for (SimilarityType st : SimilarityType.values()) {
                    avgBlockCohesion.put(st, avgBlockCohesion.get(st) + coh.getSemanticSimilarities().get(st));
                }
                noBlocks++;
            }
            if (noBlocks != 0) {
                for (SimilarityType st : SimilarityType.values()) {
                    avgBlockCohesion.put(st, avgBlockCohesion.get(st) / noBlocks);
                }
            }
            this.avgCohesion.add(new SemanticCohesion(avgBlockCohesion));
            startIndex = endIndex + 1;
        }
    }

    public void exportMetacognition() {
        try {
            LOGGER.info("Writing advanced document export");
            File output = new File(getPath().replace(".xml", ".csv"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768);

            out.write("Referred document:," + getReferredDoc().getTitleText() + "\n");
            out.write("Author:");
            for (String author : getAuthors()) {
                out.write("," + author);
            }
            out.write("\n");
            out.write("LSA space:," + getLSA().getPath() + "\n");
            out.write("LDA model:," + getLDA().getPath() + "\n");

            out.write("Text");
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                out.write("," + rs.getName());
            }
            for (SimilarityType st : SimilarityType.values()) {
                out.write("," + st.getName());
            }

            int startIndex = 0;
            int endIndex;
            for (int index = 0; index < getBlocks().size(); index++) {
                endIndex = getBlocks().get(index).getRefBlock().getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    // add rows as blocks within the document
                    SemanticCohesion coh = getBlockSimilarities()[refBlockId];
                    // add block text
                    out.write(getReferredDoc().getBlocks().get(refBlockId).getText().replaceAll(",", "") + ",");
                    // add cohesion
                    out.write(",,,,,," + coh.print() + "\n");
                }
                startIndex = endIndex + 1;

                // add corresponding verbalization
                out.write(getBlocks().get(index).getText().replaceAll(",", "") + ",");

                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    out.write(automatedReadingStrategies.get(index).get(rs) + ",");
                }
                for (SimilarityType st : SimilarityType.values()) {
                    out.write(avgCohesion.get(index).getSemanticSimilarities().get(st) + ",");
                }
                out.write("\n");
            }

            // add final row
            out.write("Overall reading strategies,");

            EnumMap<ReadingStrategyType, Integer> allAutomatedRS = getAllRS(automatedReadingStrategies);
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                out.write(allAutomatedRS.get(rs) + ",");
            }
            out.write("\n");

            out.close();
            LOGGER.info("Successfully finished writing file " + output.getName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public void computeAll(boolean computeDialogism, boolean saveOutput) {
        VerbalizationAssessment.detRefBlockSimilarities(this);
        ReadingStrategies.detReadingStrategies(this);

        computeDiscourseAnalysis(computeDialogism);
        ComplexityIndices.computeComplexityFactors(this);
        determineCohesion();

        if (saveOutput) {
            saveSerializedDocument();
        }
        LOGGER.info("Finished processing self-explanations ...");
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        // save serialized object - only path for LSA / LDA
        stream.defaultWriteObject();
        stream.writeObject(this.getLSA().getPath());
        stream.writeObject(this.getLDA().getPath());
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        // load serialized object - and rebuild LSA / LDA
        stream.defaultReadObject();
        LSA lsa = LSA.loadLSA((String) stream.readObject(), this.getLanguage());
        LDA lda = LDA.loadLDA((String) stream.readObject(), this.getLanguage());
        // rebuild LSA / LDA
        rebuildSemanticSpaces(lsa, lda);
        referredDoc.rebuildSemanticSpaces(lsa, lda);
    }

    //global count of reading strategies given as input argument
    public EnumMap<ReadingStrategyType, Integer> getAllRS(List<EnumMap<ReadingStrategyType, Integer>> rsList) {
        EnumMap<ReadingStrategyType, Integer> annotatedRS = new EnumMap<>(ReadingStrategyType.class);
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            annotatedRS.put(rs, 0);
        }
        for (int i = 0; i < this.getBlocks().size(); i++) {
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                annotatedRS.put(rs, annotatedRS.get(rs) + rsList.get(i).get(rs));
            }
        }
        return annotatedRS;
    }

    public Document getReferredDoc() {
        return referredDoc;
    }

    public void setReferredDoc(Document referredDoc) {
        this.referredDoc = referredDoc;
    }

    public SemanticCohesion[] getBlockSimilarities() {
        return blockSimilarities;
    }

    public void setBlockSimilarities(SemanticCohesion[] blockSimilarities) {
        this.blockSimilarities = blockSimilarities;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAutomatedRS() {
        return automatedReadingStrategies;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAnnotatedRS() {
        return annotatedReadingStrategies;
    }

    public List<String> getTutors() {
        return tutors;
    }

    public void setTutors(List<String> tutors) {
        this.tutors = tutors;
    }

    public double getAnnotatedFluency() {
        return annotatedFluency;
    }

    public void setAnnotatedFluency(double annotatedFluency) {
        this.annotatedFluency = annotatedFluency;
    }

    public double getAnnotatedComprehensionScore() {
        return annotatedComprehensionScore;
    }

    public void setAnnotatedComprehensionScore(
            double annotatedComprehensionScore) {
        this.annotatedComprehensionScore = annotatedComprehensionScore;
    }

    public int getComprehensionClass() {
        return comprehensionClass;
    }

    public void setComprehensionClass(int comprehensionClass) {
        this.comprehensionClass = comprehensionClass;
    }
}
