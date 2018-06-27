package com.readerbench.coreservices.data.complexity;

import com.readerbench.coreservices.data.AbstractDocument;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public interface IComplexityIndex {
    String getAcronym();

    String getDescription();
    
    String getIndexName();
    
    String getParam();

    String getCategoryName();

    double compute(AbstractDocument d);
}
