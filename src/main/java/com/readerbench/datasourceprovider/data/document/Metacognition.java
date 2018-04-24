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
package com.readerbench.datasourceprovider.data.document;

import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A generalization of Document in which the block list for meta-cognitions
 * represents the actual list of verbalizations.
 *
 * @author Mihai Dascalu
 */
public class Metacognition extends Document {

    private static final long serialVersionUID = 3740041983851246989L;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Metacognition.class);

    private Document referredDoc; // the initial referred document
    private SemanticCohesion[] blockSimilarities; // similarities with referred document blocks
    private final List<EnumMap<ReadingStrategyType, Integer>> automatedReadingStrategies;
    private final List<EnumMap<ReadingStrategyType, Integer>> annotatedReadingStrategies;
    private final List<SemanticCohesion> avgCohesion;
    private double annotatedFluency;
    private double annotatedComprehensionScore;
    private int comprehensionClass;
    private List<String> tutors = new LinkedList<>();

    public Metacognition(String path, AbstractDocumentTemplate docTmp, Document initialReadingMaterial, boolean usePOSTagging) {
        // build the corresponding structure of verbalizations
        super(path, docTmp, initialReadingMaterial.getSemanticModelsAsList(), initialReadingMaterial.getLanguage(), usePOSTagging);
        this.referredDoc = initialReadingMaterial;
        automatedReadingStrategies = new ArrayList<>();
        annotatedReadingStrategies = new ArrayList<>();
        avgCohesion = new ArrayList<>();
    }

    public static Metacognition loadVerbalization(String pathToDoc, Document initialReadingMaterial, boolean usePOSTagging) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File toProcess = new File(pathToDoc);
            LOGGER.info("Processing self-explanation {} ...", toProcess.getName());
            InputSource input = new InputSource(new FileInputStream(toProcess));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            NodeList nl;
            // determine contents
            AbstractDocumentTemplate tmp = extractDocumentContent(doc, "verbalization");

            LOGGER.info("Building internal representation ...");
            Metacognition meta = new Metacognition(pathToDoc, tmp, initialReadingMaterial, usePOSTagging);
            extractDocumentDescriptors(doc, meta, usePOSTagging);

            // add corresponding links from verbalizations to initial document
            nl = doc.getElementsByTagName("verbalization");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    meta.getAnnotatedRS().add(new EnumMap<>(ReadingStrategyType.class));
                    Integer after = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("after_p").getNodeValue());
                    Integer id = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
                    meta.getBlocks().get(id).setRefBlock(initialReadingMaterial.getBlocks().get(after));
                    // add annotated scores
                    try {
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.META_COGNITION, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_control").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.CAUSALITY, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_causality").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.PARAPHRASE, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_paraphrase").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.INFERRED_KNOWLEDGE, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_inferred").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.BRIDGING, Integer.valueOf(nl.item(i).getAttributes().getNamedItem("no_bridging").getNodeValue()));
                        meta.getAnnotatedRS().get(id).put(ReadingStrategyType.TEXT_BASED_INFERENCES, meta.getAnnotatedRS().get(id).get(ReadingStrategyType.BRIDGING) + meta.getAnnotatedRS().get(id).get(ReadingStrategyType.CAUSALITY));
                    } catch (DOMException | NumberFormatException e) {
                        LOGGER.info("Verbalization {} has no annotated reading strategies!", id);
                        LOGGER.error(e.getMessage());
                    }
                }
            }

            return meta;
        } catch (ParserConfigurationException | SAXException | IOException | ParseException | DOMException | NumberFormatException e) {
            LOGGER.error("Error evaluating input file {}!", pathToDoc);
            LOGGER.error(e.getMessage());
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
                    //pEl.setAttribute("after_p", getBlocks().get(i).getRefBlock().getIndex() + "");
                    if (!getAnnotatedRS().isEmpty() && i < getAnnotatedRS().size()) {
                        pEl.setAttribute("no_metacognition", getAnnotatedRS().get(i).get(ReadingStrategyType.META_COGNITION) + "");
                        pEl.setAttribute("no_causality", getAnnotatedRS().get(i).get(ReadingStrategyType.CAUSALITY) + "");
                        pEl.setAttribute("no_paraphrase", getAnnotatedRS().get(i).get(ReadingStrategyType.PARAPHRASE) + "");
                        pEl.setAttribute("no_inferred", getAnnotatedRS().get(i).get(ReadingStrategyType.INFERRED_KNOWLEDGE) + "");
                        pEl.setAttribute("no_bridging", getAnnotatedRS().get(i).get(ReadingStrategyType.BRIDGING) + "");
                    }

                    pEl.setTextContent(getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
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
                AbstractDocumentTemplate.BlockTemplate block = tmp.new BlockTemplate();
                if (el.hasAttribute("id")) {
                    try {
                        block.setId(Integer.parseInt(el.getAttribute("id")));
                    } catch (NumberFormatException e) {
                        block.setId(i);
                    }
                } else {
                    block.setId(i);
                }
                if (el.hasAttribute("after_p")) {
                    try {
                        block.setRefId(Integer.parseInt(el.getAttribute("after_p")));
                    } catch (NumberFormatException e) {
                        block.setRefId(0);
                    }
                } else {
                    block.setRefId(0);
                }

//                block.setContent(TextPreprocessing.doubleCleanVerbalization(el.getFirstChild().getNodeValue()));
                block.setContent(el.getFirstChild().getNodeValue());
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
    protected static void extractDocumentDescriptors(Element doc, Metacognition meta, boolean usePOSTagging) throws ParseException {
        Element el;
        NodeList nl;
        // determine title
        nl = doc.getElementsByTagName("title");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            meta.processDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), meta.getSemanticModelsAsList(), meta.getLanguage(), usePOSTagging);
        }

        // get author
        nl = doc.getElementsByTagName("author");
        if (nl != null && nl.getLength() > 0) {
            el = (Element) nl.item(0);
            if (el.getFirstChild() != null && el.getFirstChild().getNodeValue() != null) {
                meta.getAuthors().add(el.getFirstChild().getNodeValue());
            }
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
        LOGGER.info("Identyfing average cohesion to previous paragraphs ...");
        // add average cohesion between verbalization and text paragraphs
        int startIndex = 0, endIndex, noBlocks;

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
        LOGGER.info("Writing advanced document export");
        File output = new File(getPath().replace(".xml", ".csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            out.write("Referred document:," + getReferredDoc().getTitleText() + "\n");
            out.write("Author:");
            for (String author : getAuthors()) {
                out.write("," + author);
            }
            out.write("\n");
            for (ISemanticModel model : getSemanticModelsAsList()) {
                out.write(model.getType() + " space:," + model.getPath() + "\n");

            }

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
            LOGGER.info("Successfully finished writing file {}", output.getName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
        }
    }

    //todo - to be moved
//    @Override
//    public void computeAll(boolean computeDialogism, boolean useBigrams) {
//        VerbalizationAssessment.detRefBlockSimilarities(this);
//        ReadingStrategies.detReadingStrategies(this);
//
//        computeDiscourseAnalysis(computeDialogism, useBigrams);
//        ComplexityIndices.computeComplexityFactors(this);
//        determineCohesion();
//        LOGGER.info("Finished processing self-explanations ...");
//    }

    //global count of reading strategies given as input argument
    public EnumMap<ReadingStrategyType, Integer> getAllRS(List<EnumMap<ReadingStrategyType, Integer>> rsList) {
        EnumMap<ReadingStrategyType, Integer> cumulativeRS = new EnumMap<>(ReadingStrategyType.class);
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            cumulativeRS.put(rs, 0);
        }
        for (int i = 0; i < rsList.size(); i++) {
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                cumulativeRS.put(rs, cumulativeRS.get(rs) + rsList.get(i).get(rs));
            }
        }
        return cumulativeRS;
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
