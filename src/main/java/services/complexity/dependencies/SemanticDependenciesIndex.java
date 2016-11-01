/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.dependencies;

import data.AbstractDocument;
import data.Lang;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.semanticModels.SimilarityType;

/**
 *
 * @author stefan
 */
public class SemanticDependenciesIndex extends ComplexityIndex{

    public SemanticDependenciesIndex(ComplexityIndecesEnum index, Lang lang, SimilarityType simType, String aux) {
        super(index, lang, simType, aux);
    }

    @Override
    public double compute(AbstractDocument d) {
        d.getBlocks().stream()
            .filter(Objects::nonNull)
            .flatMap(b -> b.getSentences().stream())
            .flatMap(s -> StreamSupport.stream(s.getDependencies().edgeIterable().spliterator(), false))
            .map(edge -> edge.getRelation())
            .map(dep -> dep.getParent() == null ? dep : dep.getParent())
            .forEachOrdered(dep -> {
                System.out.println(dep.getLongName() + ": " + dep.getShortName());
            });
        return 0.;
    }
}
