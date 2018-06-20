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

import com.readerbench.coreservices.semanticmodels.wordnet.WordOntologyProcessing;
import com.readerbench.datasourceprovider.commons.ReadProperty;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import com.readerbench.textualcomplexity.IndexLevel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Ruseti
 */
public class WordComplexityFactory extends ComplexityIndicesFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordComplexityFactory.class);
    private static final Properties PROPERTIES = ReadProperty.getProperties("textual_complexity_paths.properties");
    private static final String PROPERTY_GENERIC_NAME = "GENERIC_%s_PATH";

    private static final Map<Lang, Map<String, Map<String, Double>>> AoA = new TreeMap<>();
    private static final Map<Lang, Map<String, Map<String, Double>>> AoE = new TreeMap<>();

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
            if (!AoA.containsKey(lang)) {
                AoA.put(lang, readCSV(lang, "AoA.csv"));
            }
            for (Map.Entry<String, Map<String, Double>> e : AoA.get(lang).entrySet()) {
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOA_PER_DOC,
                        lang,
                        e.getKey(),
                        IndexLevel.DOC,
                        e.getValue()));
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOA_PER_BLOCK,
                        lang,
                        e.getKey(),
                        IndexLevel.BLOCK,
                        e.getValue()));
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOA_PER_SENTENCE,
                        lang,
                        e.getKey(),
                        IndexLevel.SENTENCE,
                        e.getValue()));
            }
            if (!AoE.containsKey(lang)) {
                AoE.put(lang, readCSV(lang, "AoE.csv"));
            }
            for (Map.Entry<String, Map<String, Double>> e : AoE.get(lang).entrySet()) {
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOE_PER_DOC,
                        lang,
                        e.getKey(),
                        IndexLevel.DOC,
                        e.getValue()));
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOE_PER_BLOCK,
                        lang,
                        e.getKey(),
                        IndexLevel.BLOCK,
                        e.getValue()));
                result.add(new AvgAoAScore(
                        ComplexityIndicesEnum.AVG_AOE_PER_SENTENCE,
                        lang,
                        e.getKey(),
                        IndexLevel.SENTENCE,
                        e.getValue()));
            }
        }
        return result;
    }

    public Map<String, Map<String, Double>> readCSV(Lang lang, String fileName) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        String path = PROPERTIES.getProperty(String.format(PROPERTY_GENERIC_NAME, lang.name().toUpperCase())) + "/" + fileName;
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(path); BufferedReader in = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
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
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return map;
    }
}
