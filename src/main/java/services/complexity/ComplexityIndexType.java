/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity;

import services.complexity.CAF.CAFFactory;
import services.complexity.cohesion.CohesionFactory;
import services.complexity.connectives.ConnectivesFactory;
import services.complexity.coreference.CoreferenceFactory;
import services.complexity.dialogism.DialogismFactory;
import services.complexity.entityDensity.EntityDensityFactory;
import services.complexity.readability.ReadabilityFactory;
import services.complexity.surface.SurfaceFactory;
import services.complexity.syntax.SyntaxFactory;
import services.complexity.wordComplexity.WordComplexityFactory;

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
    RHYTHM(null);
    
    private final ComplexityIndecesFactory factory;

    private ComplexityIndexType(ComplexityIndecesFactory factory) {
        this.factory = factory;
    }

    public ComplexityIndecesFactory getFactory() {
        return factory;
    }
}
