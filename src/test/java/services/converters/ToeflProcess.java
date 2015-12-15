package services.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.hdf.extractor.data.LST;
import org.apache.poi.hssf.util.HSSFColor.AQUA;
import org.junit.Test;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Word;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import data.document.Document;
import data.sentiment.SentimentGrid;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.topicMining.TopicModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class ToeflProcess {
	
	static Logger logger = Logger.getLogger(PdfToTextFrenchCVs.class);
	
	AbstractDocument queryQuestion;
	AbstractDocument aAnswer, bAnswer, cAnswer, dAnswer;
	String question = "";
	String oldQuestion = "";
	
	@Test
	public void process() {
		
		//String prependPath = "/Users/Berilac/Projects/Eclipse/readerbench/resources/";
		String prependPath = "/Users/Berilac/OneDrive/ReaderBench/";
		logger.info("Starting toefl tests processing...");
		String lsaPath = "resources/config/LSA/tasa_en";
		String ldaPath = "resources/config/LDA/tasa_en";
		
		try {
			Files.walk(Paths.get(prependPath + "toefl")).forEach(filePath -> {
				StringBuilder sb = new StringBuilder();
				sb.append("sep=,\nq,a,%,b,%,c,%,d,%\n");
				
				
				String filePathString = filePath.toString();
			    if (filePathString.contains(".txt")) {
			        
			    	logger.info("Processing file: " + filePathString);
			    	
			    	List<String> list = new ArrayList<>();

			    	try(Stream<String> filteredLines = Files.lines(filePath)
                            //test if file is closed or not
                            .onClose(() -> System.out.println("File closed")))
			    	{
			    		list = filteredLines.collect(Collectors.toList());
					} catch (IOException e) {
						e.printStackTrace();
					}
			    	
			    	logger.info("File has " + list.size() + " lines");
		    		
			    	list.forEach((line) -> {
			    		logger.info("Processing line " + line);
			    		if (line.contains("a. ")) {
			    			aAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			SemanticCohesion sc = new SemanticCohesion(queryQuestion, aAnswer);
			    			sb.append("a," + Formatting.formatNumber(sc.getCohesion()) + ",");
			    		}
			    		else if (line.contains("b. ")) {
			    			bAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			SemanticCohesion sc = new SemanticCohesion(queryQuestion, bAnswer);
			    			sb.append("b," + Formatting.formatNumber(sc.getCohesion()) + ",");
			    		}
			    		else if (line.contains("c. ")) {
			    			cAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			SemanticCohesion sc = new SemanticCohesion(queryQuestion, cAnswer);
			    			sb.append("c," + Formatting.formatNumber(sc.getCohesion()) + ",");
			    		}
			    		else if (line.contains("d. ")) {
			    			dAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			SemanticCohesion sc = new SemanticCohesion(queryQuestion, dAnswer);
			    			sb.append("d," + Formatting.formatNumber(sc.getCohesion()) + "\n");
			    		}
			    		else {
			    			question = line;
			    			queryQuestion = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			
			    			String[] parts = line.split("\\.");
			    			sb.append(parts[0] + ",");
			    		}
			    	});
			    	
			        logger.info("Finished processing file: " + filePathString);

			        File file = new File(filePathString.replace(".txt", ".csv"));
					try {
						FileUtils.writeStringToFile(file,sb.toString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logger.info("Printed information to: " + file.getAbsolutePath());
					
			    }
			});
			
		} catch (IOException e) {
			logger.info("Error opening path.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Toefl questions processing ended...");
		
	}
	
	private List<ResultNode> getTopics(String query, String pathToLSA, String pathToLDA, String lang, boolean posTagging,
			double threshold) {

		List<ResultNode> nodes = new ArrayList<ResultNode>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);

		List<Topic> topics = TopicModeling.getSublist(queryDoc.getTopics(), 50,
				false, false);

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

		int i = 0;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord())) {
				nodes.add(new ResultNode(t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance())));
			}
		}

		return nodes;
	}
	
	public AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, String language,
			boolean posTagging) {
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
		queryDoc.computeAll(null, null);
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
