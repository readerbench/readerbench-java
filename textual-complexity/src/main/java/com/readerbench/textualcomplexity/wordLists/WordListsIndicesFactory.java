/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.wordLists;

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import com.readerbench.textualcomplexity.IndexLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author stefan
 */
public class WordListsIndicesFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        if (!WordValences.SUPPORTED_LANGUAGES.contains(lang)) {
            return result;
        }
        WordValences.getValences(lang).keySet().stream()
                .forEach(sv -> {
                    result.add(new AvgWordsInList(
                            ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_DOC,
                            lang,
                            sv,
                            IndexLevel.DOC));
                    result.add(new AvgWordsInList(
                            ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_BLOCK,
                            lang,
                            sv,
                            IndexLevel.BLOCK));
                    result.add(new AvgWordsInList(
                            ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_SENTENCE,
                            lang,
                            sv,
                            IndexLevel.SENTENCE));
                });
        return result;
    }

}
