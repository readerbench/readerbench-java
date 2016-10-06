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
import data.Word;
import java.util.Objects;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;

/**
 *
 * @author Mihai Dascalu
 */
public class Keyword implements Comparable<Keyword>, Serializable {

    private static final long serialVersionUID = -2955989168004509623L;

    private Word word;
    private double relevance;
    private double termFrequency;
    private double lsaSim;
    private double ldaSim;

    public Keyword(Word word, AnalysisElement e) {
        this.word = word;
        this.updateRelevance(e);
    }

    public final void updateRelevance(AnalysisElement e) {
        termFrequency = 1 + Math.log(e.getWordOccurences().get(word));
        // do not consider Idf in order to limit corpus specificity
        // double inverseDocumentFrequency = word.getIdf();
        if (e.getLDA() == null && e.getLSA() == null) {
            this.relevance = termFrequency;
            return;
        }

        // determine importance within analysis element
        lsaSim = VectorAlgebra.cosineSimilarity(word.getLSAVector(), e.getLSAVector());
        ldaSim = LDA.getSimilarity(word.getLDAProbDistribution(), e.getLDAProbDistribution());
        this.relevance = termFrequency * SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
    }

    public double getTermFrequency() {
        return termFrequency;
    }

    public double getLSASim() {
        return lsaSim;
    }

    public double getLDASim() {
        return ldaSim;
    }

    public Keyword(Word word, double relevance) {
        super();
        this.word = word;
        this.relevance = relevance;
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
}
