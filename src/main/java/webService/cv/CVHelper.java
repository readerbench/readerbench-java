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
			String pathToLSA,
			String pathToLDA,
			Lang lang,
			boolean usePOSTagging,
			boolean computeDialogism,
			double threshold
			) {
		
		ResultCv result = new ResultCv();
		
		// topic extraction
		result.setConcepts(ConceptMap.getTopics(
				document, threshold));
		
		// word occurrences
		Map<String, Integer> wordOccurences = new HashMap<String, Integer>();
		List<String> positiveWords = new ArrayList<String>();
		List<String> negativeWords = new ArrayList<String>();
		Map<String, List<String>> liwcEmotions = new HashMap<String, List<String>>();
		liwcEmotions.put(SentimentValence.get("Affect_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Posemo_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Negemo_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Anx_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Anger_LIWC").getName(), new ArrayList<String>());
		liwcEmotions.put(SentimentValence.get("Sad_LIWC").getName(), new ArrayList<String>());
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
					if (fanValence >= 5) positiveWords.add(word.getLemma());
					else negativeWords.add(word.getLemma());
					
					// FAN weighted average
					upperValue += fanValence * (1 + Math.log(document.getWordOccurences().get(word)));
					lowerValue += 1 + Math.log(document.getWordOccurences().get(word));
				}
			}
			
			if (lowerValue == 0) {
				result.setFanWeightedAverage(0);
			}
			else {
				result.setFanWeightedAverage(upperValue / lowerValue);
			}
			
			// LIWC
			Double liwcSentimnet; 
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
			
		}
		
		// textual complexity
		TextualComplexity textualComplexity = new TextualComplexity(document, lang, usePOSTagging, computeDialogism);
		result.setTextualComplexity(textualComplexity.getComplexityIndices());
		
		// number of images
		result.setImages(pdfConverter.getImages());

		// average number of images per page
		result.setAvgImagesPerPage(pdfConverter.getAvgImagesPerPage());
				
		// number of colors
		result.setColors(pdfConverter.getColors());
		
		// average number of colors per page
		result.setAvgColorsPerPage(pdfConverter.getAvgColorsPerPage());
		
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
		
		// normalized number of paragraphs
		result.setNormalizedParagraphs(result.getParagraphs() / result.getPages());
		
		// normalized number of sentences
		result.setNormalizedSentences(result.getSentences() / result.getPages());
		
		// normalized number of words
		result.setNormalizedWords(result.getWords() / result.getPages());
		
		// normalized number of content words
		result.setNormalizedContentWords(result.getContentWords() / result.getPages());
		// positive words
		result.setPositiveWords(positiveWords);
		
		// negative words
		result.setNegativeWords(negativeWords);
		
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
