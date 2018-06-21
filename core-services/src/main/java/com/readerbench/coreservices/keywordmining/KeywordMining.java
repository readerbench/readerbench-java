package com.readerbench.coreservices.keywordmining;

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import com.readerbench.processingservice.exportdata.ExportDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.NGram;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.MetaDocument;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.coreservices.semanticmodels.SimilarityType;

/**
*
* @author Mihai Dascalu
*/
public class KeywordMining {

   private static final Logger LOGGER = LoggerFactory.getLogger(KeywordMining.class);

   private final String processingPath;
   private final int noTopKeyWords;
   private final List<SemanticModel> models;
   private final Lang lang;
   private final boolean usePOSTagging;
   private final boolean computeDialogism;
   private final boolean meta;

   public KeywordMining(String processingPath, int noTopKeyWords, List<SemanticModel> models, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean meta) {
       this.processingPath = processingPath;
       this.noTopKeyWords = noTopKeyWords;
       this.models = models;
       this.lang = lang;
       this.usePOSTagging = usePOSTagging;
       this.computeDialogism = computeDialogism;
       this.meta = meta;
   }

   public void addTopKeywords(Set<Keyword> keywords, Document d, int noTopKeyWords) {
       List<Keyword> topics = KeywordModeling.getSublist(d.getTopics(), noTopKeyWords, false, false);
       for (Keyword t : topics) {
           keywords.add(t);
       }
   }

   public Map<Word, Double> getRelevance(Document d, Set<Word> keywords) {
       Map<Word, Double> keywordOccurrences = new TreeMap<>();

       List<Keyword> topics = d.getTopics();
       for (int i = 0; i < topics.size(); i++) {
           for (Word keyword : keywords) {
               //determine identical stem
               if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                   keywordOccurrences.put(topics.get(i).getWord(), topics.get(i).getRelevance());
               }
           }
       }
       return keywordOccurrences;
   }

   public Map<Word, Integer> getIndex(Document d, Set<Word> keywords) {
       Map<Word, Integer> keywordOccurrences = new TreeMap<>();

       List<Keyword> topics = d.getTopics();
       for (int i = 0; i < topics.size(); i++) {
           for (Word keyword : keywords) {
               //determine identical stem
               if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                   keywordOccurrences.put(topics.get(i).getWord(), i);
               }
           }
       }
       return keywordOccurrences;
   }

   public void saveSerializedFromXml() {
       File dir = new File(processingPath);

       if (!dir.exists()) {
           throw new RuntimeException("Inexistent Folder: " + dir.getPath());
       }

       File[] files = dir.listFiles((File pathname) -> {
           return pathname.getName().toLowerCase().endsWith(".xml");
       });

       for (File file : files) {
           File f = new File(file.getPath().replace(".xml", ".ser"));
           if (f.exists() && !f.isDirectory()) {
               continue;
           }
           LOGGER.info("Processing {} file", file.getName());
           // Create file

           Document d;
           try {
               if (meta) {
                   d = new MetaDocument(file.getAbsolutePath(), models, lang);
                   ((MetaDocument)d).setLevel(MetaDocument.DocumentLevel.Subsection);
               } else {
                   d = new Document(file.getAbsolutePath(), models, lang);
               }
              // d.save(AbstractDocument.SaveType.SERIALIZED);
           } catch (Exception e) {
               //LOGGER.error("Runtime error while processing {}: {} ...", new Object[]{file.getName(), e.getMessage()});
               e.printStackTrace();
           }
       }
   }

   public void generateKeywordsSers(List<String> acceptedKeywords, List<String> acceptedBigrams) {
       File dir = new File(processingPath);

       if (!dir.exists()) {
           throw new RuntimeException("Inexistent Folder: " + dir.getPath());
       }

       File[] files = dir.listFiles((File pathname) -> {
           return pathname.getName().toLowerCase().endsWith(".ser");
       });

       for (File file : files) {
           //LOGGER.info("Processing {} file", file.getName());
           Document d = null;
           try {
               //d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());

               KeywordModeling.determineKeywords(d, false);
               List<Keyword> keywords = d.getTopics();
               StringBuilder sbKeywords = new StringBuilder();
               StringBuilder sbKeywordsBigrams = new StringBuilder();
               StringBuilder sbKeywordsAccepted = new StringBuilder();
               StringBuilder sbKeywordsBigramsAccepted = new StringBuilder();
               StringBuilder sbKeywordsAcceptedAndBigrams = new StringBuilder();
               for (Keyword keyword : keywords) {
                   if (keyword.getElement() instanceof Word) {
                       sbKeywordsBigrams.append(keyword.getWord().getLemma()).append(" ");
                       sbKeywords.append(keyword.getWord().getLemma()).append(" ");
                       if (acceptedKeywords.contains(keyword.getWord().getLemma())) {
                           sbKeywordsBigramsAccepted.append(keyword.getWord().getLemma()).append(" ");
                           sbKeywordsAccepted.append(keyword.getWord().getLemma()).append(" ");
                           sbKeywordsAcceptedAndBigrams.append(keyword.getWord().getLemma()).append(" ");
                       }
                   } else {
                       sbKeywordsBigrams.append(keyword.getElement().toString()).append(" ");
                       sbKeywordsAcceptedAndBigrams.append(keyword.getElement().toString()).append(" ");
                       if (acceptedBigrams.contains(keyword.getElement().toString())) {
                           sbKeywordsBigramsAccepted.append(keyword.getElement().toString()).append(" ");
                       }
                   }
               }
               BufferedWriter bw = null;
               FileWriter fw = null;
               
               fw = new FileWriter(file.getPath().replace(".ser", "_keywords.txt"));
               bw = new BufferedWriter(fw);
               bw.write(sbKeywords.toString());
               sbKeywords.setLength(0);
               bw.close();
               fw.close();
               
               fw = new FileWriter(file.getPath().replace(".ser", "_keywords_bigrams.txt"));
               bw = new BufferedWriter(fw);
               bw.write(sbKeywordsBigrams.toString());
               sbKeywordsBigrams.setLength(0);
               bw.close();
               fw.close();
               
               fw = new FileWriter(file.getPath().replace(".ser", "_keywords_accepted.txt"));
               bw = new BufferedWriter(fw);
               bw.write(sbKeywordsAccepted.toString());
               sbKeywordsAccepted.setLength(0);
               bw.close();
               fw.close();
               
               fw = new FileWriter(file.getPath().replace(".ser", "_keywords_bigrams_accepted.txt"));
               bw = new BufferedWriter(fw);
               bw.write(sbKeywordsBigramsAccepted.toString());
               sbKeywordsBigramsAccepted.setLength(0);
               bw.close();
               fw.close();
               
               fw = new FileWriter(file.getPath().replace(".ser", "_keywords_accepted_and_bigrams.txt"));
               bw = new BufferedWriter(fw);
               bw.write(sbKeywordsAcceptedAndBigrams.toString());
               sbKeywordsAcceptedAndBigrams.setLength(0);
               bw.close();
               fw.close();
               
               /*AbstractDocumentTemplate templateKeywords = AbstractDocumentTemplate.getDocumentModel(sb.toString());
               AbstractDocument documentKeywords = new Document(file.getPath().replace(".ser", ".keywords.ser"), templateKeywords, models, lang, usePOSTagging);
               documentKeywords.save(AbstractDocument.SaveType.SERIALIZED);*/
           } catch (IOException ex) {
               ex.printStackTrace();
           }
       }
   }
   
   public void concatenateCategoryPapers(String path, String catLetter) throws IOException {
	   File dir = new File(path);
	   File[] files = dir.listFiles((File pathname) -> {
           return pathname.getName().toLowerCase().endsWith(".txt") && 
        		   pathname.getName().toLowerCase().startsWith(catLetter);
       });
	   String outFile = path + "/" + catLetter + "aggregated.txt";
	   OutputStream out = new FileOutputStream(outFile);
	   
	   byte[] encoded;
	   for (File file: files) {
		   	try {
		   		encoded = Files.readAllBytes(Paths.get(file.getPath()));
		   		out.write(encoded);
		   		out.write('\n');
		   		out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   out.close();
   }
   
   public void mergeCategoriesKeywords(String path, int noIgnoreLines, int percentage) throws IOException {
	   File dir = new File(path);
	   File[] files = dir.listFiles((File pathname) -> {
           return pathname.getName().toLowerCase().endsWith("-0-keywords_lsa.csv");
       });
	   System.out.println("Se merge-uiesc " + files.length + " fisiere");
	   Map<String, String> types = new HashMap<>();
	   Map<String, String> words = new HashMap<>();
	   Map<String, String> poses = new HashMap<>();
	   Map<String, Double> relevances = new HashMap<>();
	   Map<String, Integer> nrOccurences = new HashMap<>();
	   for (File file: files) {
		   BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
		   String line;
           for (int i = 0; i < noIgnoreLines; i++) br.readLine();
           while ((line = br.readLine()) != null) {
        	   String[] data = line.split(";");
        	   String type = data[0];
        	   String word = data[1];
        	   String lemma = data[2];
        	   String pos = data[3];
        	   String relevance = data[4];
        	   types.put(lemma, type);
        	   words.put(lemma, word);
        	   poses.put(lemma, pos);
        	   if (!relevances.containsKey(lemma)) {
        		   relevances.put(lemma, Double.valueOf(0));
        		   nrOccurences.put(lemma, 1);
        	   }
        	   else {
        		   relevances.put(lemma, relevances.get(lemma) + Double.valueOf(relevance));
        		   nrOccurences.put(lemma, nrOccurences.get(lemma) + 1);
        	   }
           }
	   }
	   System.out.println("In urma merge-ului, se obtin " + relevances.size() + "lemme (incl. bigrame) pornind de la " +
			   	nrOccurences.values().stream().mapToInt(Number::intValue).sum() + " cuvinte");
	   for (String lemma: relevances.keySet())
		   relevances.put(lemma, relevances.get(lemma) / 4);
	   int maxSize = relevances.size() * percentage / 100;
	   System.out.println("Se vor pastra " + percentage + "% din lemme, adica " + maxSize);
	   List<Entry<String, Double>> results = relevances.entrySet().stream()
	   						.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	   						.limit(maxSize).collect(Collectors.toList());
	   System.out.println("S-au pastrat " + results.size() + " lemme, inclusiv bigrame");
	   // Write the merged keywords to an output file
	   BufferedWriter outRelevance = new BufferedWriter(new FileWriter(path + "/" + "lsa_merged_keywords.csv", true));
	   StringBuilder csv = new StringBuilder("SEP=;\ntype;keyword;lemma;pos;relevance\n");
	   int nrLinesWritten = 0;
	   for (Entry<String, Double> line: results) {
		   if (types.get(line.getKey()).toLowerCase().compareTo("ngram") == 0)
			   continue;
		   nrLinesWritten += 1;
		   csv.append(types.get(line.getKey())).append(";");
		   csv.append(words.get(line.getKey())).append(";");
		   csv.append(line.getKey()).append(";");
		   csv.append(poses.get(line.getKey())).append(";");
		   csv.append(line.getValue()).append(";");
		   outRelevance.write(csv.toString());
           outRelevance.newLine();
           outRelevance.flush();
           csv.setLength(0);
	   }
	   outRelevance.close();
	   System.out.println("Au fost scrise " + nrLinesWritten + " keyword-uri, fara bigrame");
   }

   public void processTexts(boolean useSerialized, boolean useBigrams, String outputFileName, String start) {
       File dir = new File(processingPath);

       if (!dir.exists()) {
           throw new RuntimeException("Inexistent Folder: " + dir.getPath());
       }

       //List<Document> documents = new ArrayList<>();
       Set<Keyword> keywords = new TreeSet<>();
       ExportDocument exportDoc = new ExportDocument();

       if (useSerialized) {
//           File[] files = dir.listFiles((File pathname) -> {
//               return pathname.getName().toLowerCase().endsWith(".ser");
//           });
//
//           for (File file : files) {
//               Document d = null;
//               try {
//                   d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
//                   documents.add(d);
//                   d.exportDocument();
//               } catch (IOException | ClassNotFoundException ex) {
//                   ex.printStackTrace();
//               }
//           }
       } else {
           File[] files = dir.listFiles((File pathname) -> {
               return pathname.getName().toLowerCase().endsWith("aggregated.txt") && 
            		   pathname.getName().toLowerCase().startsWith(start);
           });
           
           List<Annotators> annotators = new ArrayList<>();
           annotators.add(Annotators.NLP_PREPROCESSING);
           if (useBigrams)
        	   annotators.add(Annotators.USE_BIGRAMS);
           DocumentProcessingPipeline dpp = new DocumentProcessingPipeline(this.lang, this.models, annotators);

           for (File file : files) {
               LOGGER.info("Processing {} file", file.getName());
               // Create file

               Document d;
               try {
                   if (meta) {
                	   LOGGER.error("Meta not supported");
                	   return;
                   } 
                   
                   byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
                   String text = new String(encoded, Charset.defaultCharset());
                   
                   AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(text);
                   d = dpp.createDocumentFromTemplate(docTmp);
                   
                   
                   //d = dpp.createDocumentFromXML(file.getPath());
                   d.setPath(file.getPath());
                   
                   KeywordModeling.determineKeywords(d, useBigrams);
                   //exportDoc.exportSerializedDocument(d);
                   addTopKeywords(keywords, d, noTopKeyWords);
                   //documents.add(d);
               } catch (Exception e) {
                   LOGGER.error("Runtime error while processing {}: {} ...", new Object[]{file.getName(), e.getMessage()});
                   e.printStackTrace();
               }
           }
       }

       //determing joint keywords
       //Set<Keyword> keywords = getTopKeywords(documents, noTopKeyWords);

       try (BufferedWriter outRelevance = new BufferedWriter(new FileWriter(processingPath + "/" + outputFileName, true))) {
           StringBuilder csv = new StringBuilder("SEP=;\ntype;keyword;lemma;pos;relevance\n");
           for (Keyword keyword : keywords) {
               if (keyword.getElement() instanceof Word) {
                   csv.append("word;").append(keyword.getWord().getText()).append(";");
               } else if (keyword.getElement() instanceof NGram) {
                   NGram nGram = (NGram) keyword.getElement();
                   csv.append("ngram;");
                   for (Word w : nGram.getWords()) {
                       csv.append(w.getText()).append(" ");
                   }
                   csv.append(";");
               }
               csv.append(keyword.getWord().getLemma()).append(";");
               if (keyword.getElement() instanceof Word) {
                   csv.append(keyword.getWord().getPOS());
               } else {
                   NGram nGram = (NGram) keyword.getElement();
                   StringBuilder sb = new StringBuilder();
                   for (Word word : nGram.getWords()) {
                       sb.append(word.getPOS()).append("_");
                   }
                   String nGramLemmas = sb.toString();
                   sb.setLength(0);
                   csv.append(nGramLemmas.substring(0, nGramLemmas.length() - 1));
               }
               csv.append(";").append(keyword.getRelevance());
               outRelevance.write(csv.toString());
               outRelevance.newLine();
               outRelevance.flush();
               csv.setLength(0);
           }
           outRelevance.close();
       } catch (IOException ex) {
           LOGGER.error("Runtime error while analyzing selected folder ...");
           ex.printStackTrace();
       }
   }

   public static void main(String[] args) throws IOException {
	   List<SemanticModel> models = new ArrayList<>();
       SemanticModel lsa = SemanticModel.loadModel("TASA", Lang.en, SimilarityType.LSA);
       SemanticModel w2vec = SemanticModel.loadModel("TASA", Lang.en, SimilarityType.WORD2VEC);
       
       models.add(lsa);
       models.add(w2vec);
  
       KeywordMining keywordMining = new KeywordMining("fulltexts", 0, models, Lang.en, true, false, false);

       // false, true, output_file, input_file_startsWith
       keywordMining.processTexts(false, true, "j-0-keywords_lsa.csv", "j-");
       //keywordMining.mergeCategoriesKeywords("fulltexts", 2, 30);
   }
}

