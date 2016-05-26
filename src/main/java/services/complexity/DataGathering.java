package services.complexity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.AbstractDocument.SaveType;
import data.Lang;
import data.complexity.Measurement;
import data.document.Document;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class DataGathering {
	static Logger logger = Logger.getLogger(DataGathering.class);

	public static final int MAX_PROCESSED_FILES = 10000;

	public static void writeHeader(String path) {
		// create measurements.csv header
		try {
			FileWriter fstream = new FileWriter(path + "/measurements.csv", false);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Grade Level,File name,Genre,Complexity,Paragraphs,Sentences,Words,Content words");
			for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++)
				out.write("," + ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[i]);
			out.close();
		} catch (Exception e) {
			logger.error("Runtime error while initializing measurements.csv file");
			e.printStackTrace();
		}
	}

	public static void processTexts(String path, int gradeLevel, boolean writeHeader, LSA lsa, LDA lda, Lang lang,
			boolean usePOSTagging, boolean computeDialogism) throws IOException {
		File dir = new File(path);

		if (!dir.exists()) {
			throw new IOException("Inexistent Folder: " + dir.getPath());
		}

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(".xml"))
					return true;
				return false;
			}
		});

		if (writeHeader) {
			writeHeader(dir.getPath());
		}

		int noProcessedFiles = 0;
		for (File file : files) {
			logger.info("Processing " + file.getName() + " file");
			// Create file

			Document d = null;
			try {
				d = Document.load(file, lsa, lda, lang, usePOSTagging, true);
				d.computeAll(computeDialogism, null, null, SaveType.NONE);
				ComplexityIndices.computeComplexityFactors(d);
			} catch (Exception e) {
				logger.error("Runtime error while processing " + file.getName() + ": " + e.getMessage());
				e.printStackTrace();
			}

			if (d != null) {
				try {
					FileWriter fstream = new FileWriter(dir.getPath() + "/measurements.csv", true);
					StringBuffer concat = new StringBuffer();
					BufferedWriter out = new BufferedWriter(fstream);

					concat.append("\n").append(gradeLevel).append(",").append(file.getName().replaceAll(",", ""))
							.append(",").append((d.getGenre() != null ? d.getGenre().trim() : "")).append(",")
							.append((d.getComplexityLevel() != null ? d.getComplexityLevel().trim() : ""));
					concat.append(",").append(d.getNoBlocks());
					concat.append(",").append(d.getNoSentences());
					concat.append(",").append(d.getNoWords());
					concat.append(",").append(d.getNoContentWords());
					for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++)
						concat.append(",").append(d.getComplexityIndices()[i]);
					System.out.println(concat);
					out.write(concat.toString());
					out.close();
				} catch (IOException e) {
					logger.error("Runtime error while initializing measurements.csv file");
					e.printStackTrace();
					throw e;
				}
			}

			noProcessedFiles++;

			if (noProcessedFiles >= MAX_PROCESSED_FILES)
				break;
		}
	}

	public static Map<Double, List<Measurement>> getMeasurements(String fileName) {
		Map<Double, List<Measurement>> result = new TreeMap<Double, List<Measurement>>();

		try {
			BufferedReader input = new BufferedReader(new FileReader(fileName));
			try {
				// disregard first line
				String line = input.readLine();
				while ((line = input.readLine()) != null) {
					String[] fields = line.split("[;,]");
					double[] values = new double[fields.length - 4];

					double classNumber = Double.parseDouble(fields[0]);
					for (int i = 4; i < fields.length; i++) {
						values[i - 4] = Double.parseDouble(fields[i]);
					}
					if (!result.containsKey(classNumber))
						result.put(classNumber, new ArrayList<Measurement>());
					result.get(classNumber).add(new Measurement(classNumber, values));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
