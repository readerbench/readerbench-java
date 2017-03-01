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
package services.comprehensionModel.utils.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.comprehensionModel.utils.distanceStrategies.FullSemanticSpaceWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.SyntacticWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.utils.CMCorefIndexer;
import services.comprehensionModel.utils.distanceStrategies.utils.CMSyntacticGraph;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.ISemanticModel;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Sentence;
import data.Word;
import data.document.Document;
import services.semanticModels.utils.WordSimilarityContainer;

/**
 *
 * @author Ionut Paraschiv
 */
public class CMIndexer {

    private final ISemanticModel semanticModel;
    private final String text;
    public AbstractDocument document;

    private WordSimilarityContainer wordSimilarityContainer;
    private List<WordDistanceIndexer> syntacticIndexerList;
    private final Map<CMNodeDO, Double> nodeActivationScoreMap;

    public CMIndexer(String text, ISemanticModel semanticModel, double threshold, int noTopSimilarWords) {
        this.text = text;
        this.semanticModel = semanticModel;
        this.nodeActivationScoreMap = new TreeMap<>();
        this.loadDocument();
        this.indexFullSemanticSpaceDistances(threshold, noTopSimilarWords);
        this.indexSyntacticDistances();
    }

    public CMIndexer(String text, ISemanticModel semanticModel) {
        this.text = text;
        this.semanticModel = semanticModel;
        this.nodeActivationScoreMap = new TreeMap<>();
        this.loadDocument();
        this.indexSyntacticDistances();
    }

    private void loadDocument() {
        AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(this.text);
        List<ISemanticModel> models = new ArrayList<>();
        models.add(semanticModel);
        this.document = new Document(contents, models, semanticModel.getLanguage(), true);
    }

    private void indexFullSemanticSpaceDistances(double threshold, int noTopSimilarWords) {
        FullSemanticSpaceWordDistanceStrategy wdStrategy = new FullSemanticSpaceWordDistanceStrategy(this.semanticModel, threshold, noTopSimilarWords);
        this.wordSimilarityContainer = wdStrategy.getWordSimilarityContainer();
        this.addWordListToWordActivationScoreMap(wdStrategy.getWordList());
    }

    private void indexSyntacticDistances() {
        CMCorefIndexer corefContainer = new CMCorefIndexer(this.document, this.semanticModel.getLanguage());

        List<Sentence> sentenceList = this.document.getSentencesInDocument();
        Iterator<Sentence> sentenceIterator = sentenceList.iterator();
        this.syntacticIndexerList = new ArrayList<>();
        int sentenceNum = 0;
        while (sentenceIterator.hasNext()) {
            Sentence sentence = sentenceIterator.next();

            CMSyntacticGraph syntacticGraph = corefContainer.getCMSyntacticGraph(sentence, sentenceNum);
            SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(syntacticGraph);

            WordDistanceIndexer wdIndexer = new WordDistanceIndexer(syntacticGraph.getWordList(), syntacticStrategy);
            this.syntacticIndexerList.add(wdIndexer);
            this.addWordListToWordActivationScoreMap(wdIndexer.getWordList());
            sentenceNum++;
        }
    }

    private void addWordListToWordActivationScoreMap(List<Word> wordList) {
        for (int i = 0; i < wordList.size(); i++) {
            CMNodeDO node = new CMNodeDO(wordList.get(i), CMNodeType.Inferred);
            this.nodeActivationScoreMap.put(node, 0.0);
        }
    }

    public WordSimilarityContainer getWordSimilarityContainer() {
        return this.wordSimilarityContainer;
    }

    public List<WordDistanceIndexer> getSyntacticIndexerList() {
        return this.syntacticIndexerList;
    }

    public Map<CMNodeDO, Double> getNodeActivationScoreMap() {
        return this.nodeActivationScoreMap;
    }

    public AbstractDocument getDocument() {
        return this.document;
    }
    
    public ISemanticModel getSemanticModel() {
        return this.semanticModel;
    }
}
