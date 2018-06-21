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

import com.readerbench.coreservices.data.cscl.CSCLIndices;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.CollaborationZone;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.cscl.CollaborationEvaluation;
import com.readerbench.coreservices.keywordmining.Keyword;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.datasourceprovider.commons.Formatting;
import com.readerbench.coreservices.data.*;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.discourse.SemanticChain;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.data.document.Metacognition;
import com.readerbench.coreservices.data.document.ReadingStrategyType;
import com.readerbench.coreservices.data.lexicalchains.LexicalChain;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            for (SemanticModel model : abstractDocument.getSemanticModelsAsList()) {
                out.write(model.getSimilarityType() + " space:," + model.getName() + "\n");
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
                    results = CollaborationEvaluation.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesSocialKB());

                    out.write("\nOverlap between annotated collaboration zones and Social KB model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                                    .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getSocialKBEvolution()));

                    results = CollaborationEvaluation.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
                            c.getIntenseCollabZonesVoice());
                    out.write("\nOverlap between annotated collaboration zones and Voice PMI model\n" + "P=,"
                            + results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
                                    .pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getVoicePMIEvolution()));
                }
                results = CollaborationEvaluation.overlapCollaborationZones(c, c.getIntenseCollabZonesSocialKB(),
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
        }
    }

    public void exportSerializedDocument(AbstractDocument abstractDocument) {
        LOGGER.info("Saving serialized document ...");
        try {
            FileOutputStream fos;
            System.out.println("nume: " + abstractDocument.getPath());
            System.out.println(abstractDocument.getPath().replace(".txt", ".ser"));
            fos = new FileOutputStream(new File(abstractDocument.getPath().replace(".txt", ".ser")));
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(this);
                Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
                abstractDocument.getSemanticModels().entrySet().forEach((e) -> {
                    modelPaths.put(e.getKey(), e.getValue().getName());
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
            for (SemanticModel model : m.getSemanticModelsAsList()) {
                out.write(model.getSimilarityType() + " space:," + model.getName() + "\n");
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
}
