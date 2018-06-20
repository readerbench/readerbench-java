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
package com.readerbench.comprehensionmodel.utils.indexer;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.comprehensionmodel.utils.distanceStrategies.SyntacticWordDistanceStrategy;
import com.readerbench.comprehensionmodel.utils.distanceStrategies.utils.CMCorefIndexer;
import com.readerbench.comprehensionmodel.utils.distanceStrategies.utils.CMSyntacticGraph;
import com.readerbench.coreservices.semanticmodels.SemanticModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Ionut Paraschiv
 */
public class CMIndexer {

    private final SemanticModel semanticModel;
    private final String text;
    public AbstractDocument document;

    private List<WordDistanceIndexer> syntacticIndexerList;

    public CMIndexer(String text, SemanticModel semanticModel) {
        this.text = text;
        this.semanticModel = semanticModel;
//        this.indexSyntacticDistances();
    }

//    private void indexSyntacticDistances() {
//        CMCorefIndexer corefContainer = new CMCorefIndexer(this.document, this.semanticModel.getLanguage());
//
//        List<Sentence> sentenceList = this.document.getSentencesInDocument();
//        Iterator<Sentence> sentenceIterator = sentenceList.iterator();
//        this.syntacticIndexerList = new ArrayList<>();
//        int sentenceNum = 0;
//        while (sentenceIterator.hasNext()) {
//            Sentence sentence = sentenceIterator.next();
//
//            CMSyntacticGraph syntacticGraph = corefContainer.getCMSyntacticGraph(sentence, sentenceNum);
//            SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(syntacticGraph);
//
//            WordDistanceIndexer wdIndexer = new WordDistanceIndexer(syntacticGraph.getWordList(), syntacticStrategy);
//            this.syntacticIndexerList.add(wdIndexer);
//            sentenceNum++;
//        }
//    }

    public List<WordDistanceIndexer> getSyntacticIndexerList() {
        return this.syntacticIndexerList;
    }

    public AbstractDocument getDocument() {
        return this.document;
    }

    public SemanticModel getSemanticModel() {
        return this.semanticModel;
    }
}
