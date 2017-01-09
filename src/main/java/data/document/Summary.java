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

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import data.AbstractDocumentTemplate;
import data.discourse.SemanticCohesion;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import services.complexity.ComplexityIndices;
import services.readingStrategies.ReadingStrategies;

public class Summary extends Metacognition {

    private static final long serialVersionUID = -3087279898902437719L;

    private final SemanticCohesion cohesion;

    public Summary(String path, AbstractDocumentTemplate docTmp, Document initialReadingMaterial, boolean usePOSTagging) {
        super(path, docTmp, initialReadingMaterial, usePOSTagging);
        cohesion = new SemanticCohesion(this, initialReadingMaterial);
    }

    public Summary(String content, Document initialReadingMaterial, boolean usePOSTagging) {
        super(null, AbstractDocumentTemplate.getDocumentModel(content), initialReadingMaterial, usePOSTagging);
        cohesion = new SemanticCohesion(this, initialReadingMaterial);
    }

    public static Summary loadSummary(String pathToDoc, Document initialReadingMaterial, boolean usePOSTagging) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(new File(
                    pathToDoc)));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);
            Element doc = dom.getDocumentElement();
            // determine contents
            AbstractDocumentTemplate contents = extractDocumentContent(doc, "p");
            if (contents.getBlocks().isEmpty()) {
                return null;
            }

            LOGGER.info("Building summary internal representation ...");
            Summary meta = new Summary(pathToDoc, contents, initialReadingMaterial, usePOSTagging);

            extractDocumentDescriptors(doc, meta, usePOSTagging);
            return meta;
        } catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Error evaluating input file: {0}, error:{1}!", new Object[]{pathToDoc, e.toString()});
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    @Override
    public void exportXML(String path) {
        try {
            org.w3c.dom.Document dom = generateDOMforXMLexport(path);

            Element docEl = writeDocumentDescriptors(dom);

            Element bodyEl = dom.createElement("summary_body");
            docEl.appendChild(bodyEl);

            for (int i = 0; i < getBlocks().size(); i++) {
                if (getBlocks().get(i) != null) {
                    Element pEl = dom.createElement("p");
                    pEl.setAttribute("id", i + "");
                    pEl.setTextContent(getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            LOGGER.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public SemanticCohesion getCohesion() {
        return cohesion;
    }

    @Override
    public void computeAll(boolean computeDialogism) {
        computeDiscourseAnalysis(computeDialogism);
        ReadingStrategies.detReadingStrategies(this);
        ComplexityIndices.computeComplexityFactors(this);
        LOGGER.info("Finished processing summary ...");
    }
}
