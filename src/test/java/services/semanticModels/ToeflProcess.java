package services.semanticModels;

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
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class ToeflProcess {

	static Logger logger = Logger.getLogger(ToeflProcess.class);

	AbstractDocument queryQuestion;
	AbstractDocument aAnswer, bAnswer, cAnswer, dAnswer;
	String question = "";
	String oldQuestion = "";

	@Test
	public void process(String path, ISemanticModel semModel) {
		logger.info("Starting toefl tests processing...");

		final LSA lsa;
		final LDA lda;
		if (semModel instanceof LSA) {
			lsa = (LSA) semModel;
			lda = null;
		} else if (semModel instanceof LDA) {
			lsa = null;
			lda = (LDA) semModel;
		} else {
			logger.error("Inappropriate semantic model used for assessment: " + semModel.getPath());
			return;
		}

		try {
			Files.walk(Paths.get(path)).forEach(filePath -> {
				StringBuilder sb = new StringBuilder();
				sb.append("sep=,\nq,a,%,b,%,c,%,d,%\n");

				String filePathString = filePath.toString();
				if (filePathString.contains(".txt")) {
					logger.info("Processing file: " + filePathString);

					List<String> list = new ArrayList<>();

					try (Stream<String> filteredLines = Files.lines(filePath)
							// test if file is closed or not
							.onClose(() -> System.out.println("File closed"))) {
						list = filteredLines.collect(Collectors.toList());
					} catch (IOException e) {
						e.printStackTrace();
					}

					logger.info("File has " + list.size() + " lines");

					list.forEach((line) -> {
						logger.info("Processing line " + line);
						if (line.contains("a. ")) {
							aAnswer = VocabularyTest.processDoc(line, lsa, lda, semModel.getLanguage());
							SemanticCohesion sc = new SemanticCohesion(queryQuestion, aAnswer);
							sb.append("a," + Formatting.formatNumber(sc.getCohesion()) + ",");
						} else if (line.contains("b. ")) {
							bAnswer = VocabularyTest.processDoc(line, lsa, lda, semModel.getLanguage());
							SemanticCohesion sc = new SemanticCohesion(queryQuestion, bAnswer);
							sb.append("b," + Formatting.formatNumber(sc.getCohesion()) + ",");
						} else if (line.contains("c. ")) {
							cAnswer = VocabularyTest.processDoc(line, lsa, lda, semModel.getLanguage());
							SemanticCohesion sc = new SemanticCohesion(queryQuestion, cAnswer);
							sb.append("c," + Formatting.formatNumber(sc.getCohesion()) + ",");
						} else if (line.contains("d. ")) {
							dAnswer = VocabularyTest.processDoc(line, lsa, lda, semModel.getLanguage());
							SemanticCohesion sc = new SemanticCohesion(queryQuestion, dAnswer);
							sb.append("d," + Formatting.formatNumber(sc.getCohesion()) + "\n");
						} else {
							question = line;
							queryQuestion = VocabularyTest.processDoc(line, lsa, lda, semModel.getLanguage());

							String[] parts = line.split("\\.");
							sb.append(parts[0] + ",");
						}
					});

					logger.info("Finished processing file: " + filePathString);

					File file = new File(filePathString.replace(".txt", ".csv"));
					try {
						FileUtils.writeStringToFile(file, sb.toString());
					} catch (Exception e) {
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
