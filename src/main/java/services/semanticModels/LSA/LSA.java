package services.semanticModels.LSA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import DAO.AnalysisElement;
import DAO.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.ObjectManipulation;
import services.commons.VectorAlgebra;
import services.semanticModels.ISemanticModel;

/**
 * 
 * @author Mihai Dascalu
 */
public class LSA implements ISemanticModel {
	private static Logger logger = Logger.getLogger(LSA.class);

	private static List<LSA> LOADED_LSA_SPACES = new LinkedList<LSA>();

	public static final int LOWER_BOUND = 50;
	public static final double LSA_THRESHOLD = 0.25;
	public static final int K = 300;
	public static final int NO_KNN_NEIGHBOURS = 100;

	private Lang language;
	private String path;
	private double[][] Uk;
	private double[] Sk;
	private double[][] Vtk;
	private BidiMap<Word, Integer> words;
	private Map<Word, Double> mapIdf;
	private double[] vectorSpaceMean;

	public static LSA loadLSA(String path, Lang language) {
		try {
			for (LSA lsa : LOADED_LSA_SPACES) {
				if (path.equals(lsa.getPath())) {
					return lsa;
				}
			}

			logger.info("Loading LSA semantic space");
			LSA lsaLoad = new LSA();
			lsaLoad.setLanguage(language);
			lsaLoad.setPath(path);
			lsaLoad.loadWordList(path);
			lsaLoad.loadIdf(path);
			lsaLoad.Uk = (double[][]) ObjectManipulation.loadObject(path + "/U.ser");
			lsaLoad.determineSpaceMean();
			LOADED_LSA_SPACES.add(lsaLoad);
			return lsaLoad;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Error during vector space loading!");
			return null;
		}
	}

	protected void loadIdf(String path) throws FileNotFoundException, IOException {
		// loads IDf matrix from file
		FileInputStream inputFile = new FileInputStream(path + "/idf.txt");
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		mapIdf = new TreeMap<Word, Double>();
		String str_linie = "";
		StringTokenizer strk;
		while ((str_linie = in.readLine()) != null) {
			strk = new StringTokenizer(str_linie, " ");
			mapIdf.put(Word.getWordFromConcept(strk.nextToken(), language), Double.parseDouble(strk.nextToken()));
		}
		in.close();
	}

	protected void loadWordList(String path) throws FileNotFoundException, IOException {
		// loads IDf matrix from file
		FileInputStream inputFile = new FileInputStream(path + "/wordlist.txt");
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		words = new DualTreeBidiMap<Word, Integer>();
		String str_linie = "";
		StringTokenizer strk;

		while ((str_linie = in.readLine()) != null) {
			strk = new StringTokenizer(str_linie, " ");
			words.put(Word.getWordFromConcept(strk.nextToken(), language), Integer.parseInt(strk.nextToken()));
		}
		in.close();
	}

	public double getWordIDf(Word word) {
		double idf = 0;
		if (words.containsKey(word)) {
			// words exist in learning space
			idf = Math.log(mapIdf.get(word));
		} else {
			// extract all words from the semantic space that have the same
			// stem
			int no = 0;
			for (Word w : words.keySet()) {
				if (w.getStem().equals(word.getStem())) {
					idf += 1 / mapIdf.get(w);
					no++;
				}
			}
			if (no != 0) {
				idf = Math.log(no / idf);
			}
		}
		return idf;
	}

	public double[] getWordVector(Word word) {
		double[] vector = new double[K];
		if (words.containsKey(word)) {
			// words exist in learning space
			int index = words.get(word);
			for (int i = 0; i < LSA.K; i++) {
				vector[i] = Uk[index][i];
			}
		} else {
			// extract all words from the semantic space that have the same
			// stem
			int no = 0;
			for (Word w : words.keySet()) {
				if (w.getStem().equals(word.getStem())) {
					int index = words.get(w);
					for (int i = 0; i < LSA.K; i++) {
						vector[i] += Uk[index][i];
					}
					no++;
				}
			}
			if (no != 0) {
				for (int i = 0; i < LSA.K; i++) {
					vector[i] /= no;
				}
			}
		}
		return vector;
	}

	private void determineSpaceMean() {
		// determine space median
		vectorSpaceMean = new double[K];
		for (Word w : words.keySet()) {
			double idf = Math.log(mapIdf.get(w));
			int index = words.get(w);
			for (int i = 0; i < K; i++) {
				vectorSpaceMean[i] += Uk[index][i] * idf;
			}
		}
		for (int i = 0; i < K; i++) {
			vectorSpaceMean[i] /= words.size();
		}
	}

	public double getSimilarity(Word w1, Word w2) {
		// compare similarity between two strings
		return VectorAlgebra.cosineSimilarity(getWordVector(w1), getWordVector(w2));
	}

	public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
		return VectorAlgebra.cosineSimilarity(e1.getLSAVector(), e2.getLSAVector());
	}

	public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
		return getSimilarConcepts(getWordVector(w), minThreshold);
	}

	public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
		return getSimilarConcepts(e.getLSAVector(), minThreshold);
	}

	public TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
		TreeMap<Word, Double> similarConcepts = new TreeMap<Word, Double>();
		double[] vector2 = null;
		double sim;
		for (Word c : words.keySet()) {
			vector2 = getWordVector(c);
			sim = VectorAlgebra.cosineSimilarity(vector, vector2);
			if (sim >= minThreshold) {
				similarConcepts.put(c, sim);
			}
		}
		return similarConcepts;
	}

	public Lang getLanguage() {
		return language;
	}

	public void setLanguage(Lang language) {
		this.language = language;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public double[][] getUk() {
		return Uk;
	}

	public void setUk(double[][] uk) {
		Uk = uk;
	}

	public double[] getSk() {
		return Sk;
	}

	public void setSk(double[] sk) {
		Sk = sk;
	}

	public double[][] getVtk() {
		return Vtk;
	}

	public void setVtk(double[][] vtk) {
		Vtk = vtk;
	}

	public BidiMap<Word, Integer> getWords() {
		return words;
	}

	@Override
	public Set<Word> getWordSet() {
		return words.keySet();
	}

	public void setWords(BidiMap<Word, Integer> words) {
		this.words = words;
	}

	public Map<Word, Double> getMapIdf() {
		return mapIdf;
	}

	public void setMapIdf(Map<Word, Double> mapIdf) {
		this.mapIdf = mapIdf;
	}

	public double[] getVectorSpaceMean() {
		return vectorSpaceMean;
	}

	public void setVectorSpaceMean(double[] vectorSpaceMean) {
		this.vectorSpaceMean = vectorSpaceMean;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		// LSA lsa = LSA.loadLSA("config/LSA/tasa_new_en2", Lang.eng);
		//
		// Word w1 = Word.getWordFromConcept("cat", Lang.eng);
		// Word w2 = Word.getWordFromConcept("dog", Lang.eng);
		// System.out.println(lsa.getSimilarity(w1, w2));
		//
		// for (Entry<Word, Double> entry : lsa.getSimilarConcepts(
		// Word.getWordFromConcept("cat", Lang.eng), 0.3).entrySet()) {
		// System.out.println(entry.getKey().getLemma() + "\t"
		// + entry.getValue());
		// }

		LSA lsa = LSA.loadLSA("config/LSA/tasa_en", Lang.eng);

		Word w1 = Word.getWordFromConcept("men", Lang.eng);
		Word w2 = Word.getWordFromConcept("address", Lang.eng);
		System.out.println(lsa.getSimilarity(w1, w2));

		// for (Entry<Word, Double> entry :
		// lsa.getSimilarConcepts(Word.getWordFromConcept("psicología",
		// Lang.es), 0.3)
		// .entrySet()) {
		// System.out.println(entry.getKey().getLemma() + "\t" +
		// entry.getValue());
		// }
	}
}
