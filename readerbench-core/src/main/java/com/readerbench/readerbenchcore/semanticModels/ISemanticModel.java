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
package com.readerbench.readerbenchcore.semanticModels;

import com.readerbench.data.AnalysisElement;
import com.readerbench.data.Lang;
import com.readerbench.data.Word;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

public interface ISemanticModel {

    public double getSimilarity(AnalysisElement e1, AnalysisElement e2);

    public double getSimilarity(double[] v1, double[] v2);

    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold);

    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold);

    public Set<Word> getWordSet();

    public Map<Word, double[]> getWordRepresentations();

    public double[] getWordRepresentation(Word w);

    public String getPath();

    public Lang getLanguage();

    public SimilarityType getType();

    public BiFunction<double[], double[], Double> getSimilarityFuction();

    public int getNoDimensions();
}
