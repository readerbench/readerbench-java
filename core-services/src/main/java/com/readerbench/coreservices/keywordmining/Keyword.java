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
package com.readerbench.coreservices.keywordmining;

import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.datasourceprovider.commons.Formatting;

import com.readerbench.coreservices.data.NGram;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.semanticmodels.SimilarityType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mihai Dascalu
 */
public class Keyword implements Comparable<Keyword>, Serializable {

    private static final long serialVersionUID = -2955989168004509623L;

    private Word word;
    private NGram ngram = null;
    private double relevance;
    private double termFrequency;
    private double semanticSimilarity;
    private int count = 0;
    private double arfScore;	// average reduced frequency
    private double awtScore;	// average waiting time
    
    public Keyword(Word word, AnalysisElement e) {
        this.word = word;
        this.updateRelevance(e, word);
    }

    public Keyword(Word word, double relevance) {
        this.word = word;
        this.relevance = relevance;
    }
    
    public Keyword(NGram ngram, AnalysisElement e, int count) {
        this.ngram = ngram;
        this.word = ngram.getUnified();
        this.updateRelevance(e, ngram, count);
    }

    public Keyword(NGram ngram, double relevance) {
        this.ngram = ngram;
        this.word = ngram.getUnified();
        this.relevance = relevance;
    }

    public final void updateRelevance(AnalysisElement e, Word newWord) {
        int newCount = e.getWordOccurences().get(newWord);
        //Replace old word if the new one is more relevant
        if (newCount > e.getWordOccurences().get(word)) {
            word = newWord;
        }
        count += newCount;
        termFrequency = 1 + Math.log(count);
        if (e.getSemanticModelsAsList().isEmpty()) {
            this.relevance = termFrequency;
            return;
        }
        double semSim = SemanticCohesion.getAverageSemanticModelSimilarity(newWord, e);
        this.relevance = termFrequency * semSim;
        this.semanticSimilarity = semSim;
    }
    
    public final void updateRelevance(AnalysisElement e, NGram newWord, int newCount) {
        if (newCount > count) {
            ngram = newWord;
        }
        count += newCount;
        termFrequency = 1 + Math.log(count);
        // do not consider Idf in order to limit corpus specificity
        // double inverseDocumentFrequency = word.getIdf();
        if (e.getSemanticModelsAsList().isEmpty()) {
            this.relevance = termFrequency;
            return;
        }
        double semSim = SemanticCohesion.getAverageSemanticModelSimilarity(newWord, e);
        this.relevance = termFrequency * semSim;
        this.semanticSimilarity = semSim;
    }
    
    public final void computeARFScore(AbstractDocument d) {
		Double segmentLength, score = 0.0;
		int noWords = d.getNoWords();
		List<Word> words = d.getAllWords();
		List<Integer> occurencesPos = new ArrayList<>();
		
		for (int i = 0; i < words.size(); i++)
			if (words.get(i).compareTo(this.word) == 0)
				occurencesPos.add(i);
		
		segmentLength = noWords * 1.0 / this.count;
		for (int i = 0; i < this.count; i++) {
			Integer dist;
			if (i == 0)
				dist = occurencesPos.get(0) + noWords - occurencesPos.get(this.count - 1);
			else
				dist = occurencesPos.get(i) - occurencesPos.get(i - 1);
			score += Math.min(segmentLength, dist);
		}
		this.arfScore = score / segmentLength;
    }
    
    public double getTermFrequency() {
        return termFrequency;
    }

    public double getSemanticSimilarity() {
        return semanticSimilarity;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Keyword)) {
            return false;
        }
        Keyword t = (Keyword) obj;
        return this.getWord().getLemma().equals(t.getWord().getLemma());
    }

    @Override
    public int hashCode() {
        return word.getLemma().hashCode();
    }

    @Override
    public int compareTo(Keyword o) {
        if (o.getRelevance() == this.getRelevance()) {
            if (this.equals(o)) {
                return 0;
            }
            else {
                return getElement().getText().compareTo(o.getElement().getText());
            }
        }
        return (int) Math.signum(o.getRelevance() - this.getRelevance());
    }

    @Override
    public String toString() {
        return "{" + word + ", relevance=" + getRelevance() + "}";
    }

    public String getDescription() {
        return "(" + word.getLemma() + ", " + Formatting.formatNumber(getRelevance(), 3) + ")";
    }
    
    public double[] getModelRepresentation(SimilarityType type) {
        return getElement().getModelRepresentation(type);
    }
    
    public AnalysisElement getElement() {
        if (ngram != null) {
            return ngram;
        }
        return word;
    }
}
