/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordLists;

import dao.ValenceDAO;
import data.Lang;
import data.sentiment.SentimentValence;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndicesFactory;
import services.complexity.ComplexityIndex;
import utils.IndexLevel;

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
