package services.semanticModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Block;
import data.Lang;
import data.Sentence;
import data.Word;
import data.document.Document;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import services.commons.TextPreprocessing;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.listOfWords.Dictionary;
import services.semanticModels.LSA.LSA;

public class PreProcessing {
	static Logger logger = Logger.getLogger(PreProcessing.class);

	public static final int MIN_NO_OCCURRENCES = 5;

	private Map<String, Integer> newConcepts = new TreeMap<>();

	private String parseDocumentProcessing(AbstractDocument d, int noMinWordPerDoc, boolean includeWordAssociations) {
		// returns new entries to write
		StringBuilder toWrite = new StringBuilder();
		List<Word> document = new ArrayList<>();
		int no_words_to_write = 0;
		for (Block b : d.getBlocks()) {
			// combine with previous paragraph, if the case
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getWords()) {
					no_words_to_write++;
					document.add(w);
				}
				// include semantic graph dependencies as word associations
				SemanticGraph dependencies = s.getDependencies();
				if (includeWordAssociations && dependencies != null) {
					for (SemanticGraphEdge edge : dependencies.edgeListSorted()) {
						String dependent = edge.getDependent().word().toLowerCase();
						Word w1 = Word.getWordFromConcept(dependent, d.getLanguage());
						w1.setLemma(StaticLemmatizer.lemmaStatic(dependent, d.getLanguage()));

						String governor = edge.getGovernor().word().toLowerCase();
						Word w2 = Word.getWordFromConcept(governor, d.getLanguage());
						w2.setLemma(StaticLemmatizer.lemmaStatic(governor, d.getLanguage()));
						// GrammaticalRelation relation = edge.getRelation();
						if (s.getWords().contains(w1) && s.getWords().contains(w2)
								&& !w1.getLemma().equals(w2.getLemma())) {
							String association = w1.getLemma() + Word.WORD_ASSOCIATION + w2.getLemma();
							document.add(new Word(association, association, association, null, null, d.getLanguage()));
						}
					}
				}

				document.add(new Word(".", ".", ".", null, null, d.getLanguage()));
			}
			if (no_words_to_write >= noMinWordPerDoc) {
				// flush the actual contents of the document
				for (Word w : document) {
					toWrite.append(w.getLemma()).append(" ");
				}
				toWrite.append("\n");
				document.clear();
				no_words_to_write = 0;
			}
		}
		return toWrite.toString();
	}

	public String processContent(String content, Lang lang, boolean usePOSTagging, int noMinWordPerDoc,
			boolean includeWordAssociations) {
		String text = TextPreprocessing.cleanText(content, lang);
		AbstractDocumentTemplate docTmp = getDocumentModel(text);

		StringTokenizer st = new StringTokenizer(text, " \\.,:;!?-+[](){}'’“”\"");

		// determine new concepts
		while (st.hasMoreTokens()) {
			String word = st.nextToken().trim();
			if (word.length() > 0 && !Dictionary.getDictionaryWords(lang).contains(word)) {
				if (newConcepts.containsKey(word))
					newConcepts.put(word, newConcepts.get(word) + 1);
				else
					newConcepts.put(word, 1);
			}
		}
		// perform processing
		AbstractDocument d = new Document(null, docTmp, null, null, lang, usePOSTagging, true);
		return parseDocumentProcessing(d, noMinWordPerDoc, includeWordAssociations);
	}

	/**
	 * @param content
	 * @return
	 */
	protected AbstractDocumentTemplate getDocumentModel(String content) {
		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
		BlockTemplate block = docTmp.new BlockTemplate();
		block.setId(0);
		block.setContent(content.trim().toLowerCase());
		docTmp.getBlocks().add(block);
		return docTmp;
	}

	private void printNewConcepts() {
		// write new concepts
		for (Entry<String, Integer> entry : newConcepts.entrySet()) {
			if (entry.getValue() >= MIN_NO_OCCURRENCES)
				System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	public void parseGeneralCorpus(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc,
			boolean includeWordAssociations) throws FileNotFoundException, IOException {
		// determine number of documents

		if (!new File(path).isDirectory()) {
			return;
		}
		File[] filesToProcess = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt"));

		int total_docs_to_process = 0;
		int current_doc_to_process = 0;

		for (File f : filesToProcess) {
			FileInputStream inputFile = new FileInputStream(f);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line = "";

			while ((line = in.readLine()) != null) {
				if (line.length() > LSA.LOWER_BOUND) {
					total_docs_to_process++;
				}
			}
			in.close();
		}
		logger.info("Processing " + total_docs_to_process + " documents.");

		// read corpus
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
				32768);
		for (File f : filesToProcess) {
			logger.info("Processing file: " + f.getName());
			BufferedReader in = new BufferedReader(new FileReader(f));

			String line = "";
			String toWrite = "";

			while ((line = in.readLine()) != null) {
				if (line.length() > LSA.LOWER_BOUND) {
					// toWrite = processContent(
					// TextPreprocessing.replaceFrCorpusAdnotations(StringEscapeUtils.escapeXml(line)),
					// lang, usePOSTagging, noMinWordPerDoc);
					toWrite = processContent(line, lang,
							usePOSTagging, noMinWordPerDoc, includeWordAssociations);

					if (toWrite.length() > 0)
						out.write(toWrite + "\n");
					if ((++current_doc_to_process) % 1000 == 0)
						logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
								+ total_docs_to_process);
				}
			}
			in.close();
			logger.info("Finished pre-processing " + f.getName());
		}
		printNewConcepts();

		out.close();
		logger.info("Finished all pre-processing");
	}

	public void parseTasa(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc,
			boolean includeWordAssociations) throws FileNotFoundException, IOException {
		// determine number of documents

		if (!new File(path).isDirectory()) {
			return;
		}
		File[] filesToProcess = new File(path).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".txt");
			}
		});

		int total_docs_to_process = 0;
		int current_doc_to_process = 0;

		for (File f : filesToProcess) {
			// determine number of documents
			FileInputStream inputFile = new FileInputStream(f);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line = "";

			while ((line = in.readLine()) != null) {
				if (line.startsWith("<ID") || line.startsWith("<text")) {
					total_docs_to_process++;
				}
			}
			in.close();
		}
		logger.info("Processing " + total_docs_to_process + " documents.");

		// read corpus
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
				32768);

		for (File f : filesToProcess) {

			// read corpus
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			String line = "";
			String toWrite = "";
			String content = "";

			while ((line = in.readLine()) != null) {
				if (line.length() == 0 || line.startsWith("<")) {
					if (content.length() > LSA.LOWER_BOUND) {
						toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc,
								includeWordAssociations);
						if (toWrite.length() > 0)
							out.write(toWrite + "\n");
						// flush content
						content = "";
						if ((++current_doc_to_process) % 1000 == 0)
							logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
									+ total_docs_to_process);
					}
				} else {
					// process content
					if (line.length() > 0) {
						if (line.startsWith("  "))
							content += "\n" + line.trim();
						else
							content += " " + line.trim();
					}
				}
			}
			if (content.length() > LSA.LOWER_BOUND) {
				logger.info("Processing last block document " + (++current_doc_to_process) + " of "
						+ total_docs_to_process);
				toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc, includeWordAssociations);
				if (toWrite.length() > 0) {
					out.write(toWrite);
				}
			}
			printNewConcepts();
			in.close();
		}
		out.close();
		logger.info("Finished all pre-processing");
	}

	public void parseCOCA(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc,
			boolean includeWordAssociations) throws FileNotFoundException, IOException {
		// determine number of documents

		if (!new File(path).isDirectory()) {
			return;
		}
		File[] filesToProcess = new File(path).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".txt");
			}
		});

		int total_docs_to_process = 0;
		int current_doc_to_process = 0;

		for (File f : filesToProcess) {
			// determine number of documents
			FileInputStream inputFile = new FileInputStream(f);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line = "";

			while ((line = in.readLine()) != null) {
				if (line.startsWith("##")) {
					total_docs_to_process++;
				}
			}
			in.close();
		}
		logger.info("Processing " + total_docs_to_process + " documents.");

		// read corpus
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
				32768);

		for (File f : filesToProcess) {
			// read corpus
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			String line = "";
			String toWrite = "";

			while ((line = in.readLine()) != null) {
				if (line.length() > LSA.LOWER_BOUND) {
					line = line.replaceAll("##[0-9]* ]", "");
					line = line.replaceAll("<p>", "\n");
					line = line.replaceAll(" // ", "\n");
					line = line.replaceAll(" # ", "\n");
					line = line.replaceAll("@ @ @ @ @ @ @ @ @ @", " ");

					toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc, includeWordAssociations);

					if (toWrite.length() > 0)
						out.write(toWrite + "\n");
					if ((++current_doc_to_process) % 1000 == 0)
						logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
								+ total_docs_to_process);
				}
			}
			printNewConcepts();
			in.close();
		}
		out.close();
		logger.info("Finished all pre-processing");
	}
}
