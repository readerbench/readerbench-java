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
package com.readerbench.coreservices.data;

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.semanticmodels.SemanticModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author Mihai Dascalu
 */
public class Block extends AnalysisElement implements Serializable {

    private static final long serialVersionUID = 8767353039355337678L;

    public static final String SPEAKER_ANNOTATION = "name";
    public static final String TIME_ANNOTATION = "time";
    public static final String ID_ANNOTATION = "id";
    public static final String REF_ANNOTATION = "ref";
    public static final String VERBALIZATION_ANNOTATION = "verba";

    private List<Sentence> sentences;
    private Block refBlock;
    // used for identifying the relationship between the
    // verbalizations and the initial text
    private boolean isFollowedByVerbalization;

   // coref-chains
//    private transient Map<Integer, CorefChain> corefs;
    
    // inter-sentence cohesion values
    private SemanticCohesion[][] sentenceDistances;
    private SemanticCohesion[][] prunnedSentenceDistances;
    // cohesion between an utterance and its corresponding block
    private SemanticCohesion[] sentenceBlockDistances;
    private SemanticCohesion prevSentenceBlockDistance, nextSentenceBlockDistance;

    public Block(AnalysisElement d, int index, String text, List<SemanticModel> models, Lang lang) {
        super(d, index, text.trim(), models, lang);
        this.sentences = new ArrayList<>();
    }

    public void finalProcessing() {
        String processedText = getSentences().stream()
                .map(Sentence::getProcessedText)
                .collect(Collectors.joining(". "));
        setProcessedText(processedText.trim());

        // determine overall word occurrences
        determineWordOccurences(getSentences());

        // determine LSA block vector
        determineSemanticDimensions();
    }

    public boolean isSignificant() {
        // determine if a block is significant from a quantitative point of view
        // useful for eliminating short utterances
        int noOccurences = 0;
        for (Entry<Word, Integer> entry : getWordOccurences().entrySet()) {
            noOccurences += entry.getValue();
        }

        return (noOccurences >= 5);
    }

    public int noSignificant() {
        // determine if a block is significant from a quantitative point of view
        // useful for eliminating short utterances
        int noOccurences = 0;
        for (Entry<Word, Integer> entry : getWordOccurences().entrySet()) {
            noOccurences += entry.getValue();
        }

        return noOccurences;
    }

    public static void addBlock(AbstractDocument d, Block b) {
        d.getBlocks().add(b);
        d.setText(d.getText() + b.getText() + "\n");
        d.setProcessedText(d.getProcessedText() + b.getProcessedText() + "\n");
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

//    public Map<Integer, CorefChain> getCorefs() {
//        return corefs;
//    }
//
//    public void setCorefs(Map<Integer, CorefChain> corefs) {
//        this.corefs = corefs;
//    }

    public Block getRefBlock() {
        return refBlock;
    }

    public void setRefBlock(Block refBlock) {
        this.refBlock = refBlock;
    }

    /**
     * @return
     */
    public SemanticCohesion[] getSentenceBlockDistances() {
        return sentenceBlockDistances;
    }

    /**
     * @param sentenceBlockDistances
     */
    public void setSentenceBlockDistances(SemanticCohesion[] sentenceBlockDistances) {
        this.sentenceBlockDistances = sentenceBlockDistances;
    }

    /**
     * @return
     */
    public SemanticCohesion[][] getSentenceDistances() {
        return sentenceDistances;
    }

    /**
     * @param sentenceDistances
     */
    public void setSentenceDistances(SemanticCohesion[][] sentenceDistances) {
        this.sentenceDistances = sentenceDistances;
    }

    /**
     * @return
     */
    public SemanticCohesion[][] getPrunnedSentenceDistances() {
        return prunnedSentenceDistances;
    }

    /**
     * @param prunnedSentenceDistances
     */
    public void setPrunnedSentenceDistances(SemanticCohesion[][] prunnedSentenceDistances) {
        this.prunnedSentenceDistances = prunnedSentenceDistances;
    }

    /**
     * @return
     */
    public boolean isFollowedByVerbalization() {
        return isFollowedByVerbalization;
    }

    /**
     * @param isFollowedByVerbalization
     */
    public void setFollowedByVerbalization(boolean isFollowedByVerbalization) {
        this.isFollowedByVerbalization = isFollowedByVerbalization;
    }

    /**
     * @return
     */
    public SemanticCohesion getPrevSentenceBlockDistance() {
        return prevSentenceBlockDistance;
    }

    /**
     * @param prevSentenceBlockDistance
     */
    public void setPrevSentenceBlockDistance(SemanticCohesion prevSentenceBlockDistance) {
        this.prevSentenceBlockDistance = prevSentenceBlockDistance;
    }

    /**
     * @return
     */
    public SemanticCohesion getNextSentenceBlockDistance() {
        return nextSentenceBlockDistance;
    }

    /**
     * @param nextSentenceBlockDistance
     */
    public void setNextSentenceBlockDistance(SemanticCohesion nextSentenceBlockDistance) {
        this.nextSentenceBlockDistance = nextSentenceBlockDistance;
    }

    @Override
    public List<NGram> getBiGrams() {
        return sentences.stream()
                .flatMap(s -> s.getBiGrams().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<NGram> getNGrams(int n) {
        return sentences.stream()
                .flatMap(s -> s.getNGrams(n).stream())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String s = "";
        s += "{\n";
        s = sentences.stream().map((sentence) -> "\t" + sentence.toString() + "\n").reduce(s, String::concat);
        s += "}\n[" + getScore() + "]\n";
        return s;
    }
}
