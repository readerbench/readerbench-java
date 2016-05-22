package runtime.cv;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Lang;
import runtime.cscl.CSCLStatsNew;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.converters.PdfToTextConverter;
import webService.ReaderBenchServer;
import webService.cv.CVHelper;
import webService.query.QueryHelper;
import webService.result.ResultCv;
import webService.result.ResultKeyword;
import webService.result.ResultNode;
import webService.result.ResultTextualComplexity;
import webService.result.ResultTopic;
import webService.result.ResultValence;
import webService.services.TextualComplexity;

public class CVAnalyzer {

	public Logger logger = Logger.getLogger(CSCLStatsNew.class);
	private String path;
	private String folder;
	private int type;

	private String pathToLSA;
	private String pathToLDA;
	private Lang lang;
	private boolean usePOSTagging = false;
	private boolean computeDialogism = false;
	private double threshold = 0.3;

	private String keywords = "prospection, prospect, développement, clients, fidélisation, chiffred’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";

	public CVAnalyzer(String path, String folder, int type, 
			String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean computeDialogism, double threshold) {
		this.path = path;
		this.folder = folder;
		this.type = type;
		this.pathToLSA = pathToLSA;
		this.pathToLDA = pathToLDA;
		this.lang = lang;
		this.usePOSTagging = usePOSTagging;
		this.computeDialogism = computeDialogism;
		this.threshold = threshold;
	}

	public void process() {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(
					"folder,type,CV,images,avg images per page,colors,avg colors per page,pages,paragraphs,sentences,words,content words,normalized paragraphs,normalized sentences, normalized words, normalized content words,positive words (FAN >= 5),negative words (FAN < 5),FAN weighted average,affect,positive emotion,negative emotion,anxiety,anger,sadness,");
			// textual complexity factors
			TextualComplexity textualComplexity = new TextualComplexity(lang, usePOSTagging, computeDialogism);
			for (IComplexityFactors f : textualComplexity.getList()) {
				for (int i : f.getIDs())
					sb.append(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[i] + ',');
			}
			sb.append("keywords document relevance,");
			// keywords
			sb.append(
					"prospection,,prospect,,développement,,clients,,fidélisation,,chiffred’affaires,,marge,,vente,,portefeuille,,négociation,,budget,,rendez-vous,,proposition,,terrain,,téléphone,,rentabilité,,business,,reporting,,veille,,secteur,,objectifs,,comptes,,animation,,suivi,,création,,gestion,,");
			// concepts
			for (int i = 0; i < 25; i++) {
				sb.append("concept" + i + ',');
				sb.append("rel" + i + ',');
			}
			sb.append("\n");
			
			Set<String> keywordsList = new HashSet<String>(Arrays.asList(keywords.split(",")));

			System.out.println("Incep procesarea CV-urilor");
			// iterate through all PDF CV files
			Files.walk(Paths.get(path)).forEach(filePath -> {
				String filePathString = filePath.toString();
				if (filePathString.contains(".pdf")) {

					PdfToTextConverter pdfConverter = new PdfToTextConverter();
					String cvContent = pdfConverter.pdftoText(filePathString, true);

					logger.info("Continut cv: " + cvContent);
					AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang,
							usePOSTagging, computeDialogism);
					AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang,
							usePOSTagging, computeDialogism);

					ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, pathToLSA,
							pathToLDA, lang, usePOSTagging, computeDialogism, threshold);
					// Folder
					sb.append(folder);
					sb.append(',');
					
					// Type
					sb.append(type);
					sb.append(',');
					
					// CV
					sb.append(filePath.getFileName().toString() + ",");

					// images
					sb.append(result.getImages());
					sb.append(',');
					
					// average images per page
					sb.append(result.getAvgImagesPerPage());
					sb.append(',');

					// colors
					sb.append(result.getColors());
					sb.append(',');
					
					// average colors per page
					sb.append(result.getAvgColorsPerPage());
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
					
					// normalized paragraphs
					sb.append(result.getNormalizedParagraphs());
					sb.append(',');

					// normalized sentences
					sb.append(result.getNormalizedSentences());
					sb.append(',');

					// normalized words
					sb.append(result.getNormalizedWords());
					sb.append(',');

					// normalized content words
					sb.append(result.getNormalizedContentWords());
					sb.append(',');

					// positive words
					sb.append(result.getPositiveWords().size());
					sb.append(',');

					// negative words
					sb.append(result.getNegativeWords().size());
					sb.append(',');
					
					// FAN weighted average
					sb.append(result.getFanWeightedAverage());
					sb.append(',');

					// LIWC emotions
					for (Map.Entry<String, List<String>> entry : result.getLiwcEmotions().entrySet()) {
						// String emotion = entry.getKey();
						// sb.append(emotion);
						sb.append(entry.getValue().size());
						sb.append(',');
					}

					// textual complexity factors
					List<ResultTextualComplexity> complexityFactors = result.getTextualComplexity();
					for (ResultTextualComplexity category : complexityFactors) {
						// sb.append(category.getContent() + ": ");
						for (ResultValence factor : category.getValences()) {
							// sb.append(factor.getContent() + '(' +
							// factor.getScore() + ')');
							sb.append(factor.getScore());
							sb.append(',');
						}
						// sb.append('|');
					}
					// sb.append(',');

					// (keywords, document) relevance
					sb.append(result.getKeywordsDocumentRelevance());
					sb.append(',');

					// keywords
					for (ResultKeyword keyword : result.getKeywords()) {
						// sb.append(keyword.getName() + '(' +
						// keyword.getRelevance() + ") - " +
						// keyword.getNoOccurences() + " occurences,");
						sb.append(keyword.getRelevance());
						sb.append(',');
						sb.append(keyword.getNoOccurences());
						sb.append(',');
					}
					// sb.append(',');

					// concepts
					ResultTopic resultTopic = result.getConcepts();
					List<ResultNode> resultNodes = resultTopic.getNodes();
					int i = 0;
					for (ResultNode resultNode : resultNodes) {
						// sb.append(resultNode.getName() + '(' +
						// resultNode.getValue() + ')');
						sb.append(resultNode.getName());
						sb.append(',');
						sb.append(resultNode.getValue());
						sb.append(',');
						i++;
						if (i == 25)
							break;
					}

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

	public static void main(String[] args) {
		BasicConfigurator.configure();
		ReaderBenchServer.initializeDB();

		/*CVAnalyzer cvAnalyzerSample = new CVAnalyzer("resources/in/cv_sample/", "Sample", 1, "resources/config/LSA/lemonde_fr",
				"resources/config/LDA/lemonde_fr", Lang.getLang("French"), false, false, 0.3);
		cvAnalyzerSample.process();*/

		CVAnalyzer cvAnalyzerPositifs = new CVAnalyzer("resources/in/cv_positifs/", "Positif", 1, "resources/config/LSA/lemonde_fr",
				"resources/config/LDA/lemonde_fr", Lang.getLang("French"), false, false, 0.3);
		cvAnalyzerPositifs.process();

		CVAnalyzer cvAnalyzerNegatifs = new CVAnalyzer("resources/in/cv_negatifs/", "Negatif", 0, "resources/config/LSA/lemonde_fr",
				"resources/config/LDA/lemonde_fr", Lang.getLang("French"), false, false, 0.3);
		cvAnalyzerNegatifs.process();
	}

}
