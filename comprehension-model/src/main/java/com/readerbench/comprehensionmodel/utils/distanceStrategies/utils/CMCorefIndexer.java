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
package com.readerbench.comprehensionmodel.utils.distanceStrategies.utils;

import com.readerbench.data.*;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import com.readerbench.comprehensionmodel.utils.CMUtils;
import com.readerbench.readerbenchcore.nlp.lemmatizer.StaticLemmatizer;
import com.readerbench.readerbenchcore.nlp.parsing.Parsing;

import java.util.ArrayList;
import java.util.List;

public class CMCorefIndexer {

    private final CMUtils cmUtils;
    private final AbstractDocument document;
    private final Lang lang;
    private List<CMCoref> corefList;

    public CMCorefIndexer(AbstractDocument document, Lang lang) {
        this.cmUtils = new CMUtils();
        this.document = document;
        this.lang = lang;
        this.indexCoreferences();
    }

    private void indexCoreferences() {
        this.corefList = new ArrayList<>();
        List<Block> blockList = this.document.getBlocks();
        blockList.stream().forEach((block) -> {
            block.getStanfordSentences().stream().map((sentence) -> sentence.get(TokensAnnotation.class)).forEach((tokens) -> {
                tokens.stream().forEach((token) -> {
                    Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
                    CorefChain chain = block.getCorefs().get(corefClustId);
                    String pos = Parsing.getParser(lang).convertToPenn(token.get(PartOfSpeechAnnotation.class));
                    if (pos.equals("PR") && chain != null && chain.getMentionsInTextualOrder().size() > 1) {
                        int sentINdx = chain.getRepresentativeMention().sentNum - 1;
                        CoreMap corefSentence = block.getStanfordSentences().get(sentINdx);
                        List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);

                        CorefChain.CorefMention reprMent = chain.getRepresentativeMention();
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            pos = Parsing.getParser(lang).convertToPenn(matchedLabel.get(PartOfSpeechAnnotation.class));
                            if (pos.equals("NN")) {
                                this.corefList.add(new CMCoref(token, matchedLabel));
                            }
                        }
                    }
                });
            });
        });
    }

    public CMSyntacticGraph getCMSyntacticGraph(Sentence sentence, int sentenceIndex) {
        SemanticGraph semanticGraph = sentence.getDependencies();
        CMSyntacticGraph syntacticGraph = new CMSyntacticGraph();

        for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
            Word dependentNode = this.getActualWord(edge.getDependent(), sentenceIndex);
            Word governorNode = this.getActualWord(edge.getGovernor(), sentenceIndex);
            if (dependentNode.isContentWord() && governorNode.isContentWord()) {
                syntacticGraph.indexEdge(dependentNode, governorNode);
            }
        }

        return syntacticGraph;
    }

    private Word getActualWord(IndexedWord indexedWord, int sentenceIndex) {
        Word word = this.cmUtils.convertToWord(indexedWord, lang);
        if (word.getPOS().equals("PR")) {
            CMCoref dependentCoref = this.getCMCoref(indexedWord, sentenceIndex);
            if (dependentCoref != null) {
                System.out.println("[Sentence " + sentenceIndex + "] Replacing " + indexedWord.word() + " with " + dependentCoref.getReferencedToken().word() + "");
                return this.convertToWord(dependentCoref.getReferencedToken());
            }
        }
        return word;
    }

    private Word convertToWord(CoreLabel node) {
        String wordStr = node.word().toLowerCase();
        Word word = Word.getWordFromConcept(wordStr, lang);
        word.setLemma(StaticLemmatizer.lemmaStatic(wordStr, lang));
        word.setPOS("");
        if (node.tag() != null && node.tag().length() >= 2) {
            word.setPOS(node.tag().substring(0, 2));
        }
        return word;
    }

    private CMCoref getCMCoref(IndexedWord word, int sentenceIndex) {
        for (CMCoref coref : this.corefList) {
            if (coref.getSentenceIndex() != sentenceIndex) {
                continue;
            }
            if (coref.getToken().index() == word.index()) {
                return coref;
            }
        }
        return null;
    }
}
