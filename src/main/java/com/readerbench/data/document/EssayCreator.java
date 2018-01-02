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
package com.readerbench.data.document;

import com.readerbench.data.AbstractDocumentTemplate;
import com.readerbench.data.AbstractDocumentTemplate.BlockTemplate;
import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.readerbench.services.semanticModels.ISemanticModel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EssayCreator extends Document {

    private static final Logger LOGGER = LoggerFactory.getLogger(EssayCreator.class);

    private static final long serialVersionUID = 9219491499980439568L;
    private List<String> authors = new LinkedList<>();

    public EssayCreator(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
        authors = new LinkedList<>();
    }

    public EssayCreator(String path, AbstractDocumentTemplate docTmp, 
            List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        super(path, docTmp, models, lang, usePOSTagging);
    }

    public static EssayCreator load(File docFile, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(docFile));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();

            Element el;
            NodeList nl;
            int noBreakPoints = 0;
            // determine contents
            AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
            nl = doc.getElementsByTagName("p");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    el = (Element) nl.item(i);
                    BlockTemplate block = contents.new BlockTemplate();
                    block.setId(Integer.parseInt(el.getAttribute("id")));
                    block.setRefId(0);
                    if (el.hasAttribute("verbalization_after")) {
                        block.setVerbId(noBreakPoints);
                        noBreakPoints++;
                    }
                    // block.setContent(StringEscapeUtils.escapeXml(el.getFirstChild()
                    // .getNodeValue()));
                    block.setContent(el.getFirstChild().getNodeValue());
                    contents.getBlocks().add(block);
                }

            }
            EssayCreator d = new EssayCreator(docFile.getAbsolutePath(), contents, models, lang, usePOSTagging);
            d.setNoVerbalizationBreakPoints(noBreakPoints);

            // determine title
            nl = doc.getElementsByTagName("title");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), models, lang,
                        usePOSTagging);
            }

            // get source
            nl = doc.getElementsByTagName("source");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setSource(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get authors
            nl = doc.getElementsByTagName("author");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    if (((Element) nl.item(i)).getFirstChild() != null
                            && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                        d.getAuthors().add(((Element) nl.item(i)).getFirstChild().getNodeValue());
                    }
                }
            }

            // get complexity level
            nl = doc.getElementsByTagName("complexity_level");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setComplexityLevel(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get URL
            nl = doc.getElementsByTagName("uri");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get date
            nl = doc.getElementsByTagName("date");
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
                        LOGGER.error("Incorrect date format: " + el.getFirstChild().getNodeValue());
                    }
                }
                d.setDate(date);
            }

            // get topics
            nl = doc.getElementsByTagName("Topic");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    if (((Element) nl.item(i)).getFirstChild() != null
                            && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                        String wordToAdd = ((Element) nl.item(i)).getFirstChild().getNodeValue().toLowerCase();
                        d.getInitialTopics().add(Word.getWordFromConcept(wordToAdd, lang));
                    }
                }
            }
            return d;
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException | DOMException ex) {
            LOGGER.error("Error evaluating input file " + docFile.getPath() + " - " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

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

        // set uri
        Element uriEl = dom.createElement("uri");
        uriEl.setTextContent(getURI());
        metaEl.appendChild(uriEl);

        // set date
        Element dateEl = dom.createElement("date_of_verbalization");
        DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
        dateEl.setTextContent(formatter.format(new Date()));
        metaEl.appendChild(dateEl);

        // set comprehension score
        Element comprehenstionEl = dom.createElement("comprehension_score");
        comprehenstionEl.setTextContent("");
        metaEl.appendChild(comprehenstionEl);

        // set fluency
        Element fluencyEl = dom.createElement("fluency");
        fluencyEl.setTextContent("");
        metaEl.appendChild(fluencyEl);

        return docEl;
    }

    public void exportXMLasEssay(String path, List<String> paragraphs) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = writeDocumentDescriptors(dom);

            Element bodyEl = dom.createElement("essay_body");
            docEl.appendChild(bodyEl);

            if (paragraphs != null && paragraphs.size() > 0) {
                for (int i = 0; i < paragraphs.size(); i++) {
                    if (paragraphs.get(i) != null) {
                        Element pEl = dom.createElement("p");
                        pEl.setAttribute("id", i + "");
                        pEl.setTextContent(paragraphs.get(i));
                        bodyEl.appendChild(pEl);
                    }
                }
            } else {
                for (int i = 0; i < getBlocks().size(); i++) {
                    if (getBlocks().get(i) != null) {
                        Element pEl = dom.createElement("p");
                        pEl.setAttribute("id", i + "");
                        pEl.setTextContent(getBlocks().get(i).getText());
                        bodyEl.appendChild(pEl);
                    }
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException ex) {
            LOGGER.error(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    public List<String> readFromTxt(String path) {
        List<String> paragraphs = new ArrayList<>();

        String content = null;
        try {
            Scanner scan = new Scanner(new File(path));
            content = scan.useDelimiter("\\Z").next();
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringTokenizer tokenizer = new StringTokenizer(content, "\n");
        while (tokenizer.hasMoreTokens()) {
            paragraphs.add(tokenizer.nextToken());
        }
        return paragraphs;
    }

    @Override
    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
}
