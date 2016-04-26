package runtime.cv;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import runtime.cscl.CSCLStatsNew;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
import webService.cv.CVHelper;
import webService.query.QueryHelper;
import webService.result.ResultCv;
import webService.result.ResultKeyword;
import webService.result.ResultNode;
import webService.result.ResultTextualComplexity;
import webService.result.ResultTopic;
import webService.result.ResultValence;

public class CVAnalyzer {
	
	public static Logger logger = Logger.getLogger(CSCLStatsNew.class);
	private static String path = "resources/in/cv_sample/";
	
	private static String pathToLSA = "resources/config/LSA/lemonde_fr";
	private static String pathToLDA = "resources/config/LDA/lemonde_fr";
	private static String language = "French";
	private static boolean usePOSTagging = false;
	private static boolean computeDialogism = false;
	private static double threshold = 0.3;
	
	private static String keywords = "prospection, prospect, développement, clients, fidélisation, chiffred’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";
	
	public static void main(String[] args) {
		
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append(
					"sep=,\nCV,concepts,textual complexity factors,images,colors,pages,paragraphs,sentences,words,content words,positive words,negative words,LIWC emotions,keywords,keywords document relevance\n");

			
			// iterate through all PDF CV files
			Files.walk(Paths.get(CVAnalyzer.path)).forEach(filePath -> {
				String filePathString = filePath.toString();
				if (filePathString.contains(".pdf")) {
					
					PdfToTextConverter pdfConverter = new PdfToTextConverter();
					String cvContent = pdfConverter.pdftoText(filePathString, true);
					
					logger.info("Continut cv: " + cvContent);
					AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, language,
							usePOSTagging, computeDialogism);
					AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, language,
							usePOSTagging, computeDialogism);
					
					ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywords, pathToLSA, pathToLDA, language, usePOSTagging, computeDialogism, threshold);
					// CV
					sb.append(filePathString + ",");
					
					// concepts
					ResultTopic resultTopic = result.getConcepts();
					List<ResultNode> resultNodes = resultTopic.getNodes();
					for (ResultNode resultNode : resultNodes) {
						sb.append(resultNode.getName() + '(' + resultNode.getValue() + ')');
						sb.append(';');
					}
					sb.append(',');
					
					// textual complexity factors
					List<ResultTextualComplexity> complexityFactors = result.getTextualComplexity();
					for (ResultTextualComplexity category : complexityFactors) {
						sb.append(category.getContent() + ": ");
						for (ResultValence factor : category.getValences()) {
							sb.append(factor.getContent() + '(' + factor.getScore() + ')');
							sb.append(';');
						}
						sb.append('|');
					}
					sb.append(',');
					
					// images
					sb.append(result.getImages());
					sb.append(',');
					
					// colors
					sb.append(result.getColors());
					sb.append(',');
					
					// pages
					sb.append(result.getPages());
					sb.append(',');
					
					// paragraphs
					sb.append(result.getParagraphs());
					sb.append(',');
					
					// sentences
					sb.append(result.getSentences());
					sb.append(',');
					
					// words
					sb.append(result.getWords());
					sb.append(',');
					
					// content words
					sb.append(result.getContentWords());
					sb.append(',');
					
					// positive words
					for (String word : result.getPositiveWords()) {
						sb.append(word + ',');
					}
					sb.append(',');
					
					// negative words
					for (String word : result.getNegativeWords()) {
						sb.append(word + ',');
					}
					sb.append(',');
					
					// LIWC emotions
					for (Map.Entry<String, List<String>> entry : result.getLiwcEmotions().entrySet()) {
						String emotion = entry.getKey();
						sb.append(emotion);
						for (String word : entry.getValue()) {
							sb.append(word + ',');
						}
						sb.append('|');
					}
					sb.append(',');
					
					// keywords
					for (ResultKeyword keyword : result.getKeywords()) {
						sb.append(keyword.getName() + '(' + keyword.getRelevance() + ") - " + keyword.getNoOccurences() + " occurences,");
					}
					sb.append(',');
					
					// (keywords, document) relevance
					sb.append(result.getKeywordsDocumentRelevance());
					sb.append("\n");
					
				}
			});
			
			File file = new File(path + "stats.csv");
			try {
				FileUtils.writeStringToFile(file, sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("Printed CV stats to CSV file: " + file.getAbsolutePath());
			
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
