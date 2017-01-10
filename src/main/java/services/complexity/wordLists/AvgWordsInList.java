/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordLists;

import data.AnalysisElement;
import data.Word;
import data.sentiment.SentimentValence;
import java.util.Map;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;

/**
 *
 * @author stefan
 */
public abstract class AvgWordsInList extends ComplexityIndex {

    protected SentimentValence valence;

    public AvgWordsInList(ComplexityIndecesEnum index, SentimentValence valence) {
        super(index, valence.getName());
        this.valence = valence;
    }

    protected double countWords(AnalysisElement data) {
        double sum = 0;
        int count = 0;
        for (Map.Entry<Word, Integer> e : data.getWordOccurences().entrySet()) {
            if (e.getKey().getSentiment() != null
                    && e.getKey().getSentiment().get(valence) != null) {
                sum += e.getKey().getSentiment().get(valence) * e.getValue();
                count += e.getValue();
            }
        }
        if (count == 0) return 0;
        return sum / count;
    }
}
