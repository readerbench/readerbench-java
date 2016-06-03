package services.nlp.listOfWords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Block;
import data.Lang;
import data.Sentence;
import services.commons.TextPreprocessing;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Mihai Dascalu
 */
public class ClassesOfWords {

	static Logger logger = Logger.getLogger(ClassesOfWords.class);

	private Map<String, Set<String>> classes;

	public ClassesOfWords(String path) {
		BufferedReader in = null;
		classes = new TreeMap<>();
		try {
			FileInputStream inputFile = new FileInputStream(path);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			in = new BufferedReader(ir);
			String line;
			String className = null;
			while ((line = in.readLine()) != null) {
				String concept = line.toLowerCase().trim().toLowerCase();
				if (concept.startsWith("[")) {
					className = concept.replaceAll("\\[", "").replaceAll("\\]", "").trim();
					if (!classes.containsKey(className)) {
						classes.put(className, new TreeSet<>());
					}
				} else if (className != null && concept.length() > 0) {
					classes.get(className).add(concept);
				}
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

	public void writeClassesOfWords(String path) {
		BufferedWriter out = null;
		try {
			FileOutputStream outputFile = new FileOutputStream(path);
			OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
			out = new BufferedWriter(ow);
			for (Entry<String, Set<String>> entry : classes.entrySet()) {
				out.write("[" + entry.getKey() + "]\n");
				for (String w : entry.getValue()) {
					if (w != null & w.length() > 0) {
						out.write(w + "\n");
					}
				}
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	public Map<String, Set<String>> getClasses() {
		return classes;
	}

	public Set<String> getWords(String className) {
		return classes.get(className);
	}

	public Set<String> getAllWords() {
		Set<String> set = new TreeSet<>();
		for (Set<String> words : classes.values()) {
			set.addAll(words);
		}
		return set;
	}

	public double countAveragePatternOccurrences(AbstractDocument document, String className) {
		int no_occurences = 0;
		int no_blocks = 0;
		for (String p : this.getClasses().get(className)) {
			// check that the pattern does not exist in any of the previous
			// sentences
			for (Block b : document.getBlocks()) {
				if (b != null) {
					no_blocks++;
					for (Sentence s : b.getSentences()) {
						String text = TextPreprocessing.cleanText(s.getText(), document.getLanguage());

						Pattern pattern = Pattern.compile("(?:\\s)" + p + "(?:\\s)");
						Matcher matcher = pattern.matcher(text);

						int count = 0;
						if (matcher.find())
							count++;
						if (count > 0) {
							while (matcher.find(matcher.end() - 1))
								count++;
						}
						no_occurences += count;
					}
				}
			}
		}
		if (no_blocks == 0) {
			return ComplexityIndices.IDENTITY;
		}
		return ((double) no_occurences) / no_blocks;
	}

	public static void main(String[] args) {
		String text = TextPreprocessing.cleanText("C'est-à-dire atque  que que queque Tout va atque bien", Lang.fr);
		System.out.println(text);
		Pattern pattern = Pattern.compile("(?:\\s)que(?:\\s)");
		Matcher matcher = pattern.matcher(text);

		int count = 0;
		if (matcher.find())
			count++;
		if (count > 0) {
			while (matcher.find(matcher.end() - 1))
				count++;
		}
		System.out.println(count);
	}
}
