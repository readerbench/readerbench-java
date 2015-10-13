package services.nlp.listOfWords;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.cmu.lti.jawjaw.pobj.Lang;

/**
 * 
 * @author Mihai Dascalu
 */
public class MapOfWordWeights {
	static Logger logger = Logger.getLogger(MapOfWordWeights.class);

	private Map<String, Double> words;

	public MapOfWordWeights(String path, Lang lang) {
		BufferedReader in = null;
		words = new TreeMap<String, Double>();
		try {
			FileInputStream inputFile = new FileInputStream(path);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			in = new BufferedReader(ir);
			String line = null;
			while ((line = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line.trim());
				String word = st.nextToken();
				Double weight = 0.0;
				try {
					weight = Double.valueOf(st.nextToken());
					if (weight < 0 || weight > 1)
						throw new Exception("Weight not in [0; 1] interval!");
				} catch (Exception e) {
					e.printStackTrace();
					weight = 1.0;
				}
				if (Dictionary.isDictionaryWord(word, lang))
					words.put(word, weight);
				else
					logger.info("Word " + word
							+ " was not found within the dictionary words");
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	public Map<String, Double> getWords() {
		return words;
	}

	public void setWords(Map<String, Double> words) {
		this.words = words;
	}

}
