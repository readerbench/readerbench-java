package com.readerbench.services.semanticModels.procrustes;

import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.decompositions.SSVD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple3;
import com.readerbench.services.commons.VectorAlgebra;
import com.readerbench.services.semanticModels.LSA.LSA;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProcrustesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcrustesUtils.class);

	public static ArrayList<String> parseConceptFile(String path) throws FileNotFoundException, IOException {
		ArrayList<String> wordList = new ArrayList<String>();

		FileInputStream inputFile = new FileInputStream(path);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";

		while ((line = in.readLine()) != null) {
			wordList.add(line);
		}
		in.close();

		return wordList;
	}

	public static Set<String> parseConceptFileToSet(String path) throws FileNotFoundException, IOException {
		Set<String> wordSet = new HashSet<String>();

		FileInputStream inputFile = new FileInputStream(path);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";

		while ((line = in.readLine()) != null) {
			wordSet.add(line);
		}
		in.close();

		return wordSet;
	}

	public static double[] trimVector(double[] v, int K) {
		if (v.length == K)
			return v;

		double[] r = new double[K];

		for (int i = 0; i < K; i++) {
			r[i] = v[i];
		}

		return r;
	}

	public static double[][] get2dArrayFromMatrix(Matrix M) {
		int r = M.numRows();
		int c = M.numCols();

		double[][] d = new double[r][c];

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				d[i][j] = M.get(i, j);
			}
		}

		return d;
	}

	public static double[] getArrayFromVector(Vector v) {
		int l = v.size();

		double[] d = new double[l];

		for (int i = 0; i < l; i++) {
			d[i] = v.get(i);
		}

		return d;
	}

	public static Tuple2<ArrayList<Word>, ArrayList<Word>> getWordLists(LSA lsa1, LSA lsa2, String conceptFile1,
			String conceptFile2) {
		ArrayList<String> conceptList1 = null, conceptList2 = null;
		ArrayList<Word> wordList1 = new ArrayList<Word>();
		ArrayList<Word> wordList2 = new ArrayList<Word>();

		try {
			conceptList1 = parseConceptFile(conceptFile1);
			conceptList2 = parseConceptFile(conceptFile2);
		} catch (IOException e) {
			LOGGER.error("IO error.");
			e.printStackTrace();
		}

		int size = conceptList1.size();

		// get word lists
		Set<Word> wordSet1 = lsa1.getWordSet();
		Set<Word> wordSet2 = lsa2.getWordSet();
		Word word1, word2;

		for (int i = 0; i < size; i++) {
			word1 = Word.getWordFromConcept(conceptList1.get(i), lsa1.getLanguage());
			word2 = Word.getWordFromConcept(conceptList2.get(i), lsa2.getLanguage());

			if (wordSet1.contains(word1) && wordSet2.contains(word2)) {
				wordList1.add(word1);
				wordList2.add(word2);
			}
		}

		return new Tuple2<ArrayList<Word>, ArrayList<Word>>(wordList1, wordList2);
	}

	public static double[][] meanCenter(double[][] M) {
		int r = M.length;
		int c = M[0].length;

		for (int j = 0; j < c; j++) {
			double s = 0;

			for (int i = 0; i < r; i++) {
				s += M[i][j];
			}

			s /= r;

			for (int i = 0; i < r; i++) {
				M[i][j] = M[i][j] - s;
			}
		}

		return M;
	}

	public static Matrix meanCenter(Matrix M) {
		int r = M.numRows();
		int c = M.numCols();

		for (int j = 0; j < c; j++) {
			double s = 0;

			for (int i = 0; i < r; i++) {
				s += M.get(i, j);
			}

			s /= r;

			for (int i = 0; i < r; i++) {
				M.set(i, j, M.get(i, j) - s);
			}
		}

		return M;
	}

	public static double[][] normalize(double[][] M) {
		int r = M.length;
		int c = M[0].length;

		for (int i = 0; i < r; i++) {
			double norm = VectorAlgebra.norm2(M[i]);
			for (int j = 0; j < c; j++) {
				M[i][j] /= norm;
			}
		}

		return M;
	}

	public static Matrix calcRotationMatrix(Matrix X, Matrix Y) {
		// do the SVD: Q = U*Vt where Yt*X = U*S*Vt
		Tuple3<Matrix, Matrix, Vector> result = SSVD.ssvd(Y.transpose().times(X), 300, 0, 1);
		return result._1().times(result._2().transpose());
	}

	public static Tuple2<Matrix, Matrix> getEmbeddings(LSA lsa1, LSA lsa2, ArrayList<Word> wordList1,
			ArrayList<Word> wordList2) {
		int size = wordList1.size();
		double[][] Xk = new double[size][];
		double[][] Yk = new double[size][];

		for (int i = 0; i < size; i++) {
			Xk[i] = trimVector(lsa1.getWordVector(wordList1.get(i)), lsa1.K);
			Yk[i] = trimVector(lsa2.getWordVector(wordList2.get(i)), lsa2.K);
		}

		Matrix X = new DenseMatrix(Xk);
		Matrix Y = new DenseMatrix(Yk);

		return new Tuple2<Matrix, Matrix>(X, Y);
	}

	public static Matrix getRotationMatrix(LSA lsa1, LSA lsa2, String conceptFile1, String conceptFile2) {
		// read concept lists
		Tuple2<ArrayList<Word>, ArrayList<Word>> wl = getWordLists(lsa1, lsa2, conceptFile1, conceptFile2);
		ArrayList<Word> wordList1 = wl._1;
		ArrayList<Word> wordList2 = wl._2;

		// get rotation matrix
		Tuple2<Matrix, Matrix> emb = getEmbeddings(lsa1, lsa2, wordList1, wordList2);
		return calcRotationMatrix(emb._1, emb._2);
	}

	public static double[][] rotate(LSA lsa1, LSA lsa2, Matrix Q) {
		return get2dArrayFromMatrix(new DenseMatrix(lsa2.getUk()).times(Q));
	}

	public static void saveRotationMatrix(LSA lsa1, LSA lsa2, String conceptFile1, String conceptFile2) {

	}

	public static void main(String[] args) {
		// set paths
		String lsaFile1 = "resources/config/EN/LSA/COCA_newspaper";
		String lsaFile2 = "resources/config/FR/LSA/Le_Monde";

		Lang lang1 = Lang.en;
		Lang lang2 = Lang.fr;

		String conceptFile1 = "../wip/Word frequency lists/en-fr_en.txt";
		String conceptFile2 = "../wip/Word frequency lists/en-fr_fr.txt";

		// load LSA spaces
		LSA lsa2 = LSA.loadLSA(lsaFile1, lang1);
		LSA lsa1 = LSA.loadLSA(lsaFile2, lang2);

		// mean center
		lsa1.setUk(meanCenter(lsa1.getUk()));
		lsa2.setUk(meanCenter(lsa2.getUk()));

		// normalize
		lsa1.setUk(normalize(lsa1.getUk()));
		lsa2.setUk(normalize(lsa2.getUk()));

		// tests
		lsa2.setUk(rotate(lsa1, lsa2, getRotationMatrix(lsa1, lsa2, conceptFile1, conceptFile2)));

		/*
		 * Word w1 = Word.getWordFromConcept("newspaper", Lang.eng); Word w2 =
		 * Word.getWordFromConcept("journal", Lang.fr);
		 * 
		 * double[] wv1 = lsa1.getWordVector(w1); double[] wv2 =
		 * lsa2.getWordVector(w2); TreeMap<Word, Double> similarConcepts =
		 * lsa1.getSimilarConcepts(wv2, 0.3);
		 * 
		 * for (Map.Entry<Word, Double> entry : similarConcepts.entrySet()) {
		 * Word key = entry.getKey(); Double value = entry.getValue();
		 * 
		 * System.out.println(key.getText() + " => " + value); }
		 * 
		 * System.out.println(VectorAlgebra.cosineSimilarity(wv1, wv2)); Word
		 * res = lsa1.getMostSimilarConcept(wv2);
		 * System.out.println(res.getText());
		 * System.out.println(VectorAlgebra.cosineSimilarity(wv2,
		 * lsa1.getWordVector(res)));
		 */

		Set<String> conceptSet1 = null, conceptSet2 = null;

		try {
			conceptSet1 = parseConceptFileToSet(conceptFile1);
			conceptSet2 = parseConceptFileToSet(conceptFile2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File file1 = new File("../wip/fr_en_n.csv");

		if (!file1.exists()) {
			try {
				file1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			FileWriter fw1 = new FileWriter(file1.getAbsoluteFile());
			BufferedWriter bw1 = new BufferedWriter(fw1);

			Word y;
			Set<Word> wordSet1 = lsa1.getWordSet();
			Set<Word> wordSet2 = lsa2.getWordSet();

			for (Word x : wordSet1) {
				y = lsa2.getMostSimilarConcept(lsa1.getWordVector(x));
				bw1.write(x.getText() + "," + y.getText() + ","
						+ VectorAlgebra.cosineSimilarity(lsa1.getWordVector(x), lsa2.getWordVector(y)));

				if (conceptSet1.contains(x.getText())) {
					bw1.write("," + "1");
				} else {
					bw1.write("," + "0");
				}

				bw1.newLine();
			}

			bw1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
