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
import data.sentiment.SentimentValence;
import services.commons.Formatting;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndexType;
import services.converters.PdfToTextConverter;
import webService.ReaderBenchServer;
import webService.cv.CVHelper;
import webService.query.QueryHelper;
import webService.result.ResultCv;
import webService.result.ResultKeyword;
import webService.result.ResultTextualComplexity;
import webService.result.ResultValence;
import webService.services.TextualComplexity;

public class CVAnalyzer {

	public Logger logger = Logger.getLogger(CVAnalyzer.class);
	private String path;
	
	private String pathToLSA;
	private String pathToLDA;
	private Lang lang;
	private boolean usePOSTagging = false;
	private boolean computeDialogism = false;
	private double threshold = 0.3;
	private double FANthreshold = 5;
	private double FANdelta = 1;

	private String keywords = "prospection, prospect, développement, clients, fidélisation, chiffre d’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";
	private String ignore = "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre";

	public CVAnalyzer(String path, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean computeDialogism, double threshold) {
		this.path = path;
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
					"CV,pages,images,avg images per page,colors,avg colors per page,paragraphs,avg paragraphs per page,sentences,avg sentences per page,words,avg words per page,content words,avg content words per page," + 
					"positive words (FAN >= " + (FANthreshold + FANdelta) + "),pos words percentage," + 
					"negative words (FAN <= " + (FANthreshold - FANdelta) + "),neg words percentage," + 
					"neutral words (FAN > " + (FANthreshold - FANdelta) + " & FAN < " + (FANthreshold + FANdelta) + "),neutral words percentage," + 
					"FAN weighted average," 
			);
			//affect,positive emotion,negative emotion,anxiety,anger,sadness,");
			
			List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
			for(SentimentValence svLiwc : sentimentValences) {
				if (svLiwc.getName().contains("LIWC") && svLiwc != null) {
					sb.append(svLiwc.getName() + ",");
					sb.append(svLiwc.getName() + " percentage,");
				}
			}
			
			// textual complexity factors
			TextualComplexity textualComplexity = new TextualComplexity(lang, usePOSTagging, computeDialogism);
			for (ComplexityIndexType cat : textualComplexity.getList()) {
				for (ComplexityIndex index : cat.getFactory().build(lang))
					sb.append(index.getAcronym() + ',');
			}
			sb.append("keywords document relevance,");
			// keywords
			sb.append(
					"prospection_sim,prospection_no,prospect_sim,prospect_no,développement_sim,développement_no,clients_sim,clients_no,fidélisation_sim,fidélisation_no,chiffre d’affaires_sim,chiffre d’affaires_no,marge_sim,marge_no,vente_sim,vente_no,portefeuille_sim,portefeuille_no,négociation_sim,négociation_no,budget_sim,budget_no,rendez-vous_sim,rendez-vous_no,proposition_sim,proposition_no,terrain_sim,terrain_no,téléphone_sim,téléphone_no,rentabilité_sim,rentabilité_no,business_sim,business_no,reporting_sim,reporting_no,veille_sim,veille_no,secteur_sim,secteur_no,objectifs_sim,objectifs_no,comptes_sim,comptes_no,animation_sim,animation_no,suivi_sim,suivi_no,création_sim,création_no,gestion_sim,gestion_no,");
			// concepts
			/*for (int i = 0; i < 25; i++) {
				sb.append("concept" + i + ',');
				sb.append("rel" + i + ',');
			}*/
			sb.append("\n");
			
			//test
			Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));
			Set<String> ignoreList = new HashSet<>(Arrays.asList(ignore.split(",[ ]*")));

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

					logger.info("Lista de ignore contine " + ignore);
					ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, ignoreList,
							pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism, threshold, FANthreshold, FANdelta);
					
					// CV
					sb.append(filePath.getFileName().toString() + ",");

					// pages
					sb.append(result.getPages());
					sb.append(',');
					
					// images
					sb.append(result.getImages());
					sb.append(',');
					
					// average images per page
					sb.append(Formatting.formatNumber(result.getImages() * 1.0 / result.getPages()));
					sb.append(',');

					// colors
					sb.append(result.getColors());
					sb.append(',');
					
					// average colors per page
					sb.append(Formatting.formatNumber(result.getColors() * 1.0 / result.getPages()));
					sb.append(',');					

					// paragraphs
					sb.append(result.getParagraphs());
					sb.append(',');
					
					// avg paragraphs per page
					sb.append(Formatting.formatNumber(result.getParagraphs() * 1.0 / result.getPages()));
					sb.append(',');

					// sentences
					sb.append(result.getSentences());
					sb.append(',');
					
					// avg sentences per page
					sb.append(Formatting.formatNumber(result.getSentences() * 1.0 / result.getPages()));
					sb.append(',');

					// words
					sb.append(result.getWords());
					sb.append(',');
					
					// avg words per page
					sb.append(Formatting.formatNumber(result.getWords() * 1.0 / result.getPages()));
					sb.append(',');

					// content words
					sb.append(result.getContentWords());
					sb.append(',');
					
					// avg content words per page
					sb.append(Formatting.formatNumber(result.getContentWords() * 1.0 / result.getPages()));
					sb.append(',');

					// positive words
					sb.append(result.getPositiveWords().size());
					sb.append(',');
					
					// positive words norm.
					sb.append(Formatting.formatNumber(result.getPositiveWords().size() * 1.0 / result.getWords()));
					sb.append(',');

					// negative words
					sb.append(result.getNegativeWords().size());
					sb.append(',');
					
					// negative words norm.
					sb.append(Formatting.formatNumber(result.getNegativeWords().size() * 1.0 / result.getWords()));
					sb.append(',');
					
					// neutral words
					sb.append(result.getNeutralWords().size());
					sb.append(',');
					
					// neutral words norm.
					sb.append(Formatting.formatNumber(result.getNeutralWords().size() * 1.0 / result.getWords()));
					sb.append(',');
					
					// FAN weighted average
					sb.append(Formatting.formatNumber((result.getFanWeightedAverage())));
					sb.append(',');
					
					// LIWC emotions
					for (Map.Entry<String, List<String>> entry : result.getLiwcEmotions().entrySet()) {
						// String emotion = entry.getKey();
						// sb.append(emotion);
						sb.append(entry.getValue().size());
						sb.append(',');
						sb.append(Formatting.formatNumber(entry.getValue().size() * 1.0 / result.getWords()));
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
					/*ResultTopic resultTopic = result.getConcepts();
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
					}*/

					sb.append("\n");

				}
			});

			File file = new File(path + "stats.csv");
			try {
				FileUtils.writeStringToFile(file, sb.toString(), "UTF-8");
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

		CVAnalyzer cvAnalyzerSample = new CVAnalyzer("resources/in/cv/cv_sample/", "resources/config/LSA/lemonde_fr",
				"resources/config/LDA/lemonde_fr", Lang.getLang("French"), false, false, 0.3);
		cvAnalyzerSample.process();

		/*CVAnalyzer cvAnalyzerPositifs = new CVAnalyzer("resources/in/cv/", "resources/config/LSA/lemonde_fr",
				"resources/config/LDA/lemonde_fr", Lang.getLang("French"), false, false, 0.3);
		cvAnalyzerPositifs.process();*/
	}

}
