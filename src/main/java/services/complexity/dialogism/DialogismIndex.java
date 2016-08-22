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
package services.complexity.dialogism;

import data.AbstractDocument;
import data.discourse.SemanticChain;
import java.util.function.Function;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismIndex extends ComplexityIndex {
    
    private final Function<SemanticChain, Double> mapper;
    
    public DialogismIndex(ComplexityIndecesEnum index, Function<SemanticChain, Double> mapper) {
        super(index);
        this.mapper = mapper;
    }
    
    @Override
    public double compute(AbstractDocument d) {
        if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		return d.getSignificantVoices().parallelStream()
                .mapToDouble(mapper::apply)
                .average().orElse(ComplexityIndices.IDENTITY);
	}
    
}