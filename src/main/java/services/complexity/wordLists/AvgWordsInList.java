/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordLists;

import data.AbstractDocument;
import data.AnalysisElement;
import data.sentiment.SentimentValence;
import java.util.Objects;
import services.complexity.AbstractComplexityIndex;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import utils.IndexLevel;

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
