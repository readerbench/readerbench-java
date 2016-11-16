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

/**
 *
 * @author stefan
 */
public class SyntacticDependenciesFactory extends ComplexityIndecesFactory {

    public static final String[] DEPS = {
        "acl",
        "advcl",
        "advmod",
        "amod",
        "appos",
        "aux",
        "auxpass",
        "case",
        "cc",
        "ccomp",
        "compound",
        "conj",
        "cop",
        "csubj",
        "csubjpass",
        "dep",
        "det",
        "discourse",
        "dislocated",
        "dobj",
        "expl",
        "foreign",
        "goeswith",
        "iobj",
        "list",
        "mark",
        "mwe",
        "name",
        "neg",
        "nmod",
        "nsubj",
        "nsubjpass",
        "nummod",
        "parataxis",
        "punct",
        "remnant",
        "reparandum",
        "root",
        "vocative",
        "xcomp"};

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        if (!lang.equals(Lang.en) && !lang.equals(Lang.fr)) {
            return result;
        }
        for (String dep : DEPS) {
            result.add(new AvgDependenciesPerBlock(ComplexityIndecesEnum.DEPENDENCY_TYPES_PER_BLOCK, dep));
            result.add(new AvgDependenciesPerSentence(ComplexityIndecesEnum.DEPENDENCY_TYPES_PER_SENTENCE, dep));
        }
        return result;
    }

}
