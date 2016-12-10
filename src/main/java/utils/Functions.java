/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import data.AbstractDocument;
import data.AnalysisElement;
import data.Block;
import data.Sentence;
import data.document.Document;
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
            default:
                return null;
        }
    }
}
