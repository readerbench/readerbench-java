/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity;

import data.AbstractDocument;
import data.Lang;
import java.util.Objects;
import java.util.ResourceBundle;
import services.semanticModels.WordNet.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public abstract class ComplexityIndex {

    protected ComplexityIndecesEnum index;
    protected Lang lang;
    protected SimilarityType simType;
    protected String param;

    public ComplexityIndex(ComplexityIndecesEnum index, Lang lang, SimilarityType simType, String aux) {
        this.index = index;
        this.lang = lang;
        this.simType = simType;
        this.param = aux;
    }

    public ComplexityIndex(ComplexityIndecesEnum index) {
        this(index, null, null, null);
    }

    public ComplexityIndex(ComplexityIndecesEnum index, Lang lang) {
        this(index, lang, null, null);
    }

    public ComplexityIndex(ComplexityIndecesEnum index, SimilarityType simType) {
        this(index, null, simType, null);
    }

    public ComplexityIndex(ComplexityIndecesEnum index, String aux) {
        this(index, null, null, aux);
    }

    abstract public double compute(AbstractDocument d);

    public String getAcronym() {
        String acronym;
        try {
            acronym = ResourceBundle.getBundle("utils.localization.textual_complexity_acronyms").getString(index.name());
        } catch (Exception ex) {
            acronym = null;
        }
        if (acronym == null || acronym.isEmpty()) {
            acronym = index.name();
        }
        if (simType != null) {
            acronym += "_" + simType.getAcronym();
        }
        if (param != null) {
            acronym += "_" + param;
        }
        return acronym;
    }

    public String getDescription() {
        String description;
        try {
            description = ResourceBundle.getBundle("utils.localization.textual_complexity_descriptions").getString(index.name());
        } catch (Exception ex) {
            description = null;
        }
        if (description == null || description.isEmpty()) {
            return getAcronym();
        }
        if (simType != null) {
            description += " (" + simType.getName() + ")";
        }
        if (param != null) {
            description += " (" + param.replaceAll("_", " ") + ")";
        }
        return description;
    }

    public String getCategoryName() {
        return index.getType().name();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.index);
        hash = 37 * hash + Objects.hashCode(this.lang);
        hash = 37 * hash + Objects.hashCode(this.simType);
        hash = 37 * hash + Objects.hashCode(this.param);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComplexityIndex other = (ComplexityIndex) obj;
        if (!Objects.equals(this.param, other.param)) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (this.lang != other.lang) {
            return false;
        }
        return this.simType == other.simType;
    }

    public ComplexityIndecesEnum getIndex() {
        return index;
    }
}
