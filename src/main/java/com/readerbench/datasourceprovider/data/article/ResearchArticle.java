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
package com.readerbench.datasourceprovider.data.article;

import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResearchArticle extends Document {

    private static final long serialVersionUID = 9219491499980439567L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchArticle.class);

    private List<String> citationURIList = new ArrayList<>();
    private List<ArticleAuthor> articleAuthorList = new ArrayList<>();

    public ResearchArticle(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
    }

    public ResearchArticle(String path, AbstractDocumentTemplate docTmp,
            List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        super(path, docTmp, models, lang, usePOSTagging);
    }
    
    public ResearchArticle(AbstractDocumentTemplate docTmp, List<ISemanticModel> semModels, Lang lang, boolean usePOSTagging) {
        super(docTmp, semModels, lang, usePOSTagging);
    }

    public static ResearchArticle load(String pathToDoc, Map<SimilarityType, String> modelPaths,
            Lang lang, boolean usePOSTagging, boolean cleanInput) {
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);
        return load(new File(pathToDoc), models, lang, usePOSTagging, cleanInput);
    }

    public static ResearchArticle load(File docFile, List<ISemanticModel> models, Lang lang, boolean usePOSTagging, boolean cleanInput) {
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
                    AbstractDocumentTemplate.BlockTemplate block = contents.new BlockTemplate();
                    if (el.hasAttribute("id")) {
                        try {
                            block.setId(Integer.parseInt(el.getAttribute("id")));
                        } catch (Exception e) {
                            block.setId(i);
                        }
                    } else {
                        block.setId(i);
                    }
                    block.setRefId(0);
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
            ResearchArticle d = new ResearchArticle(docFile.getAbsolutePath(), contents, models, lang, usePOSTagging);
            d.setNoVerbalizationBreakPoints(noBreakPoints);
            // determine title
            nl = doc.getElementsByTagName("title");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.processDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), models, lang, usePOSTagging);
            }

            // determine meta
            nl = doc.getElementsByTagName("meta");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
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
                    Node authorNode = nl.item(i);
                    if (authorNode instanceof Element) {
                        Element authorElement = (Element) authorNode;

                        ArticleAuthor author = new ArticleAuthor();
                        NodeList nlInner = authorElement.getElementsByTagName("authorName");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String authorName = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            d.getAuthors().add(authorName);
                            author.authorName = authorName;
                        }
                        nlInner = authorElement.getElementsByTagName("authorUri");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String authorUri = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.authorUri = authorUri;
                        }
                        nlInner = authorElement.getElementsByTagName("affiliationName");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String affiliationName = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.affiliationName = affiliationName;
                        }
                        nlInner = authorElement.getElementsByTagName("affiliationUri");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String affiliationUri = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.affiliationUri = affiliationUri;
                        }
                        d.getArticleAuthorList().add(author);
                    }
                }
            }
            // get citationUris
            nl = doc.getElementsByTagName("citationUri");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    if (((Element) nl.item(i)).getFirstChild() != null
                            && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                        String citationUri = ((Element) nl.item(i)).getFirstChild().getNodeValue();
                        d.getCitationURIList().add(citationUri);
                    }
                }
            }

            // get complexity level
            nl = doc.getElementsByTagName("complexity_level");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setComplexityLevel(((Element) nl.item(0)).getFirstChild().getNodeValue());
            }

            // get genre
            nl = doc.getElementsByTagName("genre");
            if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
                d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
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
                    DateFormat df = new SimpleDateFormat("yyyy");
                    date = df.parse(((Element) nl.item(0)).getFirstChild().getNodeValue());
                } catch (ParseException e) {
                    DateFormat df2 = new SimpleDateFormat("dd-mm-yyyy");
                    try {
                        date = df2.parse(((Element) nl.item(0)).getFirstChild().getNodeValue());
                    } catch (ParseException e2) {
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
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            LOGGER.error("Error evaluating input file " + docFile.getPath() + " - " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public List<String> getCitationURIList() {
        return citationURIList;
    }

    public void setCitationURIList(List<String> citationURIList) {
        this.citationURIList = citationURIList;
    }

    public List<ArticleAuthor> getArticleAuthorList() {
        return articleAuthorList;
    }

    public void setArticleAuthorList(List<ArticleAuthor> articleAuthorList) {
        this.articleAuthorList = articleAuthorList;
    }

    @Override
    public int hashCode() {
        return this.getURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null || !(obj instanceof ResearchArticle)) {
            return false;
        }
        ResearchArticle a = (ResearchArticle) obj;
        return this.getURI().equals(a.getURI());
    }
}