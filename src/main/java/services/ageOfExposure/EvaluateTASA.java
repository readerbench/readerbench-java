package services.ageOfExposure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.log4j.BasicConfigurator;

import services.converters.GenericTasaDocument;
import services.converters.SplitTASA;
import data.AbstractDocument;
import data.Word;
import data.document.Document;
import data.Lang;

public class EvaluateTASA {
	private static Logger logger = Logger.getLogger("EvaluateTASA");

	private int noIndexes;
	private Map<Word, double[]> wordWCIndexesValues;
	private Map<Integer, String> WCIndexesNames;

	public EvaluateTASA(String path) {
		wordWCIndexesValues = new TreeMap<Word, double[]>();
		WCIndexesNames = new TreeMap<Integer, String>();
		getWordComplexityIndices(path);
	}

	private double getDocWordComplexity(AbstractDocument d, int index) {
		double distanceSum = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			if (wordWCIndexesValues.containsKey(e.getKey())) {
				double[] indices = wordWCIndexesValues.get(e.getKey());
				if (index < indices.length) {
					distanceSum += indices[index] * e.getValue();
					totalWords += e.getValue();
				}
			}
		}
		return (totalWords > 0 ? distanceSum / totalWords : 0);
	}

	private void getWordComplexityIndices(String path) {
		logger.info("Loading file " + path + "...");

		/* Compute the AgeOfAcquisition Dictionary */
		String tokens[];
		String line;
		String word;
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			// read first line
			if ((line = br.readLine()) != null) {
				tokens = line.split(",");
				word = tokens[0].trim();
				noIndexes = tokens.length - 1;
				for (int i = 1; i < tokens.length; i++)
					WCIndexesNames.put(i - 1, tokens[i]);
			}
			// read word indexes
			while ((line = br.readLine()) != null) {
				tokens = line.split(",");
				if (tokens.length > 1) {
					word = tokens[0].trim().replaceAll(" ", "");

					double[] values = new double[noIndexes];
					for (int i = 1; i < tokens.length; i++)
						if (tokens[i] != null && tokens[i].length() > 0)
							values[i - 1] = Double.parseDouble(tokens[i]);

					wordWCIndexesValues.put(
							Word.getWordFromConcept(word, Lang.eng), values);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processWordComplexity(String input, String path,
			boolean usePOStagging) throws FileNotFoundException, IOException {
		List<GenericTasaDocument> docs = SplitTASA.getTASAdocs(input, path);

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(path + "/measurementsWC.csv")),
				"UTF-8"), 32768);

		// print header
		out.write("Filename,Grade,DRP,Genre");
		for (int i = 0; i < noIndexes; i++) {
			out.write("," + WCIndexesNames.get(i));
		}
		out.write("\n");

		int noProcessed = 0;

		// create save folders
//		String saveLocation = path + "/files";
//		File dir = new File(saveLocation);
//		if (!dir.exists()) {
//			dir.mkdir();
//		}
//		// see if there are folders with genre names
//		for (String g : SplitTASA.TASA_GENRES) {
//			dir = new File(saveLocation + "/" + g);
//			if (dir.exists()) {
//				for (File f : dir.listFiles())
//					f.delete();
//				dir.delete();
//			}
//			dir.mkdir();
//		}

		for (GenericTasaDocument doc : docs) {
			// save files
//			doc.writeTxt(saveLocation);

			Document d = doc.getDocument(usePOStagging);
			out.write(doc.getID()
					+ ","
					+ GenericTasaDocument.get13GradeLevel(doc
							.getDRPscore()) + "," + doc.getDRPscore() + ","
					+ doc.getGenre());
			for (int i = 0; i < noIndexes; i++) {
				out.write("," + getDocWordComplexity(d, i));
			}
			out.write("\n");
			if ((++noProcessed) % 1000 == 0) {
				logger.info("Finished processing " + noProcessed
						+ " TASA documents");
			}
		}

		out.close();
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		try {
			EvaluateTASA eval = new EvaluateTASA("in/word complexity/words.csv");
			eval.processWordComplexity("tasa.txt", "in/word complexity", false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
