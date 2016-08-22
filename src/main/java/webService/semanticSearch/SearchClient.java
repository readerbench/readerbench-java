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
package webService.semanticSearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import services.commons.Formatting;
import services.semanticSearch.SemanticSearch;
import services.semanticSearch.SemanticSearchResult;
import webService.ReaderBenchServer;
import webService.result.ResultSearch;

public class SearchClient {

	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);

	public static final double MIN_THRESHOLD = 0.2d;
	public static final int NO_RESULTS = 20;

	/**
	 * Search for query in documents
	 *
	 * @param documents
	 * @param query
	 * @return List of urls for results
	 */
	public static List<ResultSearch> search(String query, List<AbstractDocument> documents, int maxContentSize) {
		logger.info("Processign query:" + query);
		AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
		BlockTemplate block = contents.new BlockTemplate();
		block.setId(0);
		block.setContent(query);
		contents.getBlocks().add(block);

		AbstractDocument queryDoc = new Document(null, contents, documents.get(0).getLSA(), documents.get(0).getLDA(),
				documents.get(0).getLanguage(), false, false);
		queryDoc.computeAll(true, null, null);
		queryDoc.setTitleText(query);

		List<SemanticSearchResult> results = SemanticSearch.search(queryDoc, documents, MIN_THRESHOLD, NO_RESULTS);
		List<ResultSearch> searchResults = new ArrayList<ResultSearch>();
		for (SemanticSearchResult r : results) {
			String content = r.getDoc().getText();
			if (content.length() > maxContentSize) {
				content = content.substring(0, maxContentSize);
				if (content.lastIndexOf(" ") != -1)
					content = content.substring(0, content.lastIndexOf(" "));
				content += "...";
			}
			searchResults
					.add(new ResultSearch(r.getDoc().getPath(), content, Formatting.formatNumber(r.getRelevance())));
		}

		return searchResults;
	}

	private static void performQuery(String query, int port) {
		try {
			int mcs = 50;
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(
					"http://localhost:" + port + "/search?q=" + URLEncoder.encode(query, "UTF-8") + "&mcs=" + mcs);
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("\n\n");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		performQuery("money", 5656);
		performQuery("financial", 5858);
	}

}
