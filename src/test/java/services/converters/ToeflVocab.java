package services.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.ageOfExposure.TopicMatchGraph;
import services.complexity.ComplexityIndices;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class ToeflVocab {
	
	static Logger logger = Logger.getLogger(PdfToTextFrenchCVs.class);
	
	AbstractDocument queryQuestion;
	AbstractDocument left1, left2, left3, left4, left5, left6;
	AbstractDocument aAnswer, bAnswer, cAnswer;
	String question = "";
	String oldQuestion = "";
	int questionId;
	
	@Test
	public void process() {
		
		//String prependPath = "/Users/Berilac/Projects/Eclipse/readerbench/resources/";
		String prependPath = "/Users/Berilac/OneDrive/ReaderBench/";
		logger.info("Starting toefl vocabulary tests processing...");
		String lsaPath = "resources/config/LSA/tasa_en";
		String ldaPath = "resources/config/LDA/tasa_en";
		
		LDA lda = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		
		try {
			Files.walk(Paths.get(prependPath + "vocabulary_test/2001")).forEach(filePath -> {
				questionId = 1;
				StringBuilder sb = new StringBuilder();
				sb.append("sep=,\nid,a,b,c\n");
				
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
			    		if (line.contains("1. ")) {
			    			left1 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			//SemanticCohesion sc = new SemanticCohesion(queryQuestion, aAnswer);
			    			//sb.append("a," + Formatting.formatNumber(sc.getCohesion()) + ",");
			    		}
			    		else if (line.contains("2. ")) {
			    			left2 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("3. ")) {
			    			left3 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("4. ")) {
			    			left4 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("5. ")) {
			    			left5 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("6. ")) {
			    			left6 = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("a. ")) {
			    			aAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("b. ")) {
			    			bAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    		}
			    		else if (line.contains("c. ")) {
			    			cAnswer = processQuery(
			    				line,
			    				lsaPath,
			    				ldaPath,
			    				"eng",
			    				true);
			    			
			    			// process current question
			    			TopicMatchGraph graph = new TopicMatchGraph(9);
			    			for (int i = 0; i < 6; i++) {
			    				for (int j = 0; j < 3; j++) {			    					
			    					switch(i) {
			    					case 0:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left1, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left1, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left1, cAnswer));
			    							break;
			    						}
			    						break;
			    					case 1:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left2, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left2, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left2, cAnswer));
			    							break;
			    						}
			    						break;
			    					case 2:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left3, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left3, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left3, cAnswer));
			    							break;
			    						}
			    						break;
			    					case 3:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left4, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left4, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left4, cAnswer));
			    							break;
			    						}
			    						break;
			    					case 4:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left5, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left5, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left5, cAnswer));
			    							break;
			    						}
			    						break;
			    					case 5:
			    						switch(j) {
			    						case 0:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left6, aAnswer));
			    							break;
			    						case 1:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left6, bAnswer));
			    							break;
			    						case 2:
			    							graph.addEdge(j, i + 3, lda.getSimilarity(left6, cAnswer));
			    							break;
			    						}
			    						break;
			    					}
			    				}
			    			}
			    			
			    			logger.info("Graph builiding finished");
			    			logger.info(graph);
			    			
			    			logger.info("Cost");
			    			
			    			logger.info("Edge");
			    			
			    			Integer[] assoc = graph.computeAssociations();
			    			logger.info("Printing associations");
			    			sb.append(questionId + ",");
			    			questionId += 1;
			    			for (int j = 0; j < assoc.length; j++) {
			    				logger.info("Association of element " + j + " = " + assoc[j]);
			    				sb.append(assoc[j] - 3 + 1 + ",");
			    			}
			    			
			    			sb.append("\n");
			    			
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
		
		logger.info("Toefl vocabulary questions processing ended...");
		
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
