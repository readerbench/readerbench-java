/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordLists;

import data.AnalysisElement;
import data.sentiment.SentimentValence;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;

/**
 *
 * @author stefan
 */
public abstract class AvgWordsInList  extends ComplexityIndex {

    protected SentimentValence valence;

    public AvgWordsInList(ComplexityIndecesEnum index, SentimentValence valence) {
        super(index, valence.getName());
        this.valence = valence;
    }
    
    protected double countWords(AnalysisElement data) {
        return data.getWordOccurences().entrySet().stream()
            .filter(e -> e.getKey().getSentiment() != null)
            .filter(e -> e.getKey().getSentiment().get(valence) != null)
            .mapToDouble(
                    e -> e.getKey().getSentiment().get(valence) * e.getValue())
            .sum();
    }
}