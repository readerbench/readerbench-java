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
package com.readerbench.processingservice.document;

import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.document.Metacognition;
import com.readerbench.datasourceprovider.data.document.ReadingStrategyType;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.readingstrategies.ReadingStrategies;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ReaderBench
 */
public class MetacognitionProcessingPipeline extends DocumentProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetacognitionProcessingPipeline.class);

    public MetacognitionProcessingPipeline(Lang lang, List<ISemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    //consider the usage of the NLP pipeline when creating a new document
    public Metacognition createMetacognitionFromXML(String path, Document initialReadingMaterial) {
        Metacognition m = new Metacognition(path, initialReadingMaterial);
        AbstractDocumentTemplate docTmp = extractDocumentContent(path, "verbalization");
        LOGGER.info("Building internal representation ...");
        Parsing.getParser(getLanguage()).parseDoc(docTmp, m, getAnnotators().contains(Annotators.NLP_PREPROCESSING));
        addInformationFromXML(path, m);
        addSpecificInformationFromXML(path, m);
        return m;
    }

    public void processMetacognition(Metacognition m) {
        detRefBlockSimilarities(m);
        ReadingStrategies.detReadingStrategies(m);
        processDocument(m);
        determineCohesion(m);
        LOGGER.info("Finished processing self-explanations ...");
    }

    protected void detRefBlockSimilarities(Metacognition m) {
        LOGGER.info("Building metacognition block similarities");

        // determine similarities with previous blocks from referred document
        m.setBlockSimilarities(new SemanticCohesion[m.getReferredDoc().getBlocks().size()]);

        int startIndex = 0;
        int endIndex = 0;
        for (Block v : m.getBlocks()) {
            if (v.getRefBlock() != null) {
                endIndex = v.getRefBlock().getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    m.getBlockSimilarities()[refBlockId] = new SemanticCohesion(v,
                            m.getReferredDoc().getBlocks().get(refBlockId));
                }
                startIndex = endIndex + 1;
            }
        }
    }

    protected void determineCohesion(Metacognition m) {
        LOGGER.info("Identyfing average cohesion to previous paragraphs ...");
        // add average cohesion between verbalization and text paragraphs
        int startIndex = 0, endIndex, noBlocks;

        for (int i = 0; i < m.getBlocks().size(); i++) {
            endIndex = m.getBlocks().get(i).getRefBlock().getIndex();
            noBlocks = 0;
            EnumMap<SimilarityType, Double> avgBlockCohesion = new EnumMap<>(SimilarityType.class);
            for (SimilarityType st : SimilarityType.values()) {
                avgBlockCohesion.put(st, 0d);
            }
            for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                SemanticCohesion coh = m.getBlockSimilarities()[refBlockId];
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
            m.getAvgCohesion().add(new SemanticCohesion(avgBlockCohesion));
            startIndex = endIndex + 1;
        }
    }

    protected static AbstractDocumentTemplate extractDocumentContent(String path, String tag) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File toProcess = new File(path);
            LOGGER.info("Processing self-explanation {} ...", toProcess.getName());
            InputSource input = new InputSource(new FileInputStream(toProcess));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
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
            // determine title
            nl = doc.getElementsByTagName("title");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                tmp.setTitle(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }
            return tmp;
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | NumberFormatException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param path
     * @param meta
     */
    protected static void addInformationFromXML(String path, Metacognition meta) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File toProcess = new File(path);
            LOGGER.info("Processing self-explanation {} ...", toProcess.getName());
            InputSource input = new InputSource(new FileInputStream(toProcess));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            NodeList nl;
            Element el;

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
        } catch (ParserConfigurationException | SAXException | IOException | ParseException | DOMException | NumberFormatException e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected static void addSpecificInformationFromXML(String path, Metacognition meta) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File toProcess = new File(path);
            LOGGER.info("Processing self-explanation {} ...", toProcess.getName());
            InputSource input = new InputSource(new FileInputStream(toProcess));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            NodeList nl;
            Element el;

            // add corresponding links from verbalizations to initial document
            nl = doc.getElementsByTagName("verbalization");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    meta.getAnnotatedRS().add(new EnumMap<>(ReadingStrategyType.class));
                    Integer after = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("after_p").getNodeValue());
                    Integer id = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
                    meta.getBlocks().get(id).setRefBlock(meta.getReferredDoc().getBlocks().get(after));
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
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | NumberFormatException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
