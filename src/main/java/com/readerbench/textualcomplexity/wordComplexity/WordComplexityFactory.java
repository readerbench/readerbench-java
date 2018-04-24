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
package com.readerbench.textualcomplexity.wordComplexity;

import com.readerbench.coreservices.semanticModels.WordNet.WordOntologyProcessing;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import com.readerbench.textualcomplexity.IndexLevel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stefan Ruseti
 */
public class WordComplexityFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_DIFF_LEMMA_STEM, lang,
                WordComplexity::getDifferenceBetweenLemmaAndStem));
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_DIFF_WORD_STEM, lang,
                WordComplexity::getDifferenceBetweenWordAndStem));
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_MAX_DEPTH_HYPERNYM_TREE, lang,
                WordOntologyProcessing::getMaxDistanceToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_AVERAGE_DEPTH_HYPERNYM_TREE, lang,
                WordOntologyProcessing::getAverageDistanceToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_PATH_COUNT_HYPERNYM_TREE, lang,
                WordOntologyProcessing::getPathCountToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndicesEnum.WORD_POLYSEMY_COUNT, lang,
                WordComplexity::getPolysemyCount));
        if (lang == Lang.en) {
            result.add(new WordComplexity(
                    ComplexityIndicesEnum.WORD_SYLLABLE_COUNT, lang,
                    WordComplexity::getSyllables));
        }
        Map<String, Map<String, Double>> map = readCSV(lang, "AoA.csv");
        for (Map.Entry<String, Map<String, Double>> e : map.entrySet()) {
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOA_PER_DOC, 
                    e.getKey(), 
                    IndexLevel.DOC, 
                    e.getValue()));
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOA_PER_BLOCK, 
                    e.getKey(), 
                    IndexLevel.BLOCK, 
                    e.getValue()));
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOA_PER_SENTENCE, 
                    e.getKey(), 
                    IndexLevel.SENTENCE, 
                    e.getValue()));
        }
        
        map = readCSV(lang, "AoE-small.csv");
        for (Map.Entry<String, Map<String, Double>> e : map.entrySet()) {
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOE_PER_DOC, 
                    e.getKey(), 
                    IndexLevel.DOC, 
                    e.getValue()));
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOE_PER_BLOCK, 
                    e.getKey(), 
                    IndexLevel.BLOCK, 
                    e.getValue()));
            result.add(new AvgAoAScore(
                    ComplexityIndicesEnum.AVG_AOE_PER_SENTENCE, 
                    e.getKey(), 
                    IndexLevel.SENTENCE, 
                    e.getValue()));
        }
        return result;
    }

    private Map<String, Map<String, Double>> readCSV(Lang lang, String fileName) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        String aoaFile = "resources/config/" + lang.toString() + "/word lists/" + fileName;
        try (BufferedReader in = new BufferedReader(new FileReader(aoaFile))) {
            in.readLine();
            String line = in.readLine();
            String[] header = line.split(",");
            for (int i = 1; i < header.length; i++) {
                map.put(header[i], new HashMap<>());
            }
            while ((line = in.readLine()) != null) {
                String[] split = line.split(",", -1);
                String word = split[0];
                for (int i = 1; i < header.length; i++) {
                    try {
                        double val = Double.parseDouble(split[i]);
                        map.get(header[i]).put(word, val);
                    }
                    catch (NumberFormatException ex) {
                        
                    }
                }
            }
        }
        catch (FileNotFoundException ex) {
        }
        catch (IOException ex) {
        }
        return map;
    }
    
}
