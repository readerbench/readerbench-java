package com.readerbench.textualcomplexity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.datasourceprovider.pojo.Lang;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author stefan
 */
public abstract class AbstractComplexityIndex extends ComplexityIndex {

    protected transient Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction;
    protected transient Function<AbstractDocument, Integer> countFunction;
    
    public AbstractComplexityIndex(
            ComplexityIndicesEnum index,
            Lang lang,
            IndexLevel level) {
        this(index, lang, null, level);
    }

    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            Lang lang,
            String aux, 
            IndexLevel level) {
        this(index, lang, aux, Functions.streamOf(level), Functions.getNumberOfElements(level));
    }
    
    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            Lang lang,
            String aux, 
            Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction) {
        this(index, lang, aux, streamFunction, null);
    }

    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            Lang lang,
            String aux, 
            Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction,
            Function<AbstractDocument, Integer> countFunction) {
        super(index, lang, aux);
        this.streamFunction = streamFunction;
        this.countFunction = countFunction;
    }    
    
    public IndexLevel getLevel() {
        for (IndexLevel level : IndexLevel.values()) {
            if (streamFunction == Functions.streamOf(level)) {
                return level;
            }
        }
        return null;
    }
    
    protected double normalize(AbstractDocument doc, double score) {
        double factor = 1.;
        if (getLevel() == IndexLevel.DOC) {
            factor = Functions.streamOfSentences(doc)
                    .flatMap(s -> s.getAllWords().stream())
                    .count();
        }
        return score / factor;
    }
    
}
