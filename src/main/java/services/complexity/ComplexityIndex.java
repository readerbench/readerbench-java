/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity;

import data.AbstractDocument;
import data.Lang;
import java.util.ResourceBundle;
import services.semanticModels.WordNet.SimilarityType;
import utils.localization.LocalizationUtils;

/**
 *
 * @author Stefan
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
        String acronym = ResourceBundle.getBundle("utils.localization.index_acronyms").getString(index.name());
        if (acronym == null || acronym.isEmpty()) {
            return index.name();
        }
        return acronym;
    }

    public String getDescription() {
        String description = LocalizationUtils.getTranslation(index.name());
        if (simType != null) description += " (" + simType.name() + ")";
        if (param != null) description += " (" + param + ")";
        return description;
    }

    public String getCategoryName() {
        return index.getType().name();
    }
}
