package com.readerbench.coreservices.keywordmining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KeywordPositions {
	private Map<String, String> lemmasEn;
	private List<String> simpleWords;
	private List<String> processedWords;
	private Map<String, List<Integer>> wordsOccurencesPos;
	private List<SimpleEntry<String, Double>> ARFKeywords;
	private List<SimpleEntry<String, Double>> AWTKeywords;
	private List<SimpleEntry<String, Double>> ALDKeywords;
	private Map<String, String> kTypes = new HashMap<>();
	private Map<String, String> kWords = new HashMap<>();
	private Map<String, String> kPoses = new HashMap<>();
	private Map<String, Double> kRelevances = new HashMap<>();
	
	public KeywordPositions() {
		this.lemmasEn = new HashMap<String, String>();
		this.simpleWords = new ArrayList<String>();
		this.lemmasEnInit();
		this.simpleWordsInit();
	}
	
	private void lemmasEnInit() {
		BufferedReader lemmasEnReader = null;

		try {
		    File lemmasEnFile = new File("all_texts/lemmas_en.txt");
		    lemmasEnReader = new BufferedReader(new FileReader(lemmasEnFile));

		    String line, key, value;
		    int spaceIdx;
		    while ((line = lemmasEnReader.readLine()) != null) {
		    	spaceIdx = line.indexOf('\t');
		        value = line.substring(0, spaceIdx);
		        key = line.substring(spaceIdx + 1, line.length());
		        this.lemmasEn.put(key.toLowerCase(), value.toLowerCase());
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        lemmasEnReader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	private void simpleWordsInit() {
		BufferedReader simpleWordsReader = null;

		try {
		    File simpleWordsFile = new File("all_texts/simple_words.txt");
		    simpleWordsReader = new BufferedReader(new FileReader(simpleWordsFile));

		    String line;
		    while ((line = simpleWordsReader.readLine()) != null) {
		        this.simpleWords.add(line.toLowerCase());
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        simpleWordsReader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	
	public void preprocess(String fileName) {
		this.processedWords = new ArrayList<String>();
		BufferedReader wordsReader = null;

		try {
		    File wordsFile = new File(fileName);
		    wordsReader = new BufferedReader(new FileReader(wordsFile));

		    String line;
		    String[] splits;

		    /* Replace numbers and punctuation marks */
		    /* Lower case the result */
		    while ((line = wordsReader.readLine()) != null) {
		    	splits = line.replaceAll("\\[", " ").replaceAll("]", " ").replaceAll("[0-9!\"#$%&'()*+,./:;<=>?@^`{|}~]", " ").toLowerCase().split("\\s+");
		    	this.processedWords.addAll(Arrays.asList(splits));
		    }
		    
		    /* Remove the simple|stop words */
		    System.out.println(this.processedWords.size());
		    this.processedWords.removeIf(word -> this.simpleWords.contains(word));
		    System.out.println(this.processedWords.size());
		    
		    /* Stemming and Lemmatization */
		    String currWord;
		    for (int i = 0; i < this.processedWords.size(); i++) {
		    	currWord = this.processedWords.get(i);
		    	if (this.lemmasEn.containsKey(currWord))
		    		this.processedWords.set(i, this.lemmasEn.get(currWord));
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        wordsReader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	
	class DescendingComparator implements Comparator<SimpleEntry<String, Double>> {
		public int compare(SimpleEntry<String, Double> arg0, SimpleEntry<String, Double> arg1) {
			if (arg0.getValue() == arg1.getValue())		return 0;
			if (arg0.getValue() < arg1.getValue())		return 1;
			return -1;
		}
	}
	class AscendingComparator implements Comparator<SimpleEntry<String, Double>> {
		public int compare(SimpleEntry<String, Double> arg0, SimpleEntry<String, Double> arg1) {
			if (arg0.getValue() == arg1.getValue())		return 0;
			if (arg0.getValue() > arg1.getValue())		return 1;
			return -1;
		}
	}
	
	/* Average reduced frequency */
	public void computeARFKeywords() {
		this.ARFKeywords = new ArrayList<>();
		for (String word: wordsOccurencesPos.keySet()) {
			Double score = 0.0;
			Double segmentLength;
			List<Integer> wordOccurencesPos = wordsOccurencesPos.get(word);
			segmentLength = this.processedWords.size() * 1.0 / wordOccurencesPos.size();
			for (int i = 0; i < wordOccurencesPos.size(); i++) {
				Integer d;
				if (i == 0)
					d = wordOccurencesPos.get(0) + this.processedWords.size() - wordOccurencesPos.get(wordOccurencesPos.size()-1);
				else
					d = wordOccurencesPos.get(i) - wordOccurencesPos.get(i - 1);
				score += Math.min(segmentLength, d);
			}
			score /= segmentLength;
			this.ARFKeywords.add(new SimpleEntry<>(word, score));
		}
		DescendingComparator arfComparator = new DescendingComparator(); 
		this.ARFKeywords.sort(arfComparator);
	}
	
	/* Average waiting time */
	public void computeAWTKeywords() {
		this.AWTKeywords = new ArrayList<>();
		for (String word: wordsOccurencesPos.keySet()) {
			Double awt = 0.0;
			List<Integer> wordOccurencesPos = wordsOccurencesPos.get(word);
			for (int i = 0; i < wordOccurencesPos.size(); i++) {
				Integer d;
				if (i == 0)
					d = wordOccurencesPos.get(0) + this.processedWords.size() - wordOccurencesPos.get(wordOccurencesPos.size()-1);
				else
					d = wordOccurencesPos.get(i) - wordOccurencesPos.get(i - 1);
				awt += 1.0 * d * d;
			}
			awt = (1 + awt / this.processedWords.size()) / 2;
			this.AWTKeywords.add(new SimpleEntry<>(word, awt));
		}
		AscendingComparator awtComparator = new AscendingComparator(); 
		this.AWTKeywords.sort(awtComparator);
	}
	
	/* Average logarithmic distance */
	public void computeALDKeywords() {
		this.ALDKeywords = new ArrayList<>();
		for (String word: wordsOccurencesPos.keySet()) {
			Double ald = 0.0;
			List<Integer> wordOccurencesPos = wordsOccurencesPos.get(word);
			for (int i = 0; i < wordOccurencesPos.size(); i++) {
				Integer d;
				if (i == 0)
					d = wordOccurencesPos.get(0) + this.processedWords.size() - wordOccurencesPos.get(wordOccurencesPos.size()-1);
				else
					d = wordOccurencesPos.get(i) - wordOccurencesPos.get(i - 1);
				ald += 1.0 * d * Math.log10(d);
			}
			ald = ald / this.processedWords.size();
			this.ALDKeywords.add(new SimpleEntry<>(word, ald));
		}
		AscendingComparator aldComparator = new AscendingComparator(); 
		this.ALDKeywords.sort(aldComparator);
	}

	private void computeWordsOccurencesPos() {
		Map<String, List<Integer>> wordsOccurencesPos = new HashMap<String, List<Integer>>();
		for (int i = 0; i < this.processedWords.size(); i++) {
			String currWord = this.processedWords.get(i);
			if (!wordsOccurencesPos.containsKey(currWord)) {
				wordsOccurencesPos.put(currWord, new ArrayList<Integer>());
			}
			wordsOccurencesPos.get(currWord).add(i);
		}
		
		this.wordsOccurencesPos = wordsOccurencesPos;
	}
	
	public void writeProcessedWordsToFile(String fileName) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			for (String word: this.processedWords) {
				out.write(word);
				out.write(' ');
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readKeywords(String fileName, int noIgnoreLines) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		kTypes = new HashMap<>();
		kWords = new HashMap<>();
		kPoses = new HashMap<>();
		kRelevances = new HashMap<>();  
        for (int i = 0; i < noIgnoreLines; i++) br.readLine();
        while ((line = br.readLine()) != null) {
     	   String[] data = line.split(";");
     	   String type = data[0];
     	   String word = data[1];
     	   String lemma = data[2].toLowerCase();
     	   String pos = data[3];
     	   String relevance = data[4];
     	   kTypes.put(lemma, type);
     	   kWords.put(lemma, word);
     	   kPoses.put(lemma, pos);
     	   kRelevances.put(lemma, Double.valueOf(relevance));
        }
        br.close();
	}
	
	public void printToFile(List<SimpleEntry<String, Double>> updatedKeywords, String fileName) throws IOException {
		BufferedWriter outRelevance = new BufferedWriter(new FileWriter(fileName, true));
		   StringBuilder csv = new StringBuilder("SEP=;\nlemma;nr_occ;relevance;\n");
		   for (Entry<String, Double> line: updatedKeywords) {
			   String lemma = line.getKey();
			   Double relevance = line.getValue();
			   if (!kRelevances.containsKey(lemma))
				   continue;
			   csv.append(lemma).append(";");
			   csv.append(this.wordsOccurencesPos.get(lemma).size()).append(";");
			   csv.append(relevance).append(";");
			   outRelevance.write(csv.toString());
	           outRelevance.newLine();
	           outRelevance.flush();
	           csv.setLength(0);
		   }
		   outRelevance.close();
	}
	
	public void generateClusteringFilesARF(String outPath, String inPath) throws IOException {
	   File dir = new File(inPath);
	   List<String> ARFKeywordsList = new ArrayList<>();
	   for (Entry<String, Double> e: this.ARFKeywords)
		   ARFKeywordsList.add(e.getKey());
	   File[] files = dir.listFiles((File pathname) -> {
           return pathname.getName().toLowerCase().endsWith(".txt");
       });
	   for (File file: files) {
		   BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
		   BufferedWriter bw = new BufferedWriter(new FileWriter(outPath + "/" + file.getName(), true));
		   StringBuilder sb = new StringBuilder("");
		   String line;
           while ((line = br.readLine()) != null) {
        	   String[] splits = line.split("\\s+");
        	   for (String split: splits) {
        		   if (ARFKeywordsList.contains(split) && kRelevances.containsKey(split)) {
        			   sb.append(split).append(' ');
        		   }
        	   }
           }
           bw.write(sb.toString());
           bw.flush();
           sb.setLength(0);
           br.close();
           bw.close();
       }
	}
	
	public void generateClusteringFilesAWT(String outPath, String inPath) throws IOException {
		   File dir = new File(inPath);
		   List<String> AWTKeywordsList = new ArrayList<>();
		   for (Entry<String, Double> e: this.AWTKeywords)
			   AWTKeywordsList.add(e.getKey());
		   File[] files = dir.listFiles((File pathname) -> {
	           return pathname.getName().toLowerCase().endsWith(".txt");
	       });
		   for (File file: files) {
			   BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
			   BufferedWriter bw = new BufferedWriter(new FileWriter(outPath + "/" + file.getName(), true));
			   StringBuilder sb = new StringBuilder("");
			   String line;
	           while ((line = br.readLine()) != null) {
	        	   String[] splits = line.split("\\s+");
	        	   for (String split: splits) {
	        		   if (AWTKeywordsList.contains(split) && kRelevances.containsKey(split)) {
	        			   sb.append(split).append(' ');
	        		   }
	        	   }
	           }
	           bw.write(sb.toString());
	           bw.flush();
	           sb.setLength(0);
	           br.close();
	           bw.close();
	       }
		}

	public static void main(String[] args) throws IOException {
		String keywordsFile = "fulltexts/lsa_merged_keywords.csv";
		String outputFileARF = "fulltexts/keywords_updated_relevance_lsa_arf_sciref_10_percent.csv";
		String outputFileAWT = "fulltexts/keywords_updated_relevance_lsa_awt_sciref_10_percent.csv";
		String clusteringPathARF = "keywords/updated_keywords_arf";
		String clusteringPathAWT = "keywords/updated_keywords_awt";
		String filesFromKeywordsPath = "keywords";
		
		KeywordPositions commonness = new KeywordPositions();
		commonness.preprocess("all_texts/all_texts.txt");
		commonness.computeWordsOccurencesPos();
		commonness.readKeywords(keywordsFile, 2);
		
		// Average reduced frequency
		commonness.computeARFKeywords();
		commonness.printToFile(commonness.ARFKeywords, outputFileARF);
		commonness.ARFKeywords = commonness.ARFKeywords.stream().limit(commonness.ARFKeywords.size() / 2).collect(Collectors.toList());
		
		// Average waiting time
		commonness.computeAWTKeywords();
		commonness.printToFile(commonness.AWTKeywords, outputFileAWT);
		commonness.AWTKeywords = commonness.AWTKeywords.stream().limit(commonness.AWTKeywords.size() / 2).collect(Collectors.toList());
		
		// Generate new files for clustering
		commonness.generateClusteringFilesARF(clusteringPathARF, filesFromKeywordsPath);
		commonness.generateClusteringFilesAWT(clusteringPathAWT, filesFromKeywordsPath);
		
	}
}
