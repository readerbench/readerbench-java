/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.wordLists;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AnalysisElement;
import com.readerbench.readerbenchcore.data.sentiment.SentimentValence;
import com.readerbench.textualcomplexity.AbstractComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.IndexLevel;

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
