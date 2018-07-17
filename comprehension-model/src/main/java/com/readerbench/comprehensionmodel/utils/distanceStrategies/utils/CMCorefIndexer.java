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

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.comprehensionmodel.utils.CMUtils;
import com.readerbench.coreservices.nlp.lemmatizer.StaticLemmatizer;
import com.readerbench.coreservices.nlp.parsing.Parsing;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Triple;

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
        // TODO: index the actual coreferences
        /*
        List<Block> blockList = this.document.getBlocks();
        blockList.stream().forEach((block) -> {
            block.getStanfordSentences().stream().map((sentence) -> sentence.get(CoreAnnotations.TokensAnnotation.class)).forEach((tokens) -> {
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
        */
    }

    public CMSyntacticGraph getCMSyntacticGraph(Sentence sentence, int sentenceIndex) {
        List<Triple<Word, Word, String>> dependencies = sentence.getDependencies();
        CMSyntacticGraph syntacticGraph = new CMSyntacticGraph();

        for (Triple<Word, Word, String> dependency : dependencies) {
            Word dependentNode = this.getActualWord(dependency.getLeft(), sentenceIndex);
            Word governorNode = this.getActualWord(dependency.getMiddle(), sentenceIndex);
            if (dependentNode.isContentWord() && governorNode.isContentWord()) {
                syntacticGraph.indexEdge(dependentNode, governorNode);
            }
        }

        return syntacticGraph;
    }
    
    private Word getActualWord(Word word, int sentenceIndex) {
        // TODO: use the indexed coreferences
        return word;
    }

}
