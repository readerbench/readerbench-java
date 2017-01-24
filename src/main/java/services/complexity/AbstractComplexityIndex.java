/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity;

import data.AbstractDocument;
import data.AnalysisElement;
import java.util.function.Function;
import java.util.stream.Stream;
import utils.Functions;
import utils.IndexLevel;

/**
 *
 * @author stefan
 */
public abstract class AbstractComplexityIndex extends ComplexityIndex {

    protected transient Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction;
    
    public AbstractComplexityIndex(
            ComplexityIndicesEnum index,
            IndexLevel level) {
        this(index, null, level);
    }

    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            String aux, 
            IndexLevel level) {
        this(index, aux, Functions.streamOf(level));
    }
    
    public AbstractComplexityIndex(
            ComplexityIndicesEnum index, 
            String aux, 
            Function<AbstractDocument, Stream<? extends AnalysisElement>> streamFunction) {
        super(index, aux);
        this.streamFunction = streamFunction;
    }

    
}
