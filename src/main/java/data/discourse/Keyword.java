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
package data.discourse;

import java.io.Serializable;

import data.AnalysisElement;
import data.NGram;
import data.Word;
import services.commons.Formatting;
import services.semanticModels.SimilarityType;

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
        double tf = 1 + Math.log(e.getWordOccurences().get(newWord));
        // do not consider Idf in order to limit corpus specificity
        // double inverseDocumentFrequency = word.getIdf();
        this.termFrequency += tf;
        if (e.getSemanticModels().isEmpty()) {
            this.relevance += tf;
            return;
        }
        double semSim = SemanticCohesion.getAverageSemanticModelSimilarity(newWord, e);
        this.relevance += tf * semSim;
        this.semanticSimilarity = Math.max(this.semanticSimilarity, semSim);
    }
    
    public final void updateRelevance(AnalysisElement e, NGram newWord, int count) {
        double tf = 1 + Math.log(count);
        // do not consider Idf in order to limit corpus specificity
        // double inverseDocumentFrequency = word.getIdf();
        this.termFrequency += tf;
        if (e.getSemanticModels().isEmpty()) {
            this.relevance += tf;
            return;
        }
        double semSim = SemanticCohesion.getAverageSemanticModelSimilarity(newWord, e);
        this.relevance += tf * semSim;
        this.semanticSimilarity = Math.max(this.semanticSimilarity, semSim);
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
