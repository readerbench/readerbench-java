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
package services.nlp.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;



import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Block;
import data.Lang;
import data.Sentence;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.sentiment.SentimentEntity;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import services.commons.TextPreprocessing;
import services.nlp.lemmatizer.StaticLemmatizerPOS;
import services.nlp.stemmer.Stemmer;

/**
 * General NLP parsing class relying on the Stanford Core NLP
 *
 * @author Mihai Dascalu
 */
public abstract class Parsing {

    static Logger logger = Logger.getLogger("");

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
            activeSpeaker = new Participant(blockTmp.getSpeaker(), c);
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
        Date time = null;
        // extract date (if applicable)
        if (null != blockTmp.getTime()) {
            try {
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                time = df.parse(blockTmp.getTime());
            } catch (ParseException e) {
                DateFormat df2 = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm aaa");
                try {
                    time = df2.parse(blockTmp.getTime());
                } catch (ParseException e2) {
                    DateFormat df3 = new SimpleDateFormat("kk.mm.ss");
                    try {
                        time = df3.parse(blockTmp.getTime());
                    } catch (ParseException e3) {
                        DateFormat df4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            time = df4.parse(blockTmp.getTime());
                        } catch (ParseException e4) {
                            DateFormat df5 = new SimpleDateFormat("dd MMMMMMMM yyyy HH:mm", Locale.FRANCE);
                            try {
                                time = df5.parse(blockTmp.getTime());
                            } catch (ParseException e5) {
                                DateFormat df6 = new SimpleDateFormat("HH:mm:ss");
                                try {
                                    time = df6.parse(blockTmp.getTime());
                                } catch (ParseException e6) {
                                    try {
                                        Long longTime = Long.parseLong(blockTmp.getTime());
                                        time = new Date(longTime * 1000);
                                    } catch (NumberFormatException e7) {
                                        logger.severe("Unparsable date: " + blockTmp.getTime());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Utterance u = new Utterance(b, activeSpeaker, time);
        return u;
    }

    public void parseDoc(AbstractDocumentTemplate adt, AbstractDocument d, boolean usePOSTagging) {
        Map<BlockTemplate, Annotation> annotations;
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
                    int id;
                    try {
                        id = Double.valueOf(blockTmp.getId()).intValue();
                    } catch (Exception e) {
                        id = -1;
                    }
                    // get ref ID
                    int ref;
                    try {
                        ref = Double.valueOf(blockTmp.getRefId()).intValue();
                    } catch (Exception e) {
                        ref = 0;
                    }

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
                    if (ref > 0) {
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
                        b.setCorefs(document.get(CorefCoreAnnotations.CorefChainAnnotation.class));
                    }
                }
            }
            // determine overall word occurrences
            d.determineWordOccurences(d.getBlocks());
            d.determineSemanticDimensions();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public Block processBlock(AbstractDocument d, int blockIndex, String content, List<CoreMap> sentences) {
        // uses Stanford Core NLP
        Block b = new Block(d, blockIndex, content, d.getSemanticModels(), d.getLanguage());

        // set Stanford sentences
        b.setStanfordSentences(sentences);
        int utteranceCounter = 0;

        for (CoreMap sentence : sentences) {
            if (sentence.toString().trim().length() > 1) {
                Sentence s = processSentence(b, utteranceCounter++, sentence);
                // add utterance to block
                b.getSentences().add(s);
                b.setProcessedText(b.getProcessedText() + s.getProcessedText() + ". ");
            }
        }

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
                Word w = new Word(s, word, StaticLemmatizerPOS.lemmaStatic(word, pos, lang),
                        Stemmer.stemWord(word, lang), Parsing.getParser(lang).convertToPenn(pos), ne,
                        s.getSemanticModels(), lang);
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
                s.setDependencies(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
            }
            if (lang.equals(Lang.en)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                SentimentEntity se = new SentimentEntity();
                // Stanford Valence
                int score = RNNCoreAnnotations.getPredictedClass(tree) - 2;
                se.add(new data.sentiment.SentimentValence(10000, "Stanford", "STANFORD", false), score);
                s.setSentimentEntity(se);
            }
        }

        s.finalProcessing();
        return s;
    }
}
