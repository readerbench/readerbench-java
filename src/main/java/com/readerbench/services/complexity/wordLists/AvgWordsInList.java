/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.wordLists;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AnalysisElement;
import com.readerbench.data.sentiment.SentimentValence;
import com.readerbench.services.complexity.AbstractComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.complexity.IndexLevel;

/**
 *
 * @author stefan
 */
public class AvgWordsInList extends AbstractComplexityIndex {

    protected SentimentValence valence;

    public AvgWordsInList(ComplexityIndicesEnum index, SentimentValence valence, IndexLevel level) {
        super(index, valence.getName(), level);
        this.valence = valence;
    }

    private double countWords(AnalysisElement data) {
        return data.getWordOccurences().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> e.getKey().getSentiment() != null)
                .filter(e -> e.getKey().getSentiment().get(valence) != null)
                .mapToDouble(
                        e -> e.getKey().getSentiment().get(valence) * e.getValue())
                .sum();
    }

    @Override
    public double compute(AbstractDocument d) {
        return streamFunction.apply(d)
                .mapToDouble(this::countWords)
                .average().orElse(0.);
    }
}
