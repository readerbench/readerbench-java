/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.dependencies;

import com.readerbench.data.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefan
 */
public class SyntacticDependenciesFactory extends ComplexityIndicesFactory {

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
            result.add(new AvgDependenciesPerBlock(ComplexityIndicesEnum.DEPENDENCY_TYPES_PER_BLOCK, dep));
            result.add(new AvgDependenciesPerSentence(ComplexityIndicesEnum.DEPENDENCY_TYPES_PER_SENTENCE, dep));
        }
        return result;
    }

}
