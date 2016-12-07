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
package services.complexity;

import services.complexity.CAF.CAFFactory;
import services.complexity.cohesion.CohesionFactory;
import services.complexity.connectives.ConnectivesFactory;
import services.complexity.coreference.CoreferenceFactory;
import services.complexity.dependencies.SyntacticDependenciesFactory;
import services.complexity.dialogism.DialogismFactory;
import services.complexity.entityDensity.EntityDensityFactory;
import services.complexity.readability.ReadabilityFactory;
import services.complexity.surface.SurfaceFactory;
import services.complexity.syntax.SyntaxFactory;
import services.complexity.wordComplexity.WordComplexityFactory;
import services.complexity.wordLists.WordListsIndicesFactory;

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
    RHYTHM(null),
    SEMANTIC_DEPENDENCIES(new SyntacticDependenciesFactory()),
    WORD_LISTS(new WordListsIndicesFactory());
    
    private final ComplexityIndecesFactory factory;

    private ComplexityIndexType(ComplexityIndecesFactory factory) {
        this.factory = factory;
    }

    public ComplexityIndecesFactory getFactory() {
        return factory;
    }
}
