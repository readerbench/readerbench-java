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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.SemanticCohesion;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
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
            Set<String> ignore,
            Map<String, String> hm,
            double deltaFAN
    ) {

        ResultCv result = new ResultCv();

        // topic extraction
        result.setConcepts(ConceptMap.getTopics(document, Double.parseDouble(hm.get("threshold")), ignore));

        // word occurrences
        Map<String, Integer> wordOccurences = new HashMap<>();
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

        double upperValue = 0;
        double lowerValue = 0;
        for (Map.Entry<Word, Integer> entry : document.getWordOccurences().entrySet()) {
            Word word = entry.getKey();
            Integer occurrences = entry.getValue();
            wordOccurences.put(word.getLemma(), occurrences);
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

        // textual complexity
        Lang lang = Lang.getLang(hm.get("lang"));
        TextualComplexity textualComplexity = new TextualComplexity(document, lang, Boolean.parseBoolean(hm.get("postagging")), Boolean.parseBoolean(hm.get("dialogism")));
        result.setTextualComplexity(textualComplexity.getComplexityIndices());

        // number of images
        result.setImages(pdfConverter.getImages());

        // number of colors
        result.setColors(pdfConverter.getColors());

        // number of pages
        result.setPages(pdfConverter.getPages());

        // number of paragraphs
        result.setParagraphs(document.getNoBlocks());

        // number of sentences
        result.setSentences(document.getNoSentences());

        // number of words
        result.setWords(document.getNoWords());

        // number of content words
        result.setContentWords(document.getNoContentWords());

        result.setFontTypes(pdfConverter.getFontTypes());
        result.setFontTypesSimple(pdfConverter.getFontTypesSimple());
        result.setFontSizes(pdfConverter.getFontSizes());
        result.setMinFontSize(pdfConverter.getMinFontSize());
        result.setMaxFontSize(pdfConverter.getMaxFontSize());
        result.setTotalCharacters(pdfConverter.getTotalCharacters());
        result.setBoldCharacters(pdfConverter.getBoldCharacters());
        result.setItalicCharacters(pdfConverter.getItalicCharacters());
        result.setBoldItalicCharacters(pdfConverter.getBoldItalicCharacters());

        // positive words
        result.setPositiveWords(positiveWords);

        // negative words
        result.setNegativeWords(negativeWords);

        // neutral words
        result.setNeutralWords(neutralWords);

        // LIWC emotions
        result.setLiwcEmotions(liwcEmotions);

        // specific keywords
        result.setKeywords(KeywordsHelper.getKeywords(document, keywordsDocument, keywords, hm));

        // (keywords, document) relevance
        SemanticCohesion scKeywordsDocument = new SemanticCohesion(keywordsDocument, document);
        result.setKeywordsDocumentRelevance(Formatting.formatNumber(scKeywordsDocument.getCohesion()));

        return result;

    }

}
