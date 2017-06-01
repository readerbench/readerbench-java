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
import services.converters.PdfToTxtConverter;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import webService.keywords.KeywordsHelper;
import webService.result.ResultJobQuest;
import webService.services.ConceptMap;
import webService.services.TextualComplexity;

public class JobQuestHelper {

    public static ResultJobQuest process(
            AbstractDocument document,
            AbstractDocument keywordsDocument,
            PdfToTxtConverter pdfConverter,
            Set<String> keywords,
            Set<String> ignoreWords,
            Lang lang,
            List<ISemanticModel> models,
            Boolean usePosTagging,
            Boolean computeDialogism,
            Double threshold,
            Double deltaFAN,
            Double deltaVeryFAN
    ) {
        ResultJobQuest result = new ResultJobQuest();

        // topic extraction
        Set<Word> ignoreWordsAsObject = new HashSet<>();
        for (String word : ignoreWords) {
            ignoreWordsAsObject.add(Word.getWordFromConcept(word.replaceAll("\\s+", "").toLowerCase(), Lang.fr));
        }
        result.setConcepts(ConceptMap.getKeywords(document, threshold, ignoreWordsAsObject));

        // word occurrences
        List<String> veryPositiveWords = new ArrayList<>();
        List<String> positiveWords = new ArrayList<>();
        List<String> negativeWords = new ArrayList<>();
        List<String> veryNegativeWords = new ArrayList<>();
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
                    if (5 - fanValence <= -deltaVeryFAN) {
                        veryNegativeWords.add(word.getLemma());
                    } else if (5 - fanValence <= -deltaFAN) {
                        negativeWords.add(word.getLemma());
                    } else if (5 - fanValence >= deltaVeryFAN) {
                        veryPositiveWords.add(word.getLemma());
                    } else if (5 - fanValence >= deltaFAN) {
                        positiveWords.add(word.getLemma());
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
        TextualComplexity textualComplexity = new TextualComplexity(document, lang, usePosTagging, computeDialogism);
        result.setTextualComplexity(textualComplexity.getComplexityIndices());
        result.setImages(pdfConverter.getNoImages());
        result.setColors(pdfConverter.getNoColors());
        result.setPages(pdfConverter.getNoPages());
        result.setWords(document.getNoWords());
        result.setFontTypes(pdfConverter.getNoFontTypes());
        result.setFontTypesSimple(pdfConverter.getNoFontTypesSimple());
        result.setFontSizes(pdfConverter.getNoFontSizes());
        result.setMinFontSize(pdfConverter.getMinFontSize());
        result.setMaxFontSize(pdfConverter.getMaxFontSize());
        result.setTotalCharacters(pdfConverter.getNoTotalChars());
        result.setVeryPositiveWords(veryPositiveWords);
        result.setPositiveWords(positiveWords);
        result.setNegativeWords(negativeWords);
        result.setVeryNegativeWords(veryNegativeWords);
        result.setLiwcEmotions(liwcEmotions);
        result.setKeywords(KeywordsHelper.getKeywords(document, keywordsDocument, keywords, lang, models, usePosTagging, computeDialogism, threshold));

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
