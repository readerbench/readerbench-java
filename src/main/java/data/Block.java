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
package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import data.discourse.SemanticCohesion;
import data.discourse.SemanticRelatedness;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.stream.Collectors;
import services.semanticModels.ISemanticModel;

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

    private transient Annotation annotation; // useful for rebuilding
    // coref-chains
    private transient Map<Integer, CorefChain> corefs;
    private transient List<CoreMap> stanfordSentences;

    // inter-sentence cohesion values
    private SemanticCohesion[][] sentenceDistances;
    private SemanticCohesion[][] prunnedSentenceDistances;
    // cohesion between an utterance and its corresponding block
    private SemanticCohesion[] sentenceBlockDistances;
    private SemanticCohesion prevSentenceBlockDistance, nextSentenceBlockDistance;

    // inner-sentence semantic similarity values
    private SemanticRelatedness[][] sentenceRelatedness;
    private SemanticRelatedness[][] prunnedSentenceRelatedness;
    // semantic similarity between an utterance and its corresponding block
    private SemanticRelatedness[] sentenceBlockRelatedness;
    private SemanticRelatedness prevSentenceBlockRelatedness, nextSentenceBlockRelatedness;

    public Block(AnalysisElement d, int index, String text, List<ISemanticModel> models, Lang lang) {
        super(d, index, text.trim(), models, lang);
        this.sentences = new ArrayList<>();
    }

    public void finalProcessing() {
        setProcessedText(getProcessedText().trim());

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
//        if (b.getIndex() != -1) {
//            while (d.getBlocks().size() < b.getIndex()) {
//                d.getBlocks().add(null);
//            }
//            d.getBlocks().add(b.getIndex(), b);
//        } else {
        d.getBlocks().add(b);
//        }
        d.setProcessedText(d.getProcessedText() + b.getProcessedText() + "\n");
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Map<Integer, CorefChain> getCorefs() {
        return corefs;
    }

    public void setCorefs(Map<Integer, CorefChain> corefs) {
        this.corefs = corefs;
    }

    public Block getRefBlock() {
        return refBlock;
    }

    public void setRefBlock(Block refBlock) {
        this.refBlock = refBlock;
    }

    public List<CoreMap> getStanfordSentences() {
        return stanfordSentences;
    }

    public void setStanfordSentences(List<CoreMap> sentences) {
        this.stanfordSentences = sentences;
    }

    /**
     * @return
     */
    public SemanticRelatedness[] getSentenceBlockRelatedness() {
        return sentenceBlockRelatedness;
    }

    /**
     * @param sentenceBlockRelatedness
     */
    public void setSentenceBlockRelatedness(SemanticRelatedness[] sentenceBlockRelatedness) {
        this.sentenceBlockRelatedness = sentenceBlockRelatedness;
    }

    /**
     * @return
     */
    public SemanticRelatedness[][] getSentenceRelatedness() {
        return sentenceRelatedness;
    }

    /**
     * @param sentenceRelatedness
     */
    public void setSentenceRelatedness(SemanticRelatedness[][] sentenceRelatedness) {
        this.sentenceRelatedness = sentenceRelatedness;
    }

    /**
     * @return
     */
    public SemanticRelatedness[][] getPrunnedSentenceRelatedness() {
        return prunnedSentenceRelatedness;
    }

    /**
     * @param prunnedSentenceRelatedness
     */
    public void setPrunnedSentenceRelatedness(SemanticRelatedness[][] prunnedSentenceRelatedness) {
        this.prunnedSentenceRelatedness = prunnedSentenceRelatedness;
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
    public SemanticRelatedness getPrevSentenceBlockSimilarity() {
        return prevSentenceBlockRelatedness;
    }

    /**
     * @param prevSentenceBlockRelatedness
     */
    public void setPrevSentenceBlockRelatedness(SemanticRelatedness prevSentenceBlockRelatedness) {
        this.prevSentenceBlockRelatedness = prevSentenceBlockRelatedness;
    }

    /**
     * @return
     */
    public SemanticRelatedness getNextSentenceBlockRelatedness() {
        return nextSentenceBlockRelatedness;
    }

    /**
     * @param nextSentenceBlockRelatedness
     */
    public void setNextSentenceBlockRelatedness(SemanticRelatedness nextSentenceBlockRelatedness) {
        this.nextSentenceBlockRelatedness = nextSentenceBlockRelatedness;
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
    public String toString() {
        String s = "";
        s += "{\n";
        s = sentences.stream().map((sentence) -> "\t" + sentence.toString() + "\n").reduce(s, String::concat);
        s += "}\n[" + getScore() + "]\n";
        return s;
    }
}
