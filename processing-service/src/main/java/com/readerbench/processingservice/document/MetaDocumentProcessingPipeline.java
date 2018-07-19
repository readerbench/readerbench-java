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
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.MetaDocument;
import static com.readerbench.coreservices.data.document.MetaDocument.getDocumentLevelOfElement;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.textualcomplexity.ComplexityIndices;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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
public class MetaDocumentProcessingPipeline extends DocumentProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDocumentProcessingPipeline.class);

    public MetaDocumentProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    public MetaDocument createMetaDocumentFromXML(String inputPath, MetaDocument.DocumentLevel maxLevel, int maxDepth) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(new File(inputPath)));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = (Element) dom.getElementsByTagName("document").item(0);
            Element root = (Element) doc.getElementsByTagName("section").item(0);
            return createMetaDocumentFromXML(inputPath, root, maxLevel, maxDepth);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error evaluating input file " + inputPath + " - " + e.getMessage());
        }
        return null;
    }

    public MetaDocument createMetaDocumentFromXML(String inputPath, Element root, MetaDocument.DocumentLevel maxLevel, int maxDepth) {
        MetaDocument doc = new MetaDocument(inputPath, getModels(), getLanguage());
        doc.setLevel(getDocumentLevelOfElement(root));
        doc.setTitleText(root.getAttribute("title"));
        NodeList childNodes = root.getChildNodes();
        boolean extraText = false;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (!(childNodes.item(i) instanceof Element)) {
                continue;
            }
            Element subsection = (Element) childNodes.item(i);
            if (!subsection.getTagName().equals("section")) {
                extraText = true;
                continue;
            }
            MetaDocument.DocumentLevel dl = getDocumentLevelOfElement(subsection);
            if (maxDepth > 0 && maxLevel.compareTo(dl) >= 0) {
                doc.getChildren().add(createMetaDocumentFromXML(inputPath, subsection, maxLevel, maxDepth - 1));
                root.removeChild(subsection);
            } else {
                extraText = true;
            }
        }
        if (extraText) {
            Document d = new Document(inputPath, getModels(), getLanguage());
            AbstractDocumentTemplate docTmp = extractDocTemplateFromXML(root);
            Parsing.parseDoc(docTmp, d, getAnnotators().contains(Annotators.NLP_PREPROCESSING), getLanguage());
            doc.addInfo(d);
        }
        return doc;
    }

    public void processMetaDocument(MetaDocument doc, boolean recursive) {
        if (!recursive) {
            super.processDocument(doc);
            return;
        }
        List<Document> leaves = doc.getLeaves();
        leaves.stream().forEach(d -> {
            if (d instanceof MetaDocument) {
                processMetaDocument((MetaDocument) d, false);
            } else {
                super.processDocument(d);
            }
        });
        if (leaves.isEmpty()) {
            LOGGER.error("Error while processing document - empty leaf documents!");
        }
        doc.setComplexityIndices(leaves.get(0).getComplexityIndices().keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        index -> leaves.parallelStream()
                                .mapToDouble(d -> d.getComplexityIndices().get(index))
                                .filter(x -> x != -1)
                                .average().orElse((double) ComplexityIndices.IDENTITY))));
    }

}
