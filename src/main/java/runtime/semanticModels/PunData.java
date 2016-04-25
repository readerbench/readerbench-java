package runtime.semanticModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocumentTemplate;
import data.Word;
import data.discourse.Topic;
import data.document.Document;
import data.Lang;
import services.commons.Formatting;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class PunData {
	static Logger logger = Logger.getLogger(PunData.class);

	private static String compareDocs(String s1, String s2, ISemanticModel semModel, double minThreshold) {
		LSA lsa = null;
		LDA lda = null;
		if (semModel instanceof LSA) {
			lsa = (LSA) semModel;
		}
		if (semModel instanceof LDA) {
			lda = (LDA) semModel;
		}
		Document d1 = new Document(null, AbstractDocumentTemplate.getDocumentModel(s1), lsa, lda,
				semModel.getLanguage(), true, false);
		Document d2 = new Document(null, AbstractDocumentTemplate.getDocumentModel(s2), lsa, lda,
				semModel.getLanguage(), true, false);
		Document merge = new Document(null, AbstractDocumentTemplate.getDocumentModel(s1 + " " + s2), lsa, lda,
				semModel.getLanguage(), true, false);
		String out = s1 + "-" + s2 + "," + Formatting.formatNumber(semModel.getSimilarity(d1, d2));

		List<Topic> inferredConcepts = new ArrayList<Topic>();

		TreeMap<Word, Double> simWords = semModel.getSimilarConcepts(merge, minThreshold);

		for (Entry<Word, Double> entry : simWords.entrySet()) {
			if (!merge.getWordOccurences().keySet().contains(entry.getKey())) {
				Topic t = new Topic(entry.getKey(), entry.getValue());

				if (inferredConcepts.contains(t)) {
					Topic updatedTopic = inferredConcepts.get(inferredConcepts.indexOf(t));
					updatedTopic.setRelevance(Math.max(updatedTopic.getRelevance(), entry.getValue()));
				} else {
					inferredConcepts.add(t);
				}
			}
		}

		Collections.sort(inferredConcepts);

		for (Topic t : inferredConcepts) {
			out += "," + t.getWord().getLemma() + "," + Formatting.formatNumber(t.getRelevance());
		}

		return out;
	}

	public void comparePuns(String pathToFile, ISemanticModel semModel, double minThreshold) {
		try {
			FileInputStream inputFile = new FileInputStream(pathToFile);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);

			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(
									pathToFile
											.replace(".csv",
													"_" + semModel.getClass().getSimpleName() + "_"
															+ (new File(semModel.getPath()).getName()) + ".csv")),
							"UTF-8"));
			// read first line apriori
			String line = in.readLine();
			while ((line = in.readLine()) != null) {
				if (line.length() > 0) {
					StringTokenizer st = new StringTokenizer(line, ",");
					try {
						String s1 = st.nextToken(), s2 = st.nextToken(), s3 = st.nextToken();
						out.write(compareDocs(s1, s3, semModel, minThreshold) + "\n");
						out.write(compareDocs(s2, s3, semModel, minThreshold) + "\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			in.close();
			out.close();
		} catch (

		IOException e)

		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		PunData comp = new PunData();

		 LDA lda = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
//		LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		comp.comparePuns("in/pun data/pun data.csv", lda, 0.5);
	}
}
