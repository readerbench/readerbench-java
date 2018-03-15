package com.readerbench.readerbenchcore.nlp.parsing;

import edu.stanford.nlp.trees.Tree;

/**
 * Created by Florea Anda-Madalina on 07.05.2017.
 */
public class ContextSentiment {
    private Tree contextTree;
    private double valence;

    public ContextSentiment(Tree contextTree, double valence) {
        this.contextTree = contextTree;
        this.valence = valence;
    }

    public Tree getContextTree() {
        return contextTree;
    }

    public void setContextTree(Tree contextTree) {
        this.contextTree = contextTree;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }
}
