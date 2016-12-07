/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordLists;

import data.AbstractDocument;
import data.sentiment.SentimentValence;
import java.util.Objects;
import services.complexity.ComplexityIndecesEnum;

/**
 *
 * @author stefan
 */
public class AvgWordsInListPerBlock extends AvgWordsInList {

    public AvgWordsInListPerBlock(ComplexityIndecesEnum index, SentimentValence valence) {
        super(index, valence);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().stream()
                .filter(Objects::nonNull)
                .mapToDouble(this::countWords)
                .average().orElse(0.);
    }

}
