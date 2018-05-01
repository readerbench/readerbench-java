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
package com.readerbench.processingservice.exportdata;

import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.Metacognition;
import com.readerbench.coreservices.data.document.ReadingStrategyType;
import com.readerbench.coreservices.data.document.Summary;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static org.apache.hadoop.yarn.util.YarnVersionInfo.getDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ReaderBench
 */
public class ExportDocumentToXML {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDocumentToXML.class);

    public void exportXML(Document doc, String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = dom.createElement("document");
            if (doc.getLanguage() != null) {
                docEl.setAttribute("language", doc.getLanguage().toString());
            }
            dom.appendChild(docEl);

            Element metaEl = dom.createElement("meta");
            docEl.appendChild(metaEl);

            Element genreEl = dom.createElement("genre");
            genreEl.setTextContent(doc.getGenre());
            metaEl.appendChild(genreEl);

            Element titleEl = dom.createElement("title");
            titleEl.setTextContent(doc.getTitleText());
            metaEl.appendChild(titleEl);

            Element authorsEl = dom.createElement("authors");
            metaEl.appendChild(authorsEl);

            for (String author : doc.getAuthors()) {
                Element authorEl = dom.createElement("author");
                authorEl.setTextContent(author);
                authorsEl.appendChild(authorEl);
            }

            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Element dateEl = dom.createElement("date");
            dateEl.setTextContent(new StringBuilder(formatter.format(doc.getDate())).toString());
            metaEl.appendChild(dateEl);

            Element sourceEl = dom.createElement("source");
            sourceEl.setTextContent(doc.getSource());
            metaEl.appendChild(sourceEl);

            Element complexityEl = dom.createElement("complexity_level");
            complexityEl.setTextContent(doc.getComplexityLevel());
            metaEl.appendChild(complexityEl);

            Element uriEl = dom.createElement("uri");
            uriEl.setTextContent(doc.getURI());
            metaEl.appendChild(uriEl);

            Element bodyEl = dom.createElement("body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < doc.getBlocks().size(); i++) {
                if (doc.getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("p");
                    pEl.setAttribute("id", i + "");
                    if (doc.getBlocks().get(i).isFollowedByVerbalization()) {
                        pEl.setAttribute("verbalization_after", "true");
                    }
                    pEl.setTextContent(doc.getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void exportXML(Metacognition m, String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);
            Element docEl = writeDocumentDescriptors(m, dom);
            Element bodyEl = dom.createElement("self_explanations_body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < m.getBlocks().size(); i++) {
                if (m.getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("verbalization");
                    pEl.setAttribute("id", i + "");
                    //pEl.setAttribute("after_p", getBlocks().get(i).getRefBlock().getIndex() + "");
                    if (!m.getAnnotatedRS().isEmpty() && i < m.getAnnotatedRS().size()) {
                        pEl.setAttribute("no_metacognition", m.getAnnotatedRS().get(i).get(ReadingStrategyType.META_COGNITION) + "");
                        pEl.setAttribute("no_causality", m.getAnnotatedRS().get(i).get(ReadingStrategyType.CAUSALITY) + "");
                        pEl.setAttribute("no_paraphrase", m.getAnnotatedRS().get(i).get(ReadingStrategyType.PARAPHRASE) + "");
                        pEl.setAttribute("no_inferred", m.getAnnotatedRS().get(i).get(ReadingStrategyType.INFERRED_KNOWLEDGE) + "");
                        pEl.setAttribute("no_bridging", m.getAnnotatedRS().get(i).get(ReadingStrategyType.BRIDGING) + "");
                    }

                    pEl.setTextContent(m.getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }
            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void exportXML(Summary s, String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = writeDocumentDescriptors(s, dom);

            Element bodyEl = dom.createElement("summary_body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < s.getBlocks().size(); i++) {
                if (s.getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("p");
                    pEl.setAttribute("id", i + "");
                    pEl.setTextContent(s.getBlocks().get(i).getText());
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
     * @param path
     * @param dom
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    private void writeDOMforXMLexport(String path, org.w3c.dom.Document dom)
            throws TransformerFactoryConfigurationError, TransformerConfigurationException, FileNotFoundException,
            TransformerException, UnsupportedEncodingException {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty("encoding", "UTF-8");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // create string from xml tree
        OutputStream out = new FileOutputStream(path);
        DOMSource domSource = new DOMSource(dom);
        trans.transform(domSource, new StreamResult((new OutputStreamWriter(out, "UTF-8"))));
    }

    /**
     * @param path
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private org.w3c.dom.Document generateDOMforXMLexport(String path) throws ParserConfigurationException, SAXException, IOException {
        // generate a corresponding XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        File output = new File(path);
        if (!output.exists()) {
            try {
                output.createNewFile();
                try (BufferedWriter in = new BufferedWriter(new FileWriter(output))) {
                    in.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<document/>");
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document dom = db.parse(output);

        // determine existing files
        NodeList nl = dom.getElementsByTagName("document");
        if (nl != null && nl.getLength() > 0) {
            for (int i = nl.getLength() - 1; i >= 0; i--) {
                dom.removeChild((Element) nl.item(i));
            }
        }
        return dom;
    }

    protected Element writeDocumentDescriptors(Metacognition m, org.w3c.dom.Document dom) {
        Element docEl = dom.createElement("document");
        if (m.getLanguage() != null) {
            docEl.setAttribute("language", m.getLanguage().toString());
        }
        dom.appendChild(docEl);

        Element metaEl = dom.createElement("meta");
        docEl.appendChild(metaEl);

        // set source
        Element sourceEl = dom.createElement("source");
        sourceEl.setTextContent(m.getSource());
        metaEl.appendChild(sourceEl);

        // set author
        Element authorEl = dom.createElement("author");
        authorEl.setTextContent(m.getAuthors().toString());
        metaEl.appendChild(authorEl);

        // set teachers
        Element teachersEl = dom.createElement("teachers");
        metaEl.appendChild(teachersEl);
        for (String teacher : m.getTutors()) {
            Element teacherEl = dom.createElement("teacher");
            teacherEl.setTextContent(teacher);
            teachersEl.appendChild(teacherEl);
        }

        // set uri
        Element uriEl = dom.createElement("uri");
        uriEl.setTextContent(m.getURI());
        metaEl.appendChild(uriEl);

        // set date
        Element dateEl = dom.createElement("date_of_verbalization");
        DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
        dateEl.setTextContent(formatter.format(getDate()));
        metaEl.appendChild(dateEl);

        // set comprehension score
        Element comprehenstionEl = dom.createElement("comprehension_score");
        comprehenstionEl.setTextContent(m.getAnnotatedComprehensionScore() + "");
        metaEl.appendChild(comprehenstionEl);

        // set fluency
        Element fluencyEl = dom.createElement("fluency");
        fluencyEl.setTextContent(m.getAnnotatedFluency() + "");
        metaEl.appendChild(fluencyEl);

        return docEl;
    }
}
