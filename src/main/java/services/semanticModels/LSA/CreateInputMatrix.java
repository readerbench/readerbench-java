package services.semanticModels.LSA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

import DAO.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class CreateInputMatrix extends LSA {
	private static Logger logger = Logger.getLogger(CreateInputMatrix.class);
	private int noWords;
	private int noDocuments;

	public void parseCorpus(String path, String inputFileName,
			String outputFileName, Lang lang) throws FileNotFoundException,
			IOException {
		logger.info("Parsing input file...");
		setWords(new DualTreeBidiMap<Word, Integer>());
		setMapIdf(new TreeMap<Word, Double>());
		noDocuments = 0;
		noWords = 0;

		// determine number of documents and create dictionary
		FileInputStream inputFile = new FileInputStream(path + "/"
				+ inputFileName);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";
		while ((line = in.readLine()) != null) {
			if (line.length() > LOWER_BOUND) {
				StringTokenizer st = new StringTokenizer(line, " .");
				boolean wasIdentified = false;
				while (st.hasMoreTokens()) {
					Word w = Word.getWordFromConcept(st.nextToken(), lang);
					if (!getWords().containsKey(w)) {
						getWords().put(w, noWords);
						getMapIdf().put(w, 1d);
						wasIdentified = true;
						noWords++;
					} else {
						if (!wasIdentified) {
							getMapIdf().put(w, getMapIdf().get(w) + 1);
							wasIdentified = true;
						}
					}
				}
				noDocuments++;
			}
		}
		in.close();

		// update IDfs as |D|/|Dw|
		logger.info("Updating IDfs");
		for (Word concept : getMapIdf().keySet())
			getMapIdf().put(concept,
					((double) (noDocuments)) / (getMapIdf().get(concept) + 1));

		// prepare output matrix
		final Configuration conf = new Configuration();

		SequenceFile.Writer writer = SequenceFile.createWriter(conf,
				Writer.file(new Path(path + "/" + outputFileName)),
				Writer.keyClass(IntWritable.class),
				Writer.valueClass(VectorWritable.class));

		Writer.compression(CompressionType.BLOCK);
		final VectorWritable value = new VectorWritable();
		final IntWritable key = new IntWritable();

		// read the corpus
		inputFile = new FileInputStream(path + "/" + inputFileName);
		ir = new InputStreamReader(inputFile, "UTF-8");
		in = new BufferedReader(ir);
		int crtDoc = 0;

		// double[][] termDoc = new double[noWords][];
		Map<Integer, Vector> termDocVectors = new TreeMap<Integer, Vector>();

		// prepare individual word vectors
		for (int i = 0; i < noWords; i++) {
			// termDoc[i] = new double[noDocuments];
			termDocVectors
					.put(i, new SequentialAccessSparseVector(noDocuments));
		}

		logger.info("Building term-doc matrix ...");
		while ((line = in.readLine()) != null) {
			if (line.length() > LOWER_BOUND) {
				StringTokenizer st = new StringTokenizer(line, " .");

				Map<Word, Integer> wordOccurrences = new TreeMap<Word, Integer>();
				while (st.hasMoreTokens()) {
					Word w = Word.getWordFromConcept(st.nextToken(), lang);
					// termDoc[getWords().get(w)][crtDoc]++;
					if (wordOccurrences.containsKey(w))
						wordOccurrences.put(w, wordOccurrences.get(w) + 1);
					else {
						wordOccurrences.put(w, 1);
					}
				}
				for (Entry<Word, Integer> entry : wordOccurrences.entrySet()) {
					int wordIndex = getWords().get(entry.getKey());
					// double idf = Math.log(getMapIdf().get(entry.getKey()));
					// if (idf < 0) {
					// logger.error("Incorrect idf for word "
					// + entry.getValue());
					// }
					// double tfIdf = (Math.log(1 + entry.getValue())) * idf;
					// termDocVectors.get(wordIndex).set(crtDoc, tfIdf);
					termDocVectors.get(wordIndex).set(crtDoc, entry.getValue());
				}
				crtDoc++;
			}
		}

		// normalize via entropy
		logger.info("Normalizaling term-doc matrix by applying log-entropy ...");
		for (Entry<Integer, Vector> entry : termDocVectors.entrySet()) {
			double gf = 0;
			Vector v = entry.getValue();

			for (Element el : v.nonZeroes()) {
				gf += el.get();

			}

			double entropy = 0;
			for (Element el : v.nonZeroes()) {
				double p = el.get() / gf;
				entropy += p * Math.log(p);
			}
			entropy = 1 + (entropy / Math.log(noDocuments));

			// update termDoc matrix to be written
			for (Element el : v.nonZeroes()) {
				double log = Math.log(1 + el.get());
				v.set(el.index(), log * entropy);
			}

			key.set(entry.getKey());
			value.set(v);
			writer.append(key, value); // write the vector
		}

		in.close();
		writer.close();

		logger.info("Vector space dimensions:\n" + noWords + " words with "
				+ noDocuments + " documents");

		saveIdf(path);
		saveWordList(path);

		logger.info("Finished all computations");
	}

	private void saveWordList(String path) throws FileNotFoundException,
			IOException {
		// loads IDf matrix from file
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(path + "/wordlist.txt"), "UTF-8"));
		for (Word w : getWords().keySet()) {
			out.write(w.getExtendedLemma() + " " + getWords().get(w) + "\n");
		}
		out.close();
	}

	private void saveIdf(String path) throws FileNotFoundException, IOException {
		// loads IDf matrix from file
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(path + "/idf.txt"), "UTF-8"));
		for (Word w : getMapIdf().keySet()) {
			out.write(w.getExtendedLemma() + " " + getMapIdf().get(w) + "\n");
		}
		out.close();
	}

	public int getNoWords() {
		return noWords;
	}

	public void setNoWords(int noWords) {
		this.noWords = noWords;
	}

	public int getNoDocuments() {
		return noDocuments;
	}

	public void setNoDocuments(int noDocuments) {
		this.noDocuments = noDocuments;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		try {
			CreateInputMatrix lsaTraining = new CreateInputMatrix();
			lsaTraining.parseCorpus("config/LSA/tasa_new_en2", "tasa.txt",
					"matrix.svd", Lang.eng);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error during learning process");
		}
	}
}
