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
package com.readerbench.services.complexity;

import com.readerbench.services.complexity.CAF.CAFFactory;
import com.readerbench.services.complexity.cohesion.CohesionFactory;
import com.readerbench.services.complexity.connectives.ConnectivesFactory;
import com.readerbench.services.complexity.coreference.CoreferenceFactory;
import com.readerbench.services.complexity.dependencies.SyntacticDependenciesFactory;
import com.readerbench.services.complexity.dialogism.DialogismFactory;
import com.readerbench.services.complexity.entityDensity.EntityDensityFactory;
import com.readerbench.services.complexity.readability.ReadabilityFactory;
import com.readerbench.services.complexity.rhythm.RhythmFactory;
import com.readerbench.services.complexity.surface.SurfaceFactory;
import com.readerbench.services.complexity.syntax.SyntaxFactory;
import com.readerbench.services.complexity.wordComplexity.WordComplexityFactory;
import com.readerbench.services.complexity.wordLists.WordListsIndicesFactory;

/**
 *
 * @author Stefan Ruseti
 */
public enum ComplexityIndexType {
    READABILITY(new ReadabilityFactory()),
    SURFACE(new SurfaceFactory()),
    CAF(new CAFFactory()),
    SYNTAX(new SyntaxFactory()),
    WORD_COMPLEXITY(new WordComplexityFactory()),
    ENTITY_DENSITY(new EntityDensityFactory()),
    COREFERENCE(new CoreferenceFactory()),
    CONNECTIVES(new ConnectivesFactory()),
    COHESION(new CohesionFactory()),
    DIALOGISM(new DialogismFactory()),
    RHYTHM(new RhythmFactory()),
    SEMANTIC_DEPENDENCIES(new SyntacticDependenciesFactory()),
    WORD_LISTS(new WordListsIndicesFactory());
    
    private final ComplexityIndicesFactory factory;

    private ComplexityIndexType(ComplexityIndicesFactory factory) {
        this.factory = factory;
    }

    public ComplexityIndicesFactory getFactory() {
        return factory;
    }
}
