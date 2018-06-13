/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.wordLists;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.AbstractComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.IndexLevel;

/**
 *
 * @author stefan
 */
public class AvgWordsInList extends AbstractComplexityIndex {

    protected String valence;

    public AvgWordsInList(ComplexityIndicesEnum index, Lang lang, String valence, IndexLevel level) {
        super(index, lang, valence, level);
        this.valence = valence;
    }

    private double countWords(AnalysisElement data) {
        return data.getWordOccurences().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .mapToDouble(e -> WordValences.getValenceForWord(e.getKey(), valence) * e.getValue())
                .sum();
    }

    @Override
    public double compute(AbstractDocument d) {
        return normalize(d, streamFunction.apply(d)
                .mapToDouble(this::countWords)
                .average().orElse(ComplexityIndices.IDENTITY));
    }
}
