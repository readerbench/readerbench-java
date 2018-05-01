package com.readerbench.textualcomplexity;

import com.readerbench.textualcomplexity.cohesion.CohesionFactory;
import com.readerbench.textualcomplexity.connectives.ConnectivesFactory;
import com.readerbench.textualcomplexity.coreference.CoreferenceFactory;
import com.readerbench.textualcomplexity.dependencies.SyntacticDependenciesFactory;
import com.readerbench.textualcomplexity.dialogism.DialogismFactory;
import com.readerbench.textualcomplexity.entityDensity.EntityDensityFactory;
import com.readerbench.textualcomplexity.readability.ReadabilityFactory;
import com.readerbench.textualcomplexity.rhythm.RhythmFactory;
import com.readerbench.textualcomplexity.surface.SurfaceFactory;
import com.readerbench.textualcomplexity.syntax.SyntaxFactory;
import com.readerbench.textualcomplexity.wordComplexity.WordComplexityFactory;
import com.readerbench.textualcomplexity.wordLists.WordListsIndicesFactory;

/**
 *
 * @author Stefan Ruseti
 */
public enum ComplexityIndexType {
    READABILITY(new ReadabilityFactory()),
    SURFACE(new SurfaceFactory()),
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
