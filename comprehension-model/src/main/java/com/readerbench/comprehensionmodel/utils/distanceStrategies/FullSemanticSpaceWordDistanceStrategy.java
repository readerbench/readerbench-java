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
package com.readerbench.comprehensionmodel.utils.distanceStrategies;

import com.readerbench.comprehensionmodel.utils.CMUtils;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeType;
import com.readerbench.coreservices.semanticModels.SpaceStatistics;
import com.readerbench.coreservices.semanticModels.utils.WordSimilarity;
import com.readerbench.coreservices.semanticModels.utils.WordSimilarityContainer;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class FullSemanticSpaceWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {

    private static final long serialVersionUID = -5922757870061109713L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FullSemanticSpaceWordDistanceStrategy.class);

    private WordSimilarityContainer wordDistanceContainer;
    private List<Word> uniqueWordList;
    private final ISemanticModel semanticModel;
    private final int noTopSimilarWords;
    private final double threshold;
    private final CMUtils cmUtils;

    public FullSemanticSpaceWordDistanceStrategy(ISemanticModel semanticModel, double threshold, int noTopSimilarWords) {
        this.semanticModel = semanticModel;
        this.cmUtils = new CMUtils();
        this.threshold = threshold;
        this.noTopSimilarWords = noTopSimilarWords;
        this.indexDistances();
        this.indexUniqueWordList();
        LOGGER.info("Finished indexing the semantic space ...");
    }

    private void indexDistances() {
        SpaceStatistics spaceStatistics = new SpaceStatistics(semanticModel);
        this.wordDistanceContainer = spaceStatistics.getWordSimilarityContainer();
    }

    private void indexUniqueWordList() {
        Iterator<String> wordLemmaIterator = this.wordDistanceContainer.getWordSimilarityMap().keySet().iterator();
        this.uniqueWordList = new ArrayList<>();
        while (wordLemmaIterator.hasNext()) {
            this.uniqueWordList.add(this.cmUtils.convertStringToWord(wordLemmaIterator.next(), this.semanticModel.getLanguage()));
        }
    }

    @Override
    public double getDistance(Word w1, Word w2) {
        double similarity = this.getSimilarity(w1.getLemma(), w2.getLemma());
        if (similarity == -1) {
            similarity = this.getSimilarity(w2.getLemma(), w1.getLemma());
        }
        return similarity >= threshold ? similarity : 0;
    }

    private double getSimilarity(String referenceLemma, String otherLemma) {
        PriorityQueue<WordSimilarity> similarityQueue = this.wordDistanceContainer.getWordSimilarityMap().get(referenceLemma);
        if (similarityQueue == null) {
            return -1;
        }
        Iterator<WordSimilarity> similarityIterator = similarityQueue.iterator();
        for (int currentStep = 0; currentStep < this.noTopSimilarWords && similarityIterator.hasNext(); currentStep++) {
            WordSimilarity sim = similarityIterator.next();
            if (sim.getWordLemma().equals(otherLemma)) {
                return sim.getSimilarity();
            }
        }
        return -1;
    }

    @Override
    public CMEdgeType getCMEdgeType() {
        return CMEdgeType.Semantic;
    }

    public List<Word> getWordList() {
        return this.uniqueWordList;
    }
    
    public WordSimilarityContainer getWordSimilarityContainer() {
        return this.wordDistanceContainer;
    }
}
