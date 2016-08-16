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
import data.discourse.Topic;
import data.document.Document;
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
			String pathToLSA,
			String pathToLDA,
			Lang lang,
			boolean usePOSTagging,
			boolean computeDialogism,
			double threshold,
			double FANthreshold,
			double deltaFAN
			) {
		
		ResultCv result = new ResultCv();
		
		// topic extraction
		result.setConcepts(ConceptMap.getTopics(document, threshold, ignore));
		
		// word occurrences
		Map<String, Integer> wordOccurences = new HashMap<String, Integer>();
		List<String> positiveWords = new ArrayList<String>();
		List<String> negativeWords = new ArrayList<String>();
		List<String> neutralWords = new ArrayList<String>();
		Map<String, List<String>> liwcEmotions = new HashMap<String, List<String>>();
		/*liwcEmotions.put(SentimentValence.get("Affect_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Posemo_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Negemo_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Anx_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Anger_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Sad_LIWC").getName(), new ArrayList<String>());*/
		
		List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
		for(SentimentValence svLiwc : sentimentValences) {
			if (svLiwc.getName().contains("LIWC") && svLiwc != null) {
				liwcEmotions.put(svLiwc.getName(), new ArrayList<String>());
			}
		}
		
		double upperValue = 0;
		double lowerValue = 0;
		for (Map.Entry<Word, Integer> entry : document.getWordOccurences().entrySet()) {
			Word word = entry.getKey();
			Integer occurrences = entry.getValue();
			wordOccurences.put(word.getLemma(), occurrences);
			SentimentEntity se = word.getSentiment();
			if (se == null) continue;
			
			// FAN (ANEW FR)
			SentimentValence sv = SentimentValence.get("Valence_ANEW");
			if (sv != null) {
				Double fanValence = se.get(sv);
				if (fanValence != null) {
					if (fanValence >= FANthreshold + deltaFAN) positiveWords.add(word.getLemma());
					else if (fanValence <= FANthreshold - deltaFAN) negativeWords.add(word.getLemma());
					else neutralWords.add(word.getLemma());
					
					// FAN weighted average
					upperValue += fanValence * (1 + Math.log(document.getWordOccurences().get(word)));
					lowerValue += 1 + Math.log(document.getWordOccurences().get(word));
				}
			}
			
			if (lowerValue == 0) {
				result.setFanWeightedAverage(0);
			}
			else {
				result.setFanWeightedAverage(Formatting.formatNumber(upperValue / lowerValue));
			}
			
			// LIWC
			Double liwcSentimnet; 
			/*
			// 125 - affect
			sv = SentimentValence.get("Affect_LIWC");
			if (sv != null) {
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			
			// 126 - emopos
			sv = SentimentValence.get("Posemo_LIWC");
			if (sv != null) {
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			
			// 127 - emoneg
			if (sv != null) {
				sv = SentimentValence.get("Negemo_LIWC");
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			
			// 128 - anxiete
			if (sv != null) {
				sv = SentimentValence.get("Anx_LIWC");
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			
			// 129 - colere
			sv = SentimentValence.get("Anger_LIWC");
			if (sv != null) {
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			
			// 130 - tristesse
			if (sv != null) {
				sv = SentimentValence.get("Sad_LIWC");
				liwcSentimnet = se.get(sv);
				if (liwcSentimnet != null && liwcSentimnet > 0) 
					liwcEmotions.get(sv.getName()).add(word.getLemma());
			}
			*/
			
			//sentimentValences = SentimentValence.getAllValences();
			for(SentimentValence svLiwc : sentimentValences) {
				if (svLiwc.getName().contains("LIWC") && svLiwc != null) {
					liwcSentimnet = se.get(svLiwc);
					if (liwcSentimnet != null && liwcSentimnet > 0) 
						liwcEmotions.get(svLiwc.getName()).add(word.getLemma());
				}
			}
			
		}
		
		// textual complexity
		TextualComplexity textualComplexity = new TextualComplexity(document, lang, usePOSTagging, computeDialogism);
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
		
		// positive words
		result.setPositiveWords(positiveWords);
		
		// negative words
		result.setNegativeWords(negativeWords);
		
		// neutral words
		result.setNeutralWords(neutralWords);
				
		// LIWC emotions
		result.setLiwcEmotions(liwcEmotions);
		
		// specific keywords
		result.setKeywords(KeywordsHelper.getKeywords(document, keywordsDocument, keywords,
				pathToLSA, pathToLDA, lang,
				usePOSTagging, computeDialogism, threshold));
		
		// (keywords, document) relevance
		SemanticCohesion scKeywordsDocument = new SemanticCohesion(keywordsDocument, document);
		result.setKeywordsDocumentRelevance(Formatting.formatNumber(scKeywordsDocument.getCohesion()));
		
		return result;
		
	}

}
