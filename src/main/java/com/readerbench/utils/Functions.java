/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.utils;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AnalysisElement;
import com.readerbench.data.Block;
import com.readerbench.data.Sentence;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author stefan
 */
public class Functions {

    public static Stream<Block> streamOfBlocks(AbstractDocument d) {
        return d.getBlocks().stream()
                .filter(Objects::nonNull);
    }

    public static Stream<Sentence> streamOfSentences(AbstractDocument d) {
        return streamOfBlocks(d).flatMap(b -> b.getSentences().stream());
    }

    public static Function<AbstractDocument, Stream<? extends AnalysisElement>>
            streamOf(IndexLevel level) {
        switch (level) {
            case BLOCK:
                return Functions::streamOfBlocks;
            case SENTENCE:
                return Functions::streamOfSentences;
            case DOC:
                return (d -> Stream.of(d));
            default:
                return null;
        }
    }
            
    public static Function<AbstractDocument, Integer> getNumberOfElements(IndexLevel level) {
        switch (level) {
            case BLOCK:
                return (d -> d.getNoBlocks());
            case SENTENCE:
                return (d -> d.getNoSentences());
            case DOC:
                return (d -> 1);
            default:
                return null;
        }
    }
}
