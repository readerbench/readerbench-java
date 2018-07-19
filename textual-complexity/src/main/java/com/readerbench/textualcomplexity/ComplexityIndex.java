package com.readerbench.textualcomplexity;

import com.readerbench.coreservices.data.complexity.IComplexityIndex;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;

import java.io.Serializable;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 *
 * @author Stefan Ruseti
 */
public abstract class ComplexityIndex implements Serializable, IComplexityIndex {

    protected ComplexityIndicesEnum index;
    protected Lang lang;
    protected SimilarityType simType;
    protected String param;

    public ComplexityIndex(ComplexityIndicesEnum index, Lang lang, SimilarityType simType, String aux) {
        this.index = index;
        this.lang = lang;
        this.simType = simType;
        this.param = aux;
    }

    public ComplexityIndex(ComplexityIndicesEnum index) {
        this(index, null, null, null);
    }

    public ComplexityIndex(ComplexityIndicesEnum index, Lang lang) {
        this(index, lang, null, null);
    }

    public ComplexityIndex(ComplexityIndicesEnum index, SimilarityType simType) {
        this(index, null, simType, null);
    }
    
    public ComplexityIndex(ComplexityIndicesEnum index, Lang lang, SimilarityType simType) {
        this(index, lang, simType, null);
    }

    public ComplexityIndex(ComplexityIndicesEnum index, String aux) {
        this(index, null, null, aux);
    }
    
    public ComplexityIndex(ComplexityIndicesEnum index, Lang lang, String aux) {
        this(index, lang, null, aux);
    }

    @Override
    public String getAcronym() {
        String acronym;
        try {
            acronym = ResourceBundle.getBundle("textual_complexity_acronyms").getString(index.name());
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

    @Override
    public String getDescription() {
        String description;
        try {
            description = ResourceBundle.getBundle("textual_complexity_descriptions", lang.getLocale()).getString(index.name());
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

    @Override
    public String getIndexName() {
        return index.name();
    }

    @Override
    public String getParam() {
        return param;
    }

    @Override
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
}
