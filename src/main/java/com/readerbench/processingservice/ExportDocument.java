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
package com.readerbench.processingservice;

import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.cscl.Collaboration;
import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Formatting;
import com.readerbench.datasourceprovider.data.NGram;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.cscl.CSCLIndices;
import com.readerbench.datasourceprovider.data.cscl.CollaborationZone;
import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.cscl.Participant;
import com.readerbench.datasourceprovider.data.cscl.Utterance;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.document.Metacognition;
import com.readerbench.datasourceprovider.data.document.ReadingStrategyType;
import com.readerbench.datasourceprovider.data.document.Summary;
import com.readerbench.datasourceprovider.data.keywordmining.Keyword;
import com.readerbench.datasourceprovider.data.lexicalChains.LexicalChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.openide.util.Exceptions;
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
public class ExportDocument {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDocument.class);

    public void export(AbstractDocument abstractDocument, List<AbstractDocument.SaveType> saveOutputs) {
        if (saveOutputs.contains(AbstractDocument.SaveType.SERIALIZED)) {
            exportSerializedDocument(abstractDocument);
        }
        if (saveOutputs.contains(AbstractDocument.SaveType.CSV)) {
            exportDocumentCSV(abstractDocument);
        }
    }

    public void exportDocumentTxt(AbstractDocument abstractDocument) {
        LOGGER.info("Saving plain text document ...");
        File output = new File(abstractDocument.getPath().replace(".xml", ".txt"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            for (Block block : abstractDocument.getBlocks()) {
                if (block != null) {
                    out.write(block.getText() + "\n");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void exportDocumentCSV(AbstractDocument abstractDocument) {
        LOGGER.info("Writing document export");
        File output = new File(abstractDocument.getPath().replace(".xml", ".csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            out.write("SEP=,\n");
            if (abstractDocument.getTitleText() != null) {
                out.write(abstractDocument.getTitleText().replaceAll(",", "").replaceAll("\\s+", " ") + "\n");
            }
            for (ISemanticModel model : abstractDocument.getSemanticModelsAsList()) {
                out.write(model.getType() + " space:," + model.getPath() + "\n");
            }

            out.write("\nBlock Index,Ref Block Index,Participant,Date,Score,Social Knowledge Building,Initial Text,Processed Text\n");
            for (Block b : abstractDocument.getBlocks()) {
                if (b != null) {
                    out.write(b.getIndex() + ",");
                    if (b.getRefBlock() != null) {
                        out.write(b.getRefBlock().getIndex() + "");
                    }
                    out.write(",");
                    if (b instanceof Utterance) {
                        if (((Utterance) b).getParticipant() != null) {
                            out.write(((Utterance) b).getParticipant().getName().replaceAll(",", "").replaceAll("\\s+", " "));
                        }
                        out.write(",");
                        if (((Utterance) b).getTime() != null) {
                            out.write(((Utterance) b).getTime() + "");
                        }
                        out.write(",");
                        out.write(b.getScore() + "," + ((Utterance) b).getSocialKB() + "," + b.getText().replaceAll(",", "") + "," + b.getProcessedText() + "\n");
                    } else {
                        out.write(",," + b.getScore() + ",," + b.getText().replaceAll(",", "") + "," + b.getProcessedText() + "\n");
                    }
                }
            }

            // print topics
            out.write("\nTopics - Relevance\n");
            out.write("Keyword, Relevance,Tf,Average semantic similarity\n");
            for (Keyword t : abstractDocument.getTopics()) {
                out.write(t.getWord().getLemma() + " (");
                if (t.getElement() instanceof Word) {
                    out.write(t.getWord().getPOS());
                } else {
                    NGram nGram = (NGram) t.getElement();
                    StringBuilder sb = new StringBuilder();
                    nGram.getWords().forEach((word) -> {
                        sb.append(word.getPOS()).append("_");
                    });
                    String nGramLemmas = sb.toString();
                    sb.setLength(0);
                    out.write(nGramLemmas.substring(0, nGramLemmas.length() - 1));
                }
                out.write("),"
                        + Formatting.formatNumber(t.getRelevance()) + ","
                        + Formatting.formatNumber(t.getTermFrequency()) + "," + Formatting.formatNumber(t.getSemanticSimilarity()) + "\n");
            }
            out.write("\n");

            if (abstractDocument.getSemanticModels().containsKey(SimilarityType.LDA)) {
                out.write("\nTopics - Clusters\n");
                Map<Integer, List<Keyword>> topicClusters = new TreeMap<>();
                abstractDocument.getTopics().stream().forEach((t) -> {
                    Integer probClass = LDA.findMaxResemblance(t.getModelRepresentation(SimilarityType.LDA), abstractDocument.getModelRepresentation(SimilarityType.LDA));
                    if (!topicClusters.containsKey(probClass)) {
                        topicClusters.put(probClass, new ArrayList<>());
                    }
                    topicClusters.get(probClass).add(t);
                });
                for (Integer cluster : topicClusters.keySet()) {
                    out.write(cluster + ":,");
                    for (Keyword t : topicClusters.get(cluster)) {
                        out.write(t.getWord().getLemma() + " (" + t.getRelevance() + "),");
                    }
                    out.write("\n");
                }
            }

            if (abstractDocument instanceof Conversation) {
                out.write("\nTopics per Participant\n");
                Conversation c = (Conversation) abstractDocument;
                if (c.getParticipants().size() > 0) {
                    for (Participant p : c.getParticipants()) {
                        out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ":");
                        for (Keyword t : p.getContributions().getTopics()) {
                            out.write("," + t.getWord().getLemma() + " (" + t.getWord().getPOS() + ") - "
                                    + Formatting.formatNumber(t.getRelevance()));
                        }
                        out.write("\n");
                    }
                }

                // print participant statistics
                if (c.getParticipants().size() > 0) {
                    out.write("\nParticipant involvement and interaction\n");
                    out.write("Participant name");
                    for (CSCLIndices CSCLindex : CSCLIndices.values()) {
                        out.write("," + CSCLindex.getDescription());
                    }
                    out.write("\n");
                    for (Participant p : c.getParticipants()) {
                        out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                        for (CSCLIndices index : CSCLIndices.values()) {
                            out.write("," + p.getIndices().get(index));
                        }
                        out.write("\n");
                    }
                    // print interaction matrix
                    out.write("Interaction matrix\n");
                    for (Participant p : c.getParticipants()) {
                        out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                    }
                    out.write("\n");
                    Iterator<Participant> it = c.getParticipants().iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        Participant part = it.next();
                        out.write(part.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                        for (int j = 0; j < c.getParticipants().size(); j++) {
                            out.write("," + Formatting.formatNumber(c.getParticipantContributions()[i][j]));
                        }
                        i++;
                        out.write("\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getAnnotatedCollabZones().size() > 0) {
                    out.write("\nIntense collaboration zones - Annotated\n");
                    for (CollaborationZone zone : c.getAnnotatedCollabZones()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getIntenseCollabZonesSocialKB().size() > 0) {
                    out.write("\nIntense collaboration zones - Social Knowledge Building\n");
                    for (CollaborationZone zone : c.getIntenseCollabZonesSocialKB()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print collaboration zone statistics
                if (c.getIntenseCollabZonesVoice().size() > 0) {
                    out.write("\nIntense collaboration zones - Voice PMI\n");
                    for (CollaborationZone zone : c.getIntenseCollabZonesVoice()) {
                        out.write(zone.toStringDetailed() + "\n");
                    }
                }

                // print statistics
                double[] results;
                if (c.getAnnotatedCollabZones() != null && c.getAnnotatedCollabZones().size() > 0) {
                    results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesSocialKB());

                    out.write("\nOverlap between annotated collaboration zones and Social KB model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                                    .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getSocialKBEvolution()));

                    results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesVoice());
                    out.write("\nOverlap between annotated collaboration zones and Voice PMI model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                                    .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getVoicePMIEvolution()));
                }
                results = Collaboration.overlapCollaborationZones(c, c.getIntenseCollabZonesSocialKB(),
                        c.getIntenseCollabZonesVoice());
                out.write("\nOverlap between Social KB model and Voice PMI model\n" + "P=," + results[0] + "\nR=,"
                        + results[1] + "\nF1 score=," + results[2] + "\nr=,"
                        + VectorAlgebra.pearsonCorrelation(c.getVoicePMIEvolution(), c.getSocialKBEvolution()) + "\n");
            }

            // print semantic chains
            if (abstractDocument.getVoices() != null && abstractDocument.getVoices().size() > 0) {
                out.write("\nVoices - Semantic chains\n");
                for (SemanticChain voice : abstractDocument.getVoices()) {
                    out.write(voice.toStringAllWords() + "\n");
                }
            }

            // print lexical chains
            if (abstractDocument.getLexicalChains().size() > 0) {
                out.write("\nLexical chains\n");
                for (LexicalChain chain : abstractDocument.getLexicalChains()) {
                    out.write(chain.toString() + "\n");
                }
            }

            // print cohesion measurements
            out.write("\nCohesion measurements\n");
            out.write("Items,LSA,LDA,Leacock Chodorow,Wu Palmer,Path Similarity,Distance,Overall\n");
            // block - doc
            for (int i = 0; i < abstractDocument.getBlocks().size(); i++) {
                if (abstractDocument.getBlocks().get(i) != null) {
                    SemanticCohesion coh = abstractDocument.getBlockDocDistances()[i];
                    out.write("D - B" + abstractDocument.getBlocks().get(i).getIndex() + "," + coh.print() + "\n");
                }
            }
            // pruned block-block
            for (int i = 0; i < abstractDocument.getBlocks().size() - 1; i++) {
                for (int j = i + 1; j < abstractDocument.getBlocks().size(); j++) {
                    if (abstractDocument.getPrunnedBlockDistances()[i][j] != null) {
                        SemanticCohesion coh = abstractDocument.getPrunnedBlockDistances()[i][j];
                        out.write("B" + i + "-B" + j + "," + coh.print() + "\n");
                    }
                }
            }
            out.write("\n");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public void exportSerializedDocument(AbstractDocument abstractDocument) {
        LOGGER.info("Saving serialized document ...");
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(new File(abstractDocument.getPath().replace(".xml", ".ser")));
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(this);
                Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class
                );
                abstractDocument.getSemanticModels().entrySet().forEach((e) -> {
                    modelPaths.put(e.getKey(), e.getValue().getPath());
                });
                out.writeObject(modelPaths);
            } catch (Exception | Error ex) {
                LOGGER.error(ex.getMessage());
            }

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void exportMetacognitionCSV(Metacognition m) {
        LOGGER.info("Writing advanced document export");
        File output = new File(m.getPath().replace(".xml", ".csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            out.write("Referred document:," + m.getReferredDoc().getTitleText() + "\n");
            out.write("Author:");
            for (String author : m.getAuthors()) {
                out.write("," + author);
            }
            out.write("\n");
            for (ISemanticModel model : m.getSemanticModelsAsList()) {
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
            for (int index = 0; index < m.getBlocks().size(); index++) {
                endIndex = m.getBlocks().get(index).getRefBlock().getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    // add rows as blocks within the document
                    SemanticCohesion coh = m.getBlockSimilarities()[refBlockId];
                    // add block text
                    out.write(m.getReferredDoc().getBlocks().get(refBlockId).getText().replaceAll(",", "") + ",");
                    // add cohesion
                    out.write(",,,,,," + coh.print() + "\n");
                }
                startIndex = endIndex + 1;

                // add corresponding verbalization
                out.write(m.getBlocks().get(index).getText().replaceAll(",", "") + ",");

                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    out.write(m.getAutomatedReadingStrategies().get(index).get(rs) + ",");
                }
                for (SimilarityType st : SimilarityType.values()) {
                    out.write(m.getAvgCohesion().get(index).getSemanticSimilarities().get(st) + ",");
                }
                out.write("\n");
            }

            // add final row
            out.write("Overall reading strategies,");

            EnumMap<ReadingStrategyType, Integer> allAutomatedRS = m.getAllRS(m.getAutomatedReadingStrategies());
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                out.write(allAutomatedRS.get(rs) + ",");
            }
            out.write("\n");
            LOGGER.info("Successfully finished writing file {}", output.getName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

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
