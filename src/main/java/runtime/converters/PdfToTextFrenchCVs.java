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
package runtime.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import data.document.Document;
import data.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.converters.PdfToTextConverter;
import services.discourse.topicMining.TopicModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class PdfToTextFrenchCVs {

	static Logger logger = Logger.getLogger(PdfToTextFrenchCVs.class);

	@Test
	public void process() {

		// String prependPath =
		// "/Users/Berilac/Projects/Eclipse/readerbench/resources/";
		String prependPath = "/Users/Berilac/OneDrive/ReaderBench/";
		logger.info("Starting French CVs processing...");
		StringBuilder sb = new StringBuilder();
		sb.append("sep=\t\nfile\tconcepts\n");

		try {
			Files.walk(Paths.get(prependPath + "cv")).forEach(filePath -> {
				String filePathString = filePath.toString();
				if (filePathString.contains(".pdf")) {

					logger.info("Processing file: " + filePathString);

					// read PDF file contents
					PdfToTextConverter pdfConverter = new PdfToTextConverter();
					String documentContent = pdfConverter.pdftoText(filePathString, true);

					// process file
					List<ResultNode> nodes = getTopics(documentContent, "resources/config/LSA/lemonde_fr",
							"resources/config/LDA/lemonde_fr", "fr", false, false, 0.3);

					StringBuilder sbNode = new StringBuilder();
					for (ResultNode node : nodes) {
						sbNode.append(node.name + " (" + node.value + "), ");
					}
					// delete last comma
					if (sbNode.length() > 2)
						sbNode.setLength(sbNode.length() - 2);
					sbNode.append("\n");

					sb.append(filePath.getFileName().toString() + "\t" + sbNode.toString());

					logger.info("Finished processing file: " + filePathString);

				}
			});

			// System.out.println(sb.toString());
			File file = new File("frenchcvs.csv");
			FileUtils.writeStringToFile(file, sb.toString());
			logger.info("Printed information to: " + file.getAbsolutePath());

		} catch (IOException e) {
			logger.info("Error opening path.");
			e.printStackTrace();
		}

		logger.info("French CVs processing ended...");

	}

	private List<ResultNode> getTopics(String query, String pathToLSA, String pathToLDA, String lang,
			boolean posTagging, boolean computeDialogism, double threshold) {

		List<ResultNode> nodes = new ArrayList<ResultNode>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging, computeDialogism);

		List<Topic> topics = TopicModeling.getSublist(queryDoc.getTopics(), 50, false, false);

		// build connected graph
		Map<Word, Boolean> visibleConcepts = new TreeMap<Word, Boolean>();

		for (Topic t : topics) {
			visibleConcepts.put(t.getWord(), false);
		}

		// determine similarities
		for (Word w1 : visibleConcepts.keySet()) {
			for (Word w2 : visibleConcepts.keySet()) {
				double lsaSim = 0;
				double ldaSim = 0;
				if (queryDoc.getLSA() != null)
					lsaSim = queryDoc.getLSA().getSimilarity(w1, w2);
				if (queryDoc.getLDA() != null)
					ldaSim = queryDoc.getLDA().getSimilarity(w1, w2);
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				if (!w1.equals(w2) && sim >= threshold) {
					visibleConcepts.put(w1, true);
					visibleConcepts.put(w2, true);
				}
			}
		}

		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord())) {
				nodes.add(new ResultNode(t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance())));
			}
		}

		return nodes;
	}

	public AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, String language,
			boolean posTagging, boolean computeDialogism) {
		logger.info("Processign query ...");
		AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
		String[] blocks = query.split("\n");
		logger.info("[Processing] There should be " + blocks.length + " blocks in the document");
		for (int i = 0; i < blocks.length; i++) {
			BlockTemplate block = contents.new BlockTemplate();
			block.setId(i);
			block.setContent(blocks[i]);
			contents.getBlocks().add(block);
		}

		// Lang lang = Lang.eng;
		Lang lang = Lang.getLang(language);
		AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA(pathToLSA, lang),
				LDA.loadLDA(pathToLDA, lang), lang, posTagging, false);
		logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
		queryDoc.computeAll(computeDialogism, null, null);
		ComplexityIndices.computeComplexityFactors(queryDoc);

		return queryDoc;
	}

	class ResultNode implements Comparable<ResultNode> {

		private String name;
		private double value;

		public ResultNode(String name, double value) {
			super();
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getValue() {
			return value;
		}

		@Override
		public int compareTo(ResultNode o) {
			return (int) Math.signum(o.getValue() - this.getValue());
		}
	}

}
