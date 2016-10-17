/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package data.cscl;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import data.AbstractDocument;

public class Participant implements Comparable<Participant>, Serializable {

    private static final long serialVersionUID = -4515721505776009876L;

    private String name;
    private transient AbstractDocument interventions;
    private transient AbstractDocument significantInterventions;
    private double gradeAnnotator;
    private double textualComplexityLevel;
    private EnumMap<CSCLIndices, Double> indices;
    private Map<Entry<CSCLIndices, CSCLCriteria>, Double> longitudinalIndices;

    public Participant(String name, AbstractDocument d) {
        super();
        this.name = name;
        this.interventions = new Conversation(null, d.getSemanticModels(), d.getLanguage());
        this.significantInterventions = new Conversation(null, d.getSemanticModels(), d.getLanguage());
        this.indices = new EnumMap<>(CSCLIndices.class);
        this.longitudinalIndices = new HashMap<>();
        this.resetIndices();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractDocument getInterventions() {
        return interventions;
    }

    public void setInterventions(AbstractDocument interventions) {
        this.interventions = interventions;
    }

    public AbstractDocument getSignificantInterventions() {
        return significantInterventions;
    }

    public void setSignificantInterventions(AbstractDocument significantInterventions) {
        this.significantInterventions = significantInterventions;
    }

    public double getGradeAnnotator() {
        return gradeAnnotator;
    }

    public void setGradeAnnotator(double gradeAnnotator) {
        this.gradeAnnotator = gradeAnnotator;
    }

    public double getTextualComplexityLevel() {
        return textualComplexityLevel;
    }

    public void setTextualComplexityLevel(double textualComplexityLevel) {
        this.textualComplexityLevel = textualComplexityLevel;
    }

    public EnumMap<CSCLIndices, Double> getIndices() {
        return indices;
    }

    public void setIndices(EnumMap<CSCLIndices, Double> indices) {
        this.indices = indices;
    }

    public Map<Entry<CSCLIndices, CSCLCriteria>, Double> getLongitudinalIndices() {
        return longitudinalIndices;
    }

    public void setLongitudinalIndices(Map<Entry<CSCLIndices, CSCLCriteria>, Double> longitudinalIndices) {
        this.longitudinalIndices = longitudinalIndices;
    }

    private void resetIndices() {
        for (CSCLIndices index : CSCLIndices.values()) {
            indices.put(index, 0d);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean equals(Object obj) {
        Participant p = (Participant) obj;
        return this.getName().equals(p.getName());
    }

    public int compareTo(Participant o) {
        return this.getName().compareTo(o.getName());
    }
}
