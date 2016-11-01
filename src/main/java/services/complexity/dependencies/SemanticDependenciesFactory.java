/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.dependencies;

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;
import services.semanticModels.SimilarityType;

/**
 *
 * @author stefan
 */
public class SemanticDependenciesFactory extends ComplexityIndecesFactory{

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        //TODO: filter by language
        
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new SemanticDependenciesIndex(ComplexityIndecesEnum.DEPENDENCY_TYPES_PER_BLOCK, lang, SimilarityType.LSA, ""));
        return result;
    }
    
}
