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

import com.readerbench.coreservices.cna.extendedcna.ArticleContainer;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.article.ArticleAuthor;
import com.readerbench.coreservices.data.article.ResearchArticle;
import com.readerbench.coreservices.semanticmodels.data.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.importdata.ImportDocument;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ReaderBench
 */
public class ResearchArticleProcessingPipeline extends DocumentProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchArticleProcessingPipeline.class);

    public ResearchArticleProcessingPipeline(Lang lang, List<ISemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    //consider the usage of the NLP pipeline when creating a new document
    public ResearchArticle createResearchArticleFromXML(String path) {
        ResearchArticle d = new ResearchArticle(path, getModels(), getLanguage());
        AbstractDocumentTemplate docTmp = super.extractDocTemplateFromXML(path);
        Parsing.getParser(getLanguage()).parseDoc(docTmp, d, getAnnotators().contains(Annotators.NLP_PREPROCESSING));
        this.addInformationFromXML(path, d);
        return d;
    }

    public static ArticleContainer buildAuthorContainerFromDirectory(String dirName) {
        File dir = new File(dirName);
        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));
        final ImportDocument id = new ImportDocument();
        List<ResearchArticle> articles = Stream.of(files)
                .parallel()
                .map(file -> {
                    try {
                        return (ResearchArticle) id.importSerializedDocument(file.getPath());
                    } catch (IOException | ClassNotFoundException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(d -> !d.getBlocks().isEmpty())
                .collect(Collectors.toList());
        return new ArticleContainer(articles);
    }

    public void addInformationFromXML(String path, ResearchArticle d) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(new File(path)));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            // parse the XML file
            Element el;
            NodeList nl;

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
                            author.setAuthorName(authorName);
                        }
                        nlInner = authorElement.getElementsByTagName("authorUri");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String authorUri = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.setAuthorUri(authorUri);
                        }
                        nlInner = authorElement.getElementsByTagName("affiliationName");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String affiliationName = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.setAffiliationName(affiliationName);
                        }
                        nlInner = authorElement.getElementsByTagName("affiliationUri");
                        if (nlInner != null && nlInner.getLength() > 0 && ((Element) nlInner.item(0)).getFirstChild() != null) {
                            String affiliationUri = ((Element) nlInner.item(0)).getFirstChild().getNodeValue();
                            author.setAffiliationUri(affiliationUri);
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
                        d.getInitialTopics().add(Parsing.getWordFromConcept(wordToAdd, d.getLanguage()));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error evaluating input file " + path + " - " + e.getMessage());
            LOGGER.error(e.getMessage());
        }
    }
}
