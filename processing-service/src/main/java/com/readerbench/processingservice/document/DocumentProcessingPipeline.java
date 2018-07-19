/**
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

import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.GenericProcessingPipeline;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ReaderBench
 */
public class DocumentProcessingPipeline extends GenericProcessingPipeline {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessingPipeline.class);
    
    public DocumentProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    //consider the usage of the NLP pipeline when creating a new document
    
    //consider the usage of the NLP pipeline when creating a new document
    public Document createDocumentFromXML(String path) {
        Document d = createDocumentFromTemplate(extractDocTemplateFromXML(path));
        d.setPath(path);
        addInformationFromXML(path, d);
        return d;
    }
    
    public Document createDocumentFromBlock(Block block) {
        Document d = new Document("", block.getSemanticModelsAsList(), block.getLanguage());
        Block.addBlock(d, block);
        d.determineWordOccurences(d.getBlocks());
        d.determineSemanticDimensions();
        return d;
    }
    
    public Document createDocumentFromSentence(Sentence sentence) {
       Block block = new Block(null, 0, sentence.getText(), sentence.getSemanticModelsAsList(), sentence.getLanguage());
        block.getSentences().add(sentence);
        block.setProcessedText(sentence.getProcessedText() + ". ");
        block.finalProcessing();
        return createDocumentFromBlock(block);
    }
    
    public AbstractDocumentTemplate extractDocTemplateFromXML(String path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(new File(path)));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);
            
            Element root = dom.getDocumentElement();
            return extractDocTemplateFromXML(root);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error evaluating input file " + path + " - " + e.getMessage());
            LOGGER.error(e.getMessage());
        }
        return null;
    }
    
    public AbstractDocumentTemplate extractDocTemplateFromXML(Element root) {
        // parse the XML file
        Element el;
        NodeList nl;
        int noBreakPoints = 0;
        // determine contents
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        nl = root.getElementsByTagName("p");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                AbstractDocumentTemplate.BlockTemplate block = contents.new BlockTemplate();
                if (el.hasAttribute("id")) {
                    try {
                        block.setId(Integer.parseInt(el.getAttribute("id")));
                    } catch (NumberFormatException e) {
                        block.setId(i);
                    }
                } else {
                    block.setId(i);
                }
                block.setRefId(-1);
                if (el.hasAttribute("verbalization_after")) {
                    block.setVerbId(noBreakPoints);
                    noBreakPoints++;
                }
                // block.setContent(StringEscapeUtils.escapeXml(el.getFirstChild().getNodeValue()));
                if (el.getFirstChild() != null && el.getFirstChild().getNodeValue() != null
                        && el.getFirstChild().getNodeValue().trim().length() > 0) {
                    block.setContent(el.getFirstChild().getNodeValue());
                    contents.getBlocks().add(block);
                }
            }
        }
        // determine title
        nl = root.getElementsByTagName("title");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            contents.setTitle(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }
        return contents;
    }
    
    public void addInformationFromXML(String path, Document d) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(new File(path)));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);
            
            Element root = dom.getDocumentElement();
            // parse the XML file
            Element el;
            NodeList nl;

            // determine meta
            nl = root.getElementsByTagName("meta");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get source
            nl = root.getElementsByTagName("source");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setSource(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get authors
            nl = root.getElementsByTagName("author");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    if (((Element) nl.item(i)).getFirstChild() != null
                            && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                        d.getAuthors().add(((Element) nl.item(i)).getFirstChild().getNodeValue());
                    }
                }
            }

            // get complexity level
            nl = root.getElementsByTagName("complexity_level");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setComplexityLevel(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get genre
            nl = root.getElementsByTagName("genre");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get URL
            nl = root.getElementsByTagName("uri");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get date
            nl = root.getElementsByTagName("date");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                el = (Element) nl.item(0);
                Date date = null;
                try {
                    DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
                    date = df.parse(el.getFirstChild().getNodeValue());
                } catch (ParseException e) {
                    DateFormat df2 = new SimpleDateFormat("dd.mm.yyyy");
                    try {
                        date = df2.parse(el.getFirstChild().getNodeValue());
                    } catch (ParseException e2) {
                    }
                }
                d.setDate(date);
            }

            // get topics
            nl = root.getElementsByTagName("Topic");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    if (((Element) nl.item(i)).getFirstChild() != null
                            && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                        String wordToAdd = ((Element) nl.item(i)).getFirstChild().getNodeValue().toLowerCase();
                        d.getInitialTopics().add(Parsing.getWordFromConcept(wordToAdd, d.getLanguage()));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error evaluating input file " + path + " - " + e.getMessage());
            LOGGER.error(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        
    }
}
