/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.data.cscl;

/**
 *
 * @author gabigutu
 */
public enum SimilarityFormula {
    READERBENCH_SIM("ReaderBenchSim", "ReaderBench similarity"),
    NORMALIZED_SIM("NormalizedSim", "Normalized ReaderBench similarity"),
    MIHALCEA_SIM("MihalceaSim", "Rada Mihalcea similarity");

    private final String acronym;
    private final String name;

    private SimilarityFormula(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }
}
