/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.wordLists;

import com.readerbench.dao.ValenceDAO;
import com.readerbench.data.Lang;
import com.readerbench.data.sentiment.SentimentValence;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.complexity.ComplexityIndicesFactory;
import com.readerbench.services.complexity.IndexLevel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefan
 */
public class WordListsIndicesFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        ValenceDAO.getInstance().findByLang(lang).stream()
            .map(sv -> SentimentValence.get(sv.getIndexLabel()))
            .forEach(sv -> {
                result.add(new AvgWordsInList(
                        ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_DOC, 
                        sv,
                        IndexLevel.DOC));
                result.add(new AvgWordsInList(
                        ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_BLOCK, 
                        sv,
                        IndexLevel.BLOCK));
                result.add(new AvgWordsInList(
                        ComplexityIndicesEnum.AVG_WORDS_IN_LIST_PER_SENTENCE, 
                        sv,
                        IndexLevel.SENTENCE));
            });
        return result;
    }
    
}
