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
package services.discourse.dialogism;

import java.util.*;

import data.sentiment.SentimentEntity;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;
import services.commons.VectorAlgebra;
import data.AbstractDocument;
import data.AnalysisElement;
import data.Block;
import data.Sentence;
import data.Word;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.lexicalChains.LexicalChain;
import services.nlp.parsing.Context;
import services.nlp.parsing.ContextSentiment;

import java.util.logging.Logger;

public class DialogismComputations {

    static final Logger LOGGER = Logger.getLogger("");

    public static final int WINDOW_SIZE = 5; // no contributions
    public static final int MAXIMUM_INTERVAL = 60; // seconds
    public static final int SEMANTIC_CHAIN_MIN_NO_WORDS = 7; //no words per voice

    public static void determineVoices(AbstractDocument d) {
        // merge chains based on LSA / LDA in order to generate semantic chains
        LOGGER.info("Starting to assess voices by first building semantic chains");
        List<SemanticChain> semanticChains = new ArrayList<>();
        for (LexicalChain chain : d.getLexicalChains()) {
            SemanticChain newChain = new SemanticChain(chain, d.getSemanticModels());
            newChain.updateSemanticRepresentation();
            semanticChains.add(newChain);
        }

        if (semanticChains.size() > 0) {
            boolean modified = true;
            List<SemanticChain> newSemanticChains;

            while (modified) {
                modified = false;
                newSemanticChains = new ArrayList<>();
                for (int i = 0; i < semanticChains.size() - 1; i++) {
                    if (semanticChains.get(i) != null) {
                        boolean alreadyAdded = false;
                        double simMax = -1;
                        int simMaxIndex = -1;
                        for (int j = i + 1; j < semanticChains.size(); j++) {
                            double sim = SemanticChain.similarity(semanticChains.get(i), semanticChains.get(j));
                            if (sim != -1 && simMax < sim) {
                                simMax = sim;
                                simMaxIndex = j;
                            }
                        }
                        if (simMaxIndex != -1) {
                            SemanticChain newChain = SemanticChain.merge(semanticChains.get(i),
                                    semanticChains.get(simMaxIndex));
                            alreadyAdded = true;
                            newSemanticChains.add(newChain);
                            // make old reference void
                            semanticChains.set(simMaxIndex, null);
                        }
                        if (!alreadyAdded) {
                            newSemanticChains.add(semanticChains.get(i));
                        }
                        modified = modified || alreadyAdded;
                    }
                }
                // add last element
                if (semanticChains.get(semanticChains.size() - 1) != null) {
                    newSemanticChains.add(semanticChains.get(semanticChains.size() - 1));
                }
                semanticChains = newSemanticChains;
            }
        }

        // specify for each word its corresponding semantic chain
        for (Iterator<SemanticChain> iterator = semanticChains.iterator(); iterator.hasNext();) {
            SemanticChain chain = iterator.next();
            if (chain.getWords().size() < SEMANTIC_CHAIN_MIN_NO_WORDS) {
                iterator.remove();
            } else {
                for (Word w : chain.getWords()) {
                    w.setSemanticChain(chain);
                }
            }
        }
        d.setVoices(semanticChains);
    }

    public static void determineVoiceDistribution(AnalysisElement e, AbstractDocument d) {
        if (d.getVoices() != null && d.getVoices().size() > 0) {
            e.setVoiceDistribution(new double[d.getVoices().size()]);

            for (Word w : e.getWordOccurences().keySet()) {
                double no = 1 + Math.log(e.getWordOccurences().get(w));
                int index = d.getVoices().indexOf(w.getSemanticChain());
                if (index >= 0) {
                    e.getVoiceDistribution()[index] += no;
                }
            }
        }
    }

    public static void determineVoiceDistributions(AbstractDocument d) {
        LOGGER.info("Identifying voice distributions...");
        // determine distribution of each lexical chain
        int noSentences = 0;
        int[][] traceability = new int[d.getBlocks().size()][];
        for (int i = 0; i < d.getBlocks().size(); i++) {
            if (d.getBlocks().get(i) != null) {
                traceability[i] = new int[d.getBlocks().get(i).getSentences().size()];
                for (int j = 0; j < d.getBlocks().get(i).getSentences().size(); j++) {
                    traceability[i][j] = noSentences++;
                }
            }
        }

        // build time intervals
        d.setBlockOccurrencePattern(new long[d.getBlocks().size()]);
        if (d instanceof Conversation) {
            Date earlierDate = null, laterDate;
            for (int blockIndex = 0; blockIndex < d.getBlocks().size(); blockIndex++) {
                if (d.getBlocks().get(blockIndex) != null) {
                    Utterance u = (Utterance) d.getBlocks().get(blockIndex);
                    if (earlierDate == null) {
                        earlierDate = u.getTime();
                    } else {
                        laterDate = u.getTime();
                        if (laterDate != null) {
                            d.getBlockOccurrencePattern()[blockIndex] = Math.min((laterDate.getTime() - earlierDate.getTime()) / 1000, 0);
                        }
                    }
                }
            }
        }

        // determine spread
        if (d.getVoices() != null) {
            d.setNoNouns(0);
            d.setNoVerbs(0);

            Map<String, Integer> auxiliarVoices = new HashMap<String, Integer>();

            for (SemanticChain chain : d.getVoices()) {
                chain.setSentenceDistribution(new double[noSentences]);
                chain.setBlockDistribution(new double[d.getBlocks().size()]);
                Map<String, Integer> voiceOccurrences = new TreeMap<>();
                boolean isPerspective = true;
                int noNouns = 0;
                int noVerbs = 0;

                for (Word w : chain.getWords()) {
                    int blockIndex = w.getBlockIndex();
                    int sentenceIndex = w.getUtteranceIndex();

                    //find the valence for the context of this voice in the sentence
                    Sentence sentence = d.getBlocks().get(blockIndex).getSentences().get(sentenceIndex);
                    double valence = 0;
                    if (w.isVerb() || w.isNoun()) {

                        //count voices which are nouns or verbs
                        if (!auxiliarVoices.containsKey(w.getText())) {
                            auxiliarVoices.put(w.getText(), 1);
                            if (w.isNoun()) {
                                d.setNoNouns(d.getNoNouns() + 1);
                                noNouns++;
                            }
                            if (w.isVerb()) {
                                d.setNoVerbs(d.getNoVerbs() + 1);
                                noVerbs++;
                            }
                        }

                        List<ContextSentiment> ctxTrees = sentence.getContextMap().get(w);
                        int noCtxTrees = ctxTrees.size();
                        double valenceForContext = 0;
                        //compute the average valence for contextTrees
                        for (ContextSentiment ctxTree : ctxTrees) {
                            valenceForContext += ctxTree.getValence();
                        }
                        valence = Math.round(valenceForContext / noCtxTrees);
                    }
                    else {
                        isPerspective = false;
                    }

                    // determine spread as 1+log(no_occurences) per sentence
                    try {
                        chain.getSentenceDistribution()[traceability[blockIndex][sentenceIndex]] += valence;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println(ex);
                    }

                    chain.getBlockDistribution()[blockIndex] += valence;

                    // build cumulative importance in terms of sentences in which occurrences have been spotted
                    if (voiceOccurrences.containsKey(blockIndex + "_" + sentenceIndex)) {
                        voiceOccurrences.put(blockIndex + "_" + sentenceIndex,
                                voiceOccurrences.get(blockIndex + "_" + sentenceIndex) + 1);
                    } else {
                        voiceOccurrences.put(blockIndex + "_" + sentenceIndex, 1);
                    }

                }

                //this voice contains only nouns and verbs
                if (isPerspective) {
                    d.setNoPerspectives(d.getNoPerspectives() + 1);
                    d.setNoNounsInPerspectives(d.getNoNounsInPerspectives() + noNouns);
                    d.setNoVerbsInPerspectives(d.getNoVerbsInPerspectives() + noVerbs);
                }

                for (String key : voiceOccurrences.keySet()) {
                    Integer blockIndex = Integer.valueOf(key.substring(0, key.indexOf("_")));
                    Integer sentenceIndex = Integer.valueOf(key.substring(key.indexOf("_") + 1));
                    Sentence s = d.getBlocks().get(blockIndex).getSentences().get(sentenceIndex);

                    if (s.getWords().size() > 0) {
                        chain.setAverageImportanceScore(chain.getAverageImportanceScore() + s.getScore());
                    }
                }
                // normalize
                if (voiceOccurrences.size() > 0) {
                    chain.setAverageImportanceScore(chain.getAverageImportanceScore() / voiceOccurrences.size());
                }

                // define moving average at block level, relevant for chat conversations
                chain.setBlockMovingAverage(VectorAlgebra.movingAverage(chain.getBlockDistribution(), WINDOW_SIZE,
                        d.getBlockOccurrencePattern(), MAXIMUM_INTERVAL));
            }
            System.out.println("--------Number of voices: " + d.getVoices().size());
            System.out.println("-------Number of perspectives: " + d.getNoPerspectives());
            System.out.println("-------Number of nouns in perspectives: " + d.getNoNounsInPerspectives());
            System.out.println("-------Number of verbs in perspectives: " + d.getNoVerbsInPerspectives());
            System.out.println("-------Number of nouns: " + d.getNoNouns());
            System.out.println("-------Number of verbs: " + d.getNoVerbs());
            // sort semantic chains (voices) by importance
            Collections.sort(d.getVoices());

            // build voice distribution vectors for each block
            for (Block b : d.getBlocks()) {
                if (b != null) {
                    determineVoiceDistribution(b, d);
                }
            }
        }
    }

    public static void determineParticipantInterAnimation(Conversation c) {
        if (c.getVoices() == null || c.getVoices().isEmpty()) {
            return;
        }

        // take all voices
        for (int i = 0; i < c.getVoices().size(); i++) {
            for (int p1 = 0; p1 < c.getParticipants().size() - 1; p1++) {
                for (int p2 = p1 + 1; p2 < c.getParticipants().size(); p2++) {
                    // for different participants build collaboration based on inter-twined voices
                    double[] ditrib1 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), c.getParticipants().get(p1));
                    double[] ditrib2 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), c.getParticipants().get(p2));
                    double addedInterAnimationDegree = VectorAlgebra.mutualInformation(ditrib1, ditrib2);

                    c.getParticipants().get(p1).getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
                            c.getParticipants().get(p1).getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE) + addedInterAnimationDegree);
                    c.getParticipants().get(p2).getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
                            c.getParticipants().get(p2).getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE) + addedInterAnimationDegree);
                }
            }
        }
    }

    /**
     * @param d
     *
     * Build for every sentence a context map with all the voices and the associated context Tree with its valence
     */
    public static void findSentimentUsingContext(AbstractDocument d) {
        LOGGER.info("Searching context for every voice in every sentence");
        Context ctx = new Context();

        //for every sentence make a map which has key voice and value a list of pair(Tree, valence)
        for (Block b: d.getBlocks()) {
            for (Sentence sentence: b.getSentences()) {

                List<Word> words = sentence.getWords();
                Map<Word, List<ContextSentiment>> contextMap = new HashMap<>();

                for (SemanticChain chain: d.getVoices()) {
                    for (Word w: chain.getWords()) {
                        //the context for this context was computed in the past
                        if (contextMap.containsKey(w)) {
                            continue;
                        }
                        //for adj. voice it is not determined the context
                        if (!w.isNoun() && !w.isVerb()) {
                            continue;
                        }

                        List<ContextSentiment> contextTrees = new ArrayList<ContextSentiment>();
                        //check if the word from voice is in sentence
                        for (Word aux : words) {
                            if (aux.getText().equals(w.getText())) {
                                double valence = 0;
                                Tree tree = sentence.getTree();
                                List<Tree> subTrees = ctx.findContextTree(tree, w, w.isNoun());
                                //for every contextSubtree compute the valence
                                for (Tree subTree:subTrees) {
                                    valence = RNNCoreAnnotations.getPredictedClass(subTree) - 2;
                                    contextTrees.add(new ContextSentiment(subTree, valence));
                                }
                                contextMap.put(w, contextTrees);

                                break;
                            }
                        }
                    }
                }
                //every sentence has a map with voice- (context, valence)
                sentence.setContextMap(contextMap);
            }
        }
    }
}
