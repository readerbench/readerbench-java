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
package com.readerbench.services.nlp.parsing;

import com.readerbench.data.*;
import com.readerbench.data.AbstractDocumentTemplate.BlockTemplate;
import com.readerbench.data.cscl.Conversation;
import com.readerbench.data.cscl.Participant;
import com.readerbench.data.cscl.Utterance;
import com.readerbench.data.sentiment.SentimentEntity;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.openide.util.Exceptions;
import com.readerbench.services.commons.TextPreprocessing;
import com.readerbench.services.nlp.lemmatizer.StaticLemmatizerPOS;
import com.readerbench.services.nlp.stemmer.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * General NLP parsing class relying on the Stanford Core NLP
 *
 * @author Mihai Dascalu
 */
public abstract class Parsing {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parsing.class);

    public static final int STANFORD_ID = 10000;

    protected Lang lang;

    public static Parsing getParser(Lang lang) {
        switch (lang) {
            case fr:
                return Parsing_FR.getInstance();
            case it:
                return Parsing_IT.getInstance();
            case es:
                return Parsing_ES.getInstance();
            case nl:
                return Parsing_NL.getInstance();
            case ro:
                return Parsing_RO.getInstance();
            case en:
                return Parsing_EN.getInstance();
            case la:
                return Parsing_LA.getInstance();
            default:
                return null;
        }
    }

    public String convertToPenn(String pos) {
        if (pos != null && pos.length() > 2) {
            return pos.substring(0, 2);
        }
        return pos;
    }

    public abstract StanfordCoreNLP getPipeline();

    private Utterance getUtterance(Conversation c, BlockTemplate blockTmp, Block b) {
        Participant activeSpeaker = null;
        if (!blockTmp.getSpeaker().isEmpty()) {
            activeSpeaker = new Participant(blockTmp.getSpeaker(), blockTmp.getSpeakerAlias(), c);
            boolean contains = false;
            for (Participant p : c.getParticipants()) {
                if (p.equals(activeSpeaker)) {
                    activeSpeaker = p;
                    contains = true;
                }
            }
            if (!contains) {
                c.getParticipants().add(activeSpeaker);
            }
        }
        Utterance u = new Utterance(b, activeSpeaker, blockTmp.getTime());
        return u;
    }

    public void parseDoc(AbstractDocumentTemplate adt, AbstractDocument d, boolean usePOSTagging) {
        Map<BlockTemplate, Annotation> annotations;
        usePOSTagging = usePOSTagging && hasAnnotators();
        try {
            if (!adt.getBlocks().isEmpty()) {
                annotations = adt.getBlocks().stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                (blockTmp) -> new Annotation(TextPreprocessing.basicTextCleaning(blockTmp.getContent(), lang))));
                if (usePOSTagging) {
                    getPipeline().annotate(annotations.values());
                }
                for (BlockTemplate blockTmp : adt.getBlocks()) {
                    String text = annotations.get(blockTmp).toString();
                    // get block ID
                    Integer id = blockTmp.getId();
                    Integer ref = blockTmp.getRefId();

                    boolean followedByVerbalization = false;
                    // mark if the block has a verbalization afterwards
                    if (null != blockTmp.getVerbId()) {
                        try {
                            followedByVerbalization = true;
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                    }

                    Block b;
                    Annotation document = null;
                    if (usePOSTagging) {
                        document = annotations.get(blockTmp);
                        // create an empty Annotation just with the given text
                        b = processBlock(d, id, text, document.get(SentencesAnnotation.class));
                    } else {
                        b = SimpleParsing.processBlock(d, id, text);
                    }
                    if (d instanceof Conversation) {
                        b = getUtterance((Conversation) d, blockTmp, b);
                    }
                    b.setFollowedByVerbalization(followedByVerbalization);
                    Block.addBlock(d, b);
                    // add explicit reference, if the case
                    if (ref != null && ref != -1) {
                        for (Block refB : d.getBlocks()) {
                            if (refB != null && refB.getIndex() == ref) {
                                b.setRefBlock(refB);
                                break;
                            }
                        }
                    }

                    if (usePOSTagging && lang.equals(Lang.en)) {
                        // Build the co-reference link graph
                        // Each chain stores a set of mentions that link to each other, along with a method for getting the most representative mention.
                        b.setCorefs(document.get(CorefCoreAnnotations.CorefChainAnnotation.class
                        ));
                    }
                }
            }
            // determine overall word occurrences
            d.determineWordOccurences(d.getBlocks());
            d.determineSemanticDimensions();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public Block processBlock(AbstractDocument d, int blockIndex, String content, List<CoreMap> sentences) {
        // uses Stanford Core NLP
        Block b = new Block(d, blockIndex, content, d.getSemanticModels(), d.getLanguage());

        // set Stanford sentences
        b.setStanfordSentences(sentences);
        
        List<Sentence> finalSentences = sentences.parallelStream()
                .filter(s -> s.toString().trim().length() > 1)
                .map(s -> processSentence(b, 0, s))
                .collect(Collectors.toList());
        //Set sentence index
        for (int i = 0; i < finalSentences.size(); i++) {
            finalSentences.get(i).setIndex(i);
        }
        b.setSentences(finalSentences);
        b.setProcessedText(finalSentences.stream()
                .map(s -> s.getProcessedText() + ". ")
                .collect(Collectors.joining()));
        
        b.finalProcessing();
        return b;
    }

    public Sentence processSentence(Block b, int utteranceIndex, CoreMap sentence) {
        // uses Stanford Core NLP
        Sentence s = new Sentence(b, utteranceIndex, sentence.toString().trim(), b.getSemanticModels(), lang);

        sentence.get(TokensAnnotation.class).stream().forEach((token) -> {
            String word = token.get(OriginalTextAnnotation.class);
            String pos = Parsing.getParser(lang).convertToPenn(token.get(PartOfSpeechAnnotation.class));
            String ne = token.get(NamedEntityTagAnnotation.class);
            if (TextPreprocessing.isWord(word, lang)) {
                Word w = new Word(s, word, StaticLemmatizerPOS.lemmaStatic(word, pos, lang), Stemmer.stemWord(word, lang), Parsing.getParser(lang).convertToPenn(pos), ne, s.getSemanticModels(), lang);
                s.getAllWords().add(w);
                if (w.isContentWord()) {
                    s.getWords().add(w);
                    if (s.getWordOccurences().containsKey(w)) {
                        s.getWordOccurences().put(w, s.getWordOccurences().get(w) + 1);
                    } else {
                        s.getWordOccurences().put(w, 1);
                    }
                }
            }
        });

        if (lang.equals(Lang.en) || lang.equals(Lang.fr) || lang.equals(Lang.es)) {
            if (lang.equals(Lang.en) || lang.equals(Lang.fr)) {
                s.setDependencies(sentence.get(EnhancedPlusPlusDependenciesAnnotation.class));
            }
            if (lang.equals(Lang.en)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                s.setTree(tree);
                SentimentEntity se = new SentimentEntity();
                // TODO: parse Stanford Valence
                if (tree != null) {
                    int score = RNNCoreAnnotations.getPredictedClass(tree) - 2;
                    se.add(new com.readerbench.data.sentiment.SentimentValence(STANFORD_ID, "Stanford", "STANFORD", false), score);
                    s.setSentimentEntity(se);
                }
            }
        }

        s.finalProcessing();
        return s;
    }
    
    public boolean hasAnnotators() {
        return !getPipeline().getProperties().getProperty("annotators").isEmpty();
    }
}