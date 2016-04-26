package webService.query;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import services.complexity.ComplexityIndices;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class QueryHelper {
	
	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);

	public static AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, String language,
			boolean posTagging, boolean computeDialogism) {
		
		logger.info("Processign query ...");
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(query);

		// Lang lang = Lang.eng;
		Lang lang = Lang.getLang(language);
		AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA(pathToLSA, lang),
				LDA.loadLDA(pathToLDA, lang), lang, posTagging, false);
		logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
		queryDoc.computeAll(computeDialogism, null, null);
		ComplexityIndices.computeComplexityFactors(queryDoc);

		return queryDoc;
	}
	
}
