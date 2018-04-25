/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readerbench.datasourceprovider.data.document;

import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.readingstrategies.ReadingStrategies;
import com.readerbench.textualcomplexity.ComplexityIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Summary extends Metacognition {

    private static final Logger LOGGER = LoggerFactory.getLogger(Summary.class);

    private static final long serialVersionUID = -3087279898902437719L;

    private SemanticCohesion cohesion;

    public Summary(String path, Document initialReadingMaterial) {
        super(path, initialReadingMaterial);
    }

    public void setCohesion(SemanticCohesion cohesion) {
        this.cohesion = cohesion;
    }
    
    public SemanticCohesion getCohesion() {
        return cohesion;
    }
    
    

    public void computeAll(boolean computeDialogism, boolean useBigrams) {
        
    }
}
