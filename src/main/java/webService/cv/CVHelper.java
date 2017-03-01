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
package webService.cv;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.SemanticCohesion;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
import services.semanticModels.SimilarityType;
import webService.keywords.KeywordsHelper;
import webService.result.ResultCv;
import webService.services.ConceptMap;
import webService.services.TextualComplexity;

public class CVHelper {

    public static ResultCv process(
            AbstractDocument document,
            AbstractDocument keywordsDocument,
            PdfToTextConverter pdfConverter,
            Set<String> keywords,
            Set<String> ignoreWords,
            Map<String, String> hm,
            double deltaFAN
    ) {
        ResultCv result = new ResultCv();

        // topic extraction
        Set<Word> ignoreWordsAsObject = new HashSet<>();
        for (String word : ignoreWords) {
            ignoreWordsAsObject.add(Word.getWordFromConcept(word.replaceAll("\\s+", "").toLowerCase(), Lang.fr));
        }
        result.setConcepts(ConceptMap.getKeywords(document, Double.parseDouble(hm.get("threshold")), ignoreWordsAsObject));

        // word occurrences
        List<String> positiveWords = new ArrayList<>();
        List<String> negativeWords = new ArrayList<>();
        List<String> neutralWords = new ArrayList<>();
        Map<String, List<String>> liwcEmotions = new HashMap<>();

        List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
        for (SentimentValence svLiwc : sentimentValences) {
            if (svLiwc.getName().contains("LIWC")) {
                liwcEmotions.put(svLiwc.getName(), new ArrayList<>());
            }
        }

        double upperValue = 0, lowerValue = 0;
        for (Map.Entry<Word, Integer> entry : document.getWordOccurences().entrySet()) {
            Word word = entry.getKey();
            SentimentEntity se = word.getSentiment();
            if (se == null) {
                continue;
            }

            // FAN (ANEW FR)
            SentimentValence sv = SentimentValence.get("Valence_ANEW");
            if (sv != null) {
                Double fanValence = se.get(sv);
                if (fanValence != null) {
                    if (fanValence >= deltaFAN) {
                        positiveWords.add(word.getLemma());
                    } else if (fanValence <= -deltaFAN) {
                        negativeWords.add(word.getLemma());
                    } else {
                        neutralWords.add(word.getLemma());
                    }

                    // FAN weighted average
                    upperValue += fanValence * (1 + Math.log(document.getWordOccurences().get(word)));
                    lowerValue += 1 + Math.log(document.getWordOccurences().get(word));
                }
            }

            if (lowerValue == 0) {
                result.setFanWeightedAverage(0);
            } else {
                result.setFanWeightedAverage(Formatting.formatNumber(upperValue / lowerValue));
            }

            // LIWC
            Double liwcSentimnet;

            //sentimentValences = SentimentValence.getAllValences();
            for (SentimentValence svLiwc : sentimentValences) {
                if (svLiwc.getName().contains("LIWC")) {
                    liwcSentimnet = se.get(svLiwc);
                    if (liwcSentimnet != null && liwcSentimnet > 0) {
                        liwcEmotions.get(svLiwc.getName()).add(word.getLemma());
                    }
                }
            }
        }

        // remove any LIWC category that does not contain words
        for (SentimentValence svLiwc : sentimentValences) {
            if (svLiwc.getName().contains("LIWC")) {
                if (liwcEmotions.get(svLiwc.getName()).isEmpty()) {
                    liwcEmotions.remove(svLiwc.getName());
                }
            }
        }

        // textual complexity
        Lang lang = Lang.getLang(hm.get("lang"));
        TextualComplexity textualComplexity = new TextualComplexity(document, lang, Boolean.parseBoolean(hm.get("postagging")), Boolean.parseBoolean(hm.get("dialogism")));
        result.setTextualComplexity(textualComplexity.getComplexityIndices());
        result.setImages(pdfConverter.getImages());
        result.setColors(pdfConverter.getColors());
        result.setPages(pdfConverter.getPages());
        result.setParagraphs(document.getNoBlocks());
        result.setSentences(document.getNoSentences());
        result.setWords(document.getNoWords());
        result.setContentWords(document.getNoContentWords());
        result.setFontTypes(pdfConverter.getFontTypes());
        result.setFontTypesSimple(pdfConverter.getFontTypesSimple());
        result.setFontSizes(pdfConverter.getFontSizes());
        result.setMinFontSize(pdfConverter.getMinFontSize());
        result.setMaxFontSize(pdfConverter.getMaxFontSize());
        result.setTotalCharacters(pdfConverter.getTotalCharacters());
        result.setBoldCharacters(pdfConverter.getBoldCharacters());
        result.setBoldCharsCoverage(pdfConverter.getBoldCharsCoverage());
        result.setItalicCharacters(pdfConverter.getItalicCharacters());
        result.setItalicCharsCoverage(pdfConverter.getItalicCharsCoverage());
        result.setBoldItalicCharacters(pdfConverter.getBoldItalicCharacters());
        result.setBoldItalicCharsCoverage(pdfConverter.getBoldItalicCharsCoverage());
        result.setPositiveWords(positiveWords);
        result.setNegativeWords(negativeWords);
        result.setNeutralWords(neutralWords);
        result.setLiwcEmotions(liwcEmotions);
        result.setKeywords(KeywordsHelper.getKeywords(document, keywordsDocument, keywords, hm));

        // (keywords, document) relevance
        SemanticCohesion scKeywordsDocument = new SemanticCohesion(keywordsDocument, document);
        result.setKeywordsDocumentRelevance(Formatting.formatNumber(scKeywordsDocument.getCohesion()));
        EnumMap<SimilarityType, Double> semanticSimilarities = scKeywordsDocument.getSemanticSimilarities();
        Map<String, Double> similarityScores = new HashMap<>();
        for (Entry<SimilarityType, Double> semanticSimilarity : semanticSimilarities.entrySet()) {
            similarityScores.put(semanticSimilarity.getKey().getAcronym(), Formatting.formatNumber(semanticSimilarity.getValue()));
        }
        result.setKeywordsDocumentSimilarity(similarityScores);

        return result;
    }
}
