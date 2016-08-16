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

	public static AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, Lang lang,
			boolean posTagging, boolean computeDialogism) {
		
		logger.info("Processign query ...");
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(query);

		// Lang lang = Lang.eng;
		AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA(pathToLSA, lang),
				LDA.loadLDA(pathToLDA, lang), lang, posTagging, false);
		logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
		queryDoc.computeAll(computeDialogism, null, null);
		ComplexityIndices.computeComplexityFactors(queryDoc);

		return queryDoc;
	}
	
}
