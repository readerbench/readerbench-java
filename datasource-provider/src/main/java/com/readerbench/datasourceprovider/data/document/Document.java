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

import com.readerbench.datasourceprovider.data.*;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.lexicalChains.DisambiguationGraph;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.semanticModels.ISemanticModel;
import com.readerbench.coreservices.semanticModels.SimilarityType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Document extends AbstractDocument implements Comparable<Document> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    private static final long serialVersionUID = 9219491499980439567L;

    private List<String> authors;
    private String uri;
    private String source;
    private String complexityLevel;
    private Date date;
    private int noVerbalizationBreakPoints;
    private List<Word> initialTopics;
    private double keywordAbstractOverlap;

    public Document(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
        this.authors = new ArrayList<>();
        this.initialTopics = new ArrayList<>();
    }

    public Document(String path, AbstractDocumentTemplate docTmp, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        this(path, models, lang);
        setDocTmp(docTmp);
        Parsing.getParser(lang).parseDoc(docTmp, this, usePOSTagging);
    }

    public Document(AbstractDocumentTemplate docTmp, List<ISemanticModel> semModels, Lang lang, boolean usePOSTagging) {
        this(docTmp, semModels, lang, usePOSTagging, Parsing.getParser(lang));
    }
    
    public Document(AbstractDocumentTemplate docTmp, List<ISemanticModel> semModels,
            Lang lang, boolean usePOSTagging, Parsing parser) {
        setLanguage(lang);
        super.setSemanticModels(semModels);
        setDisambiguationGraph(new DisambiguationGraph(lang));
        setText(docTmp.getText());
        setDocTmp(docTmp);

        parser.parseDoc(docTmp, this, usePOSTagging);
    }

    public static Document load(String pathToDoc, Map<SimilarityType, String> modelPaths, Lang lang, boolean usePOSTagging) {
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);
        return load(new File(pathToDoc), models, lang, usePOSTagging);
    }

    public static Document load(File docFile, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(docFile));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            return load(docFile.getAbsolutePath(), doc, models, lang, usePOSTagging);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error evaluating input file " + docFile.getPath() + " - " + e.getMessage());
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static Document load(String inputPath, Element root, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
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
                BlockTemplate block = contents.new BlockTemplate();
                if (el.hasAttribute("id")) {
                    try {
                        block.setId(Integer.parseInt(el.getAttribute("id")));
                    } catch (Exception e) {
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
                // block.setContent(StringEscapeUtils.escapeXml(el.getFirstChild()
                // .getNodeValue()));
                if (el.getFirstChild() != null && el.getFirstChild().getNodeValue() != null
                        && el.getFirstChild().getNodeValue().trim().length() > 0) {
                    block.setContent(el.getFirstChild().getNodeValue());
                    contents.getBlocks().add(block);
                }
            }
        }
        Document d = new Document(inputPath, contents, models, lang, usePOSTagging);
        d.setNoVerbalizationBreakPoints(noBreakPoints);

        // determine title
        nl = root.getElementsByTagName("title");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), models, lang, usePOSTagging);
        }

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
                    d.getInitialTopics().add(Word.getWordFromConcept(wordToAdd, lang));
                }
            }
        }
        return d;
    }

    public void addToXML(org.w3c.dom.Document doc, Element parent) {
        for (int i = 0; i < getBlocks().size(); i++) {
            Block b = getBlocks().get(i);
            if (b != null) {
                Element pEl = doc.createElement("p");
                if (b.getIndex() != -1) {
                    pEl.setAttribute("id", b.getIndex() + "");
                }
                if (b.isFollowedByVerbalization()) {
                    pEl.setAttribute("verbalization_after", "true");
                }
                pEl.setTextContent(getBlocks().get(i).getText());
                parent.appendChild(pEl);
            }
        }
    }

    public void exportXML(String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = dom.createElement("document");
            if (getLanguage() != null) {
                docEl.setAttribute("language", getLanguage().toString());
            }
            dom.appendChild(docEl);

            Element metaEl = dom.createElement("meta");
            docEl.appendChild(metaEl);

            Element genreEl = dom.createElement("genre");
            genreEl.setTextContent(getGenre());
            metaEl.appendChild(genreEl);

            Element titleEl = dom.createElement("title");
            titleEl.setTextContent(getTitleText());
            metaEl.appendChild(titleEl);

            Element authorsEl = dom.createElement("authors");
            metaEl.appendChild(authorsEl);

            for (String author : this.authors) {
                Element authorEl = dom.createElement("author");
                authorEl.setTextContent(author);
                authorsEl.appendChild(authorEl);
            }

            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Element dateEl = dom.createElement("date");
            dateEl.setTextContent(new StringBuilder(formatter.format(date)).toString());
            metaEl.appendChild(dateEl);

            Element sourceEl = dom.createElement("source");
            sourceEl.setTextContent(this.source);
            metaEl.appendChild(sourceEl);

            Element complexityEl = dom.createElement("complexity_level");
            complexityEl.setTextContent(this.complexityLevel);
            metaEl.appendChild(complexityEl);

            Element uriEl = dom.createElement("uri");
            uriEl.setTextContent(this.uri);
            metaEl.appendChild(uriEl);

            Element bodyEl = dom.createElement("body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < getBlocks().size(); i++) {
                if (getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("p");
                    pEl.setAttribute("id", i + "");
                    if (getBlocks().get(i).isFollowedByVerbalization()) {
                        pEl.setAttribute("verbalization_after", "true");
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
     * @param path
     * @param dom
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    protected void writeDOMforXMLexport(String path, org.w3c.dom.Document dom)
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
    protected org.w3c.dom.Document generateDOMforXMLexport(String path) throws ParserConfigurationException, SAXException, IOException {
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

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNoVerbalizationBreakPoints() {
        return noVerbalizationBreakPoints;
    }

    public void setNoVerbalizationBreakPoints(int noVerbalizationBreakPoints) {
        this.noVerbalizationBreakPoints = noVerbalizationBreakPoints;
    }

    public String getFullDescription() {
        StringBuilder descr = new StringBuilder();
        if (this.getPath() != null) {
            descr.append(this.getPath()).append("_");
        }
        if (this.getTitleText() != null) {
            descr.append(this.getTitleText()).append("_");
        }
        for (ISemanticModel model : getSemanticModels()) {
            descr.append(model.getPath()).append("_");
        }
        if (this.getAuthors() != null) {
            descr.append(this.getAuthors().stream().map((author) -> author + "_").reduce("", String::concat));
        }
        if (this.getText() != null) {
            descr.append(this.getText()).append("_");
        }
        return descr.toString();
    }

    @Override
    public int hashCode() {
        return this.getFullDescription().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null) {
            return false;
        }
        Document d = (Document) obj;
        return this.getFullDescription().equals(d.getFullDescription());
    }

    @Override
    public int compareTo(Document d) {
        String compare1 = "", compare2 = "";
        if (this.getPath() != null && d.getPath() != null) {
            compare1 += this.getPath() + "_";
            compare2 += d.getPath() + "_";
        }
        if (this.getTitleText() != null && d.getTitleText() != null) {
            compare1 += this.getTitleText() + "_";
            compare2 += d.getTitleText() + "_";
        }
        if (this.getAuthors() != null && d.getAuthors() != null) {
            compare1 = this.getAuthors().stream().map((author) -> author + "_").reduce(compare1, String::concat);
            compare2 = d.getAuthors().stream().map((author) -> author + "_").reduce(compare2, String::concat);
        }
        if (this.getText() != null && d.getText() != null) {
            compare1 += this.getText() + "_";
            compare2 += d.getText() + "_";
        }
        return compare1.compareTo(compare2);
    }

    public List<Word> getInitialTopics() {
        return initialTopics;
    }

    public void setInitialTopics(List<Word> initialTopics) {
        this.initialTopics = initialTopics;
    }

    public double getKeywordAbstractOverlap() {
        return keywordAbstractOverlap;
    }

    public void setKeywordAbstractOverlap(double keywordAbstractOverlap) {
        this.keywordAbstractOverlap = keywordAbstractOverlap;
    }
}
