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
package com.readerbench.coreservices.data.document;

import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A generalization of Document in which the block list for meta-cognitions
 * represents the actual list of verbalizations.
 *
 * @author Mihai Dascalu
 */
public class Metacognition extends Document {

    private static final long serialVersionUID = 3740041983851246989L;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Metacognition.class);

    private Document referredDoc; // the initial referred document
    private SemanticCohesion[] blockSimilarities; // similarities with referred document blocks
    private final List<EnumMap<ReadingStrategyType, Integer>> automatedReadingStrategies;
    private final List<EnumMap<ReadingStrategyType, Integer>> annotatedReadingStrategies;
    private final List<SemanticCohesion> avgCohesion;
    private double annotatedFluency;
    private double annotatedComprehensionScore;
    private int comprehensionClass;
    private List<String> tutors = new LinkedList<>();

    public Metacognition(String path, Document initialReadingMaterial) {
        // build the corresponding structure of verbalizations
        super(path, initialReadingMaterial.getSemanticModelsAsList(), initialReadingMaterial.getLanguage());
        this.referredDoc = initialReadingMaterial;
        automatedReadingStrategies = new ArrayList<>();
        annotatedReadingStrategies = new ArrayList<>();
        avgCohesion = new ArrayList<>();
    }

    //global count of reading strategies given as input argument
    public EnumMap<ReadingStrategyType, Integer> getAllRS(List<EnumMap<ReadingStrategyType, Integer>> rsList) {
        EnumMap<ReadingStrategyType, Integer> cumulativeRS = new EnumMap<>(ReadingStrategyType.class);
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            cumulativeRS.put(rs, 0);
        }
        for (int i = 0; i < rsList.size(); i++) {
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                cumulativeRS.put(rs, cumulativeRS.get(rs) + rsList.get(i).get(rs));
            }
        }
        return cumulativeRS;
    }

    public Document getReferredDoc() {
        return referredDoc;
    }

    public void setReferredDoc(Document referredDoc) {
        this.referredDoc = referredDoc;
    }

    public SemanticCohesion[] getBlockSimilarities() {
        return blockSimilarities;
    }

    public void setBlockSimilarities(SemanticCohesion[] blockSimilarities) {
        this.blockSimilarities = blockSimilarities;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAutomatedRS() {
        return automatedReadingStrategies;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAnnotatedRS() {
        return annotatedReadingStrategies;
    }

    public List<String> getTutors() {
        return tutors;
    }

    public void setTutors(List<String> tutors) {
        this.tutors = tutors;
    }

    public double getAnnotatedFluency() {
        return annotatedFluency;
    }

    public void setAnnotatedFluency(double annotatedFluency) {
        this.annotatedFluency = annotatedFluency;
    }

    public double getAnnotatedComprehensionScore() {
        return annotatedComprehensionScore;
    }

    public void setAnnotatedComprehensionScore(
            double annotatedComprehensionScore) {
        this.annotatedComprehensionScore = annotatedComprehensionScore;
    }

    public int getComprehensionClass() {
        return comprehensionClass;
    }

    public void setComprehensionClass(int comprehensionClass) {
        this.comprehensionClass = comprehensionClass;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAutomatedReadingStrategies() {
        return automatedReadingStrategies;
    }

    public List<EnumMap<ReadingStrategyType, Integer>> getAnnotatedReadingStrategies() {
        return annotatedReadingStrategies;
    }

    public List<SemanticCohesion> getAvgCohesion() {
        return avgCohesion;
    }
}
