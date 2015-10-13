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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import services.commons.TextPreprocessing;
import services.semanticModels.LSA.LSA;
import DAO.AbstractDocument;
import DAO.AbstractDocumentTemplate;
import DAO.AbstractDocumentTemplate.BlockTemplate;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class PreProcessing {
	static Logger logger = Logger.getLogger(PreProcessing.class);

	private String parseDocumentProcessing(AbstractDocument d, int noMinWordPerDoc) {
		// returns new entries to write
		String toWrite = "";
		List<Word> document = new LinkedList<Word>();
		int no_words_to_write = 0;
		for (Block b : d.getBlocks()) {
			// combine with previous paragraph, if the case
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getWords()) {
					no_words_to_write++;
					document.add(w);
				}
				document.add(new Word(".", ".", ".", null, null, d.getLanguage()));
			}
			if (no_words_to_write >= noMinWordPerDoc) {
				// flush the actual contents of the document
				for (Word w : document) {
					toWrite += w.getLemma() + " ";
				}
				toWrite += "\n";
				document.clear();
				no_words_to_write = 0;
			}
		}
		return toWrite;
	}

	public String processContent(String content, Lang lang, boolean usePOSTagging, int noMinWordPerDoc) {
		AbstractDocumentTemplate docTmp = getDocumentModel(content);
		// perform processing
		AbstractDocument d = new Document(null, docTmp, null, null, lang, usePOSTagging, true);
		return parseDocumentProcessing(d, noMinWordPerDoc);
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

	public void parseGeneralCorpus(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
			throws FileNotFoundException, IOException {
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
			BufferedReader in = new BufferedReader(new FileReader(f));

			String line = "";
			String toWrite = "";

			while ((line = in.readLine()) != null) {
				if (line.length() > LSA.LOWER_BOUND) {
					// toWrite = processContent(
					// TextPreprocessing.replaceFrCorpusAdnotations(StringEscapeUtils.escapeXml(line)),
					// lang, usePOSTagging, noMinWordPerDoc);
					toWrite = processContent(TextPreprocessing.replaceFrCorpusAdnotations(line, lang), lang,
							usePOSTagging, noMinWordPerDoc);

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

		out.close();
		logger.info("Finished all pre-processing");
	}

	public void parseTasa(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
			throws FileNotFoundException, IOException {
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
						toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc);
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
				toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc);
				if (toWrite.length() > 0) {
					out.write(toWrite);
				}
			}
			in.close();
		}
		out.close();
		logger.info("Finished all pre-processing");
	}

	public void parseCOCA(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
			throws FileNotFoundException, IOException {
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

					toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc);

					if (toWrite.length() > 0)
						out.write(toWrite + "\n");
					if ((++current_doc_to_process) % 1000 == 0)
						logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
								+ total_docs_to_process);
				}
			}

			in.close();
		}
		out.close();
		logger.info("Finished all pre-processing");
	}
}
