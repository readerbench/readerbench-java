package com.readerbench.processingservice;

import com.readerbench.coreservices.cna.CohesionGraph;
import com.readerbench.coreservices.cna.DisambiguisationGraphAndLexicalChains;
import com.readerbench.coreservices.cna.Scoring;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.cscl.Collaboration;
import com.readerbench.coreservices.dialogism.DialogismComputations;
import com.readerbench.coreservices.keywordMining.KeywordModeling;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.nlp.parsing.SimpleParsing;
import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.coreservices.sentimentanalysis.SentimentAnalysis;
import com.readerbench.datasourceprovider.data.*;
import com.readerbench.datasourceprovider.data.cscl.*;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.keywordmining.Keyword;
import com.readerbench.datasourceprovider.data.lexicalChains.LexicalChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class AbstractDocumentProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDocument.class);

    /**
     *
     * @param computeDialogism
     * @param useBigrams
     */
    public void computeDiscourseAnalysis(AbstractDocument abstractDocument, boolean computeDialogism, boolean useBigrams) {
        if (computeDialogism) {
            // build disambiguisation graph and lexical chains
            LOGGER.info("Build disambiguation graph...");
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(abstractDocument);
            LOGGER.info("Prune disambiguation graph...");
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(abstractDocument);
            // System.out.println(d.disambiguationGraph);

            LOGGER.info("Build lexical chains...");
            DisambiguisationGraphAndLexicalChains.buildLexicalChains(abstractDocument);
            // for (LexicalChain chain : lexicalChains) {
            // System.out.println(chain);
            // }

            // determine semantic chains / voices
            LOGGER.info("Determine semantic chains / voices...");
            DialogismComputations.determineVoices(abstractDocument);
            DialogismComputations.determineExtendedVoices(abstractDocument);

            // DialogismComputations.findSentimentUsingContext(this);
            // determine voice distributions & importance
            LOGGER.info("Determine voice distributions & importance...");
            DialogismComputations.determineVoiceDistributions(abstractDocument);
            // DialogismComputations.determineExtendedVoiceDistributions(this);
        }

        // build coherence graph
        LOGGER.info("Build coherence graph...");
        CohesionGraph.buildCohesionGraph(abstractDocument);

        LOGGER.info("Determine topics...");
        KeywordModeling.determineKeywords(abstractDocument, useBigrams);
        // TopicModel.determineTopicsLDA(this);

        Scoring.score(abstractDocument);
        // assign sentiment values
        LOGGER.info("Assign sentiment values...");
        SentimentAnalysis.weightSemanticValences(abstractDocument);
        LOGGER.info("Finished all discourse analysis processes");
    }


    public void processDocumentTitle(AbstractDocument abstractDocument, String title, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        abstractDocument.setTitleText(title);
        Annotation document;
        String processedText = title.replaceAll("\\s+", " ");

        if (processedText.length() > 0) {
            if (usePOSTagging) {
                // create an empty Annotation just with the given text
                document = new Annotation(processedText.replaceAll("[\\.\\!\\?\n]", ""));
                // run all Annotators on this text
                Parsing.getParser(lang).getPipeline().annotate(document);
                CoreMap sentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);

                // add corresponding block
                abstractDocument.setTitle(Parsing.getParser(lang).processSentence(new Block(null, 0, "", models, lang), 0, sentence));
            } else {
                abstractDocument.setTitle(SimpleParsing.processSentence(new Block(null, 0, "", models, lang), 0, processedText));
            }
        }
    }

    public void exportDocumentAdvanced(AbstractDocument abstractDocument) {
        LOGGER.info("Writing advanced document export");
        File output = new File(abstractDocument.getPath().replace(".xml", "_adv.csv"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("ID,Block,Text,Score,Cosine Sim LSA,Divergence LDA,Leacok Chodorow,Wu Palmer,Path Sim,Dist,Cohesion\n");
            int globalIndex = 0;
            for (Block b : abstractDocument.getBlocks()) {
                if (b != null) {
                    for (int index = 0; index < b.getSentences().size(); index++) {
                        Sentence u = b.getSentences().get(index);

                        out.write(globalIndex++ + ",");
                        out.write(b.getIndex() + ",");
                        out.write(u.getText().replaceAll(",", "") + ",");
                        out.write(Formatting.formatNumber(u.getScore()) + ",");
                        if (index > 0) {
                            SemanticCohesion coh = b.getSentenceDistances()[index - 1][index];
                            out.write(coh.print() + "\n");
                        } else {
                            out.write("0,0,0,0,0,0,0\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
        }
    }

    public void computeAll(AbstractDocument abstractDocument, boolean computeDialogism, boolean useBigrams) {
        if (!abstractDocument.getBlocks().isEmpty()) {
            computeDiscourseAnalysis(abstractDocument, computeDialogism, useBigrams);
            //todo - to be reviewed
            //ComplexityIndices.computeComplexityFactors(this);
        }
    }

    public void exportDocument(AbstractDocument abstractDocument) {
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
                    for (Word word : nGram.getWords()) {
                        sb.append(word.getPOS()).append("_");
                    }
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
        }
    }

    public void rebuildSemanticSpaces(AbstractDocument abstractDocument, List<ISemanticModel> models) {
        abstractDocument.setSemanticModels(models);
        for (Block b : abstractDocument.getBlocks()) {
            if (b != null) {
                b.setSemanticModels(models);
                if (b.getSentences() != null) {
                    for (Sentence s : b.getSentences()) {
                        s.setSemanticModels(models);
                        for (Word w : s.getAllWords()) {
                            w.setSemanticModels(models);
                        }
                    }
                }
            }
        }
        if (abstractDocument.getVoices() != null) {
            for (SemanticChain chain : abstractDocument.getVoices()) {
                chain.setSemanticModels(models);
            }
        }
    }

    public void save(AbstractDocument.SaveType saveOutput) {
        switch (saveOutput) {
            case SERIALIZED:
                saveSerializedDocument();
                break;
            case SERIALIZED_AND_CSV_EXPORT:
                saveSerializedDocument();
                //exportDocument();
                break;
            case FULL:
                //exportDocument();
                exportDocumentAdvanced();
                saveSerializedDocument();
                break;
            default:
                break;
        }
    }

    public static AbstractDocument loadGenericDocument(String pathToDoc,
                                                       Map<SimilarityType, String> modelPaths, Lang lang,
                                                       boolean usePOSTagging, boolean computeDialogism, boolean useBigrams, String pathToComplexityModel,
                                                       int[] selectedComplexityFactors, boolean cleanInput, AbstractDocument.SaveType saveOutput) {
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);
        return loadGenericDocument(new File(pathToDoc), models, lang, usePOSTagging, computeDialogism, useBigrams,
                pathToComplexityModel, selectedComplexityFactors, cleanInput, saveOutput);
    }



}
