package com.readerbench.textualcomplexity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AnalysisElement;

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
            IndexLevel level) {
        this(index, null, level);
    }

    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            String aux, 
            IndexLevel level) {
        this(index, aux, Functions.streamOf(level), Functions.getNumberOfElements(level));
    }
    
    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            String aux, 
            Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction) {
        this(index, aux, streamFunction, null);
    }

    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            String aux, 
            Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction,
            Function<AbstractDocument, Integer> countFunction) {
        super(index, aux);
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
    
}
