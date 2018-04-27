/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.datasourceprovider.data;

import com.readerbench.coreservices.semanticmodels.word2vec.Word2VecModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author stefan
 */
public class NGram extends AnalysisElement {

    private final List<Word> words;
    private Word unified;

    public NGram(List<Word> words) {
        this.words = words;
        initialize();
    }

    public NGram(Word first, Word second) {
        words = new ArrayList<>();
        words.add(first);
        words.add(second);
        initialize();
    }

    public NGram(Word first, Word second, Word third) {
        words = new ArrayList<>();
        words.add(first);
        words.add(second);
        words.add(third);
        initialize();
    }

    public List<Word> getWords() {
        return words;
    }

    private void initialize() {
        setWordOccurences(words.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.reducing(0, e -> 1, Integer::sum))));
        semanticModels = words.get(0).semanticModels;
        determineSemanticDimensions();
        String label = words.stream()
                .map(Word::getLemma)
                .collect(Collectors.joining("_"));
        unified = new Word(label, label, label, null, null, words.get(0).getLanguage());
        if (semanticModels.containsKey(SimilarityType.WORD2VEC)) {
            Word2VecModel model = (Word2VecModel) semanticModels.get(SimilarityType.WORD2VEC);
            double[] v = model.getWordRepresentation(unified);
            if (v != null) {
                modelVectors.put(SimilarityType.WORD2VEC, v);
            }
        }
    }

    public Word getUnified() {
        return unified;
    }

    @Override
    public String toString() {
        return unified.getLemma();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.words);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NGram other = (NGram) obj;
        if (!Objects.equals(this.words, other.words)) {
            return false;
        }
        return true;
    }

    @Override
    public String getText() {
        return unified.getText();
    }
}
