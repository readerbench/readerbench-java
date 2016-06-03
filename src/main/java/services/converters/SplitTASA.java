package services.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class SplitTASA {
	static Logger logger = Logger.getLogger(SplitTASA.class);

	public static final String[] TASA_GENRES = { "SocialStudies", "LanguageArts", "Science", "Health", "HomeEconomics",
			"IndustrialArts", "Business", "Miscellaneous", "Unmarked" };
	public static final int LOWER_BOUND = 50;
	public static final int NO_DOCS_PER_GRADE_LEVEL = 50;
	public static final int NO_GRADE_LEVELS = 13;

	public static void parseTasaFromSingleFile(String input, String path) throws FileNotFoundException, IOException {
		createFolders(path);
		// determine number of documents
		List<GenericTasaDocument> docs = getTASAdocs(input, path);

		// determine also number of unique short doc IDs
		Map<String, List<GenericTasaDocument>> uniqueShortIDs = new TreeMap<String, List<GenericTasaDocument>>();

		// create unique categories for docs with 2+ paragraphs
		for (GenericTasaDocument doc : docs) {
			if (doc.getNoParagraphs() > 1) {
				String categoryID = doc.getID().substring(0, doc.getID().indexOf("."));
				if (!uniqueShortIDs.containsKey(categoryID)) {
					uniqueShortIDs.put(categoryID, new ArrayList<GenericTasaDocument>());
				}
				uniqueShortIDs.get(categoryID).add(doc);
			}
		}

		logger.info("Writing all categories of files");

		// write all files
		int no = 0, no2 = 0, no2unique = 0, no2unique2 = 0;
		for (GenericTasaDocument doc : docs) {
			doc.writeContent(path + "/tasa");
			doc.writeTxt(path + "/tasa");
			no++;
			if (doc.getNoParagraphs() > 1) {
				doc.writeContent(path + "/tasa(2+)");
				doc.writeTxt(path + "/tasa(2+)");
				no2++;
			}
		}

		// sort categories
		for (List<GenericTasaDocument> docsToWrite : uniqueShortIDs.values()) {
			Collections.sort(docsToWrite);
			// write first doc
			logger.info("Writing " + docsToWrite.get(0).getID() + " (" + docsToWrite.get(0).getContent().length()
					+ " chars)");

			docsToWrite.get(0).writeContent(path + "/tasa(2+)unique");
			docsToWrite.get(0).writeTxt(path + "/tasa(2+)unique");
			no2unique++;

			docsToWrite.get(0).writeContent(path + "/tasa(2+)unique(2)");
			docsToWrite.get(0).writeTxt(path + "/tasa(2+)unique(2)");
			no2unique2++;

			if (docsToWrite.size() > 1) {
				logger.info("Writing " + docsToWrite.get(1).getID() + " (" + docsToWrite.get(1).getContent().length()
						+ " chars)");
				docsToWrite.get(1).writeContent(path + "/tasa(2+)unique(2)");
				docsToWrite.get(1).writeTxt(path + "/tasa(2+)unique(2)");
				no2unique2++;
			}
			logger.info("Last document in the series - " + docsToWrite.get(docsToWrite.size() - 1).getID() + " - has "
					+ docsToWrite.get(docsToWrite.size() - 1).getContent().length() + " chars");
		}

		logger.info(no + " individual TASA files have been written");
		logger.info(no2 + " individual TASA files with 2+ paragraphs have been written");
		logger.info(no2unique
				+ " individual TASA files with unique ID, 2+ paragraphs and longest content have been written");
		logger.info(no2unique2
				+ " individual TASA files with 2+ paragraphs and 2 longest content per unique ID have been written");
	}

	/**
	 * @param input
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static List<GenericTasaDocument> getTASAdocs(String input, String path)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		String corpus_path = path + "/" + input;
		FileInputStream inputFile = new FileInputStream(corpus_path);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";
		String content = "";
		int total_docs_to_process = 0;
		while ((line = in.readLine()) != null) {
			if (line.startsWith("<ID")) {
				total_docs_to_process++;
			}
		}
		in.close();
		logger.info("Processing " + total_docs_to_process + " documents in total");

		// read corpus
		in = new BufferedReader(new FileReader(corpus_path));
		String ID = "";
		double DRP = 0;
		String genre = "";
		int noParagraphs = 0;

		// create set of documents from individual file
		List<GenericTasaDocument> docs = new ArrayList<GenericTasaDocument>();

		while ((line = in.readLine()) != null) {
			if (line.startsWith("<ID")) {
				// dump existing content as a new document
				checkDoc(content, ID, DRP, genre, noParagraphs, docs);

				// flush content
				content = "";
				// extract useful info
				StringTokenizer st = new StringTokenizer(line, "\"");
				while (st.hasMoreTokens()) {
					String currentTag = st.nextToken();
					if (currentTag.contains("<ID=")) {
						ID = st.nextToken().trim();
					} else if (currentTag.contains("DRP=")) {
						DRP = Double.valueOf(st.nextToken().trim());
					} else if (currentTag.contains("P=")) {
						noParagraphs = Integer.valueOf(st.nextToken().trim());
					} else {
						genre = null;
						for (String potentialGenre : TASA_GENRES) {
							if (currentTag.contains(potentialGenre + "="))
								genre = potentialGenre;
						}
						if (genre == null) {
							genre = "Unmarked";
						}
						// all information has been extracted
						break;
					}
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
		checkDoc(content, ID, DRP, genre, noParagraphs, docs);
		in.close();
		return docs;
	}

	/**
	 * @param path
	 */
	private static void createFolders(String path) {
		String[] folders = { "tasa", "tasa(2+)", "tasa(2+)unique", "tasa(2+)unique(2)" };

		for (String folder : folders) {
			File dir = new File(path + "/" + folder);
			if (!dir.exists()) {
				dir.mkdir();
			}
			// see if there are folders with genre names
			for (String g : TASA_GENRES) {
				dir = new File(path + "/" + folder + "/" + g);
				if (dir.exists()) {
					for (File f : dir.listFiles())
						f.delete();
					dir.delete();
				}
				dir.mkdir();
			}
		}

		for (String folder : folders) {
			// delete all potential class folders as well
			for (int i = 1; i <= NO_GRADE_LEVELS; i++) {
				File dir = new File(path + "/" + folder + "/grade" + i);
				if (dir.exists()) {
					for (File f : dir.listFiles())
						f.delete();
					dir.delete();
				}
			}
		}
	}

	public static void parseTasaEquitableDistribution(String input, String path)
			throws FileNotFoundException, IOException {
		// determine number of documents
		String corpus_path = path + "/" + input;
		FileInputStream inputFile = new FileInputStream(corpus_path);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";
		String content = "";
		int total_docs_to_process = 0;
		while ((line = in.readLine()) != null) {
			if (line.startsWith("<ID")) {
				total_docs_to_process++;
			}
		}
		in.close();
		logger.info("Processing " + total_docs_to_process + " documents in total");

		// read corpus
		in = new BufferedReader(new FileReader(corpus_path));
		String ID = "";
		double DRP = 0;
		String[] genres = { "SocialStudies", "LanguageArts", "Science" };
		String folder = "tasa_equitable";
		String genre = "";
		int noParagraphs = 0;

		File dir = new File(path + "/" + folder);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// see if there are folders with genre names
		for (String g : genres) {
			dir = new File(path + "/" + folder + "/" + g);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					f.delete();
				dir.delete();
			}
			dir.mkdir();
		}

		// delete all potential class folders as well
		for (int i = 1; i <= NO_GRADE_LEVELS; i++) {
			dir = new File(path + "/" + folder + "/grade" + i);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					f.delete();
				dir.delete();
			}
		}

		// create set of documents from individual file
		List<GenericTasaDocument> docs = new ArrayList<GenericTasaDocument>();

		while ((line = in.readLine()) != null) {
			if (line.startsWith("<ID")) {
				// dump existing content as a new document
				checkDoc(content, ID, DRP, genre, noParagraphs, docs);

				// flush content
				content = "";
				// extract useful info
				StringTokenizer st = new StringTokenizer(line, "\"");
				while (st.hasMoreTokens()) {
					String currentTag = st.nextToken();
					if (currentTag.contains("<ID=")) {
						ID = st.nextToken().trim();
					} else if (currentTag.contains("DRP=")) {
						DRP = Double.valueOf(st.nextToken().trim());
					} else if (currentTag.contains("P=")) {
						noParagraphs = Integer.valueOf(st.nextToken().trim());
					} else {
						genre = null;
						for (String potentialGenre : genres) {
							if (currentTag.contains(potentialGenre + "="))
								genre = potentialGenre;
						}
						if (genre == null) {
							genre = "Unmarked";
						}
						// all information has been extracted
						break;
					}
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
		checkDoc(content, ID, DRP, genre, noParagraphs, docs);
		in.close();

		// determine also number of unique short doc IDs
		Map<String, Map<String, List<GenericTasaDocument>>> uniqueShortIDs = new TreeMap<String, Map<String, List<GenericTasaDocument>>>();

		for (String g : genres) {
			uniqueShortIDs.put(g, new TreeMap<String, List<GenericTasaDocument>>());
		}

		// create unique categories for docs with 2+ paragraphs
		for (GenericTasaDocument doc : docs) {
			if (doc.getNoParagraphs() > 1) {
				String categoryID = doc.getID().substring(0, doc.getID().indexOf("."));
				if (!uniqueShortIDs.containsKey(categoryID)) {
					uniqueShortIDs.get(doc.getGenre()).put(categoryID, new ArrayList<GenericTasaDocument>());
				}
				uniqueShortIDs.get(doc.getGenre()).get(categoryID).add(doc);
			}
		}

		logger.info("Writing all categories of files");

		// write all files
		Map<String, Map<Integer, List<GenericTasaDocument>>> docsToWrite = new TreeMap<String, Map<Integer, List<GenericTasaDocument>>>();
		for (String g : genres) {
			Map<Integer, List<GenericTasaDocument>> docsGenreToWrite = new TreeMap<Integer, List<GenericTasaDocument>>();
			for (int i = 1; i <= NO_GRADE_LEVELS; i++)
				docsGenreToWrite.put(i, new ArrayList<GenericTasaDocument>());
			docsToWrite.put(g, docsGenreToWrite);
		}

		// sort categories
		for (String g : genres) {
			for (List<GenericTasaDocument> potentialDocs : uniqueShortIDs.get(g).values()) {
				if (potentialDocs.size() > 0) {
					Collections.sort(potentialDocs);
					// add first doc for writing
					docsToWrite.get(g).get(GenericTasaDocument.get13GradeLevel(potentialDocs.get(0).getDRPscore()))
							.add(potentialDocs.get(0));
					// remove first doc from the entire list
					docs.remove(potentialDocs.get(0));
				}
			}
		}

		// create a random permutation of the documents vector
		List<Integer> index = new ArrayList<Integer>();
		for (int i = 0; i < docs.size(); i++) {
			index.add(i);
		}

		for (int permutationIndex = 0; permutationIndex < 1000000; permutationIndex++) {
			int i = (int) (Math.random() * docs.size());
			int j = (int) (Math.random() * docs.size());

			int aux = index.get(i);
			index.set(i, index.get(j));
			index.set(j, aux);
		}

		// add the documents to corresponding bin in the permutated manner
		for (int i = 0; i < index.size(); i++) {
			GenericTasaDocument d = docs.get(index.get(i));
			docsToWrite.get(d.getGenre()).get(GenericTasaDocument.get13GradeLevel(d.getDRPscore())).add(d);
		}

		// write first representative documents
		for (String g : genres) {
			for (int i = 1; i <= NO_GRADE_LEVELS; i++) {
				int no = 0;
				for (int j = 0; j < Math.min(NO_DOCS_PER_GRADE_LEVEL, docsToWrite.get(g).get(i).size()); j++) {
					GenericTasaDocument d = docsToWrite.get(g).get(i).get(j);
					logger.info("Writing " + d.getID() + " (" + d.getContent().length() + " chars)");
					d.writeContent(path + "/" + folder);
					d.writeTxt(path + "/" + folder);
					no++;
				}
				logger.info(no + " individual TASA files with 2+ paragraphs and an equitable distribution of "
						+ NO_DOCS_PER_GRADE_LEVEL + " files per " + g + " genre and complexity class " + i
						+ " have been written");
			}
		}

	}

	private static void checkDoc(String content, String ID, double DRP, String genre, int noParagraphs,
			List<GenericTasaDocument> docs) throws UnsupportedEncodingException {
		content = content.trim();
		if (!genre.equals("Unmarked") && content.trim().length() > 0
				&& GenericTasaDocument.get13GradeLevel(DRP) != -1) {
			// create new temporary object
			GenericTasaDocument tmpDoc = new GenericTasaDocument(ID, DRP, noParagraphs, content, genre);
			GenericTasaDocument existingDoc = null;
			for (GenericTasaDocument doc : docs) {
				if (doc.getID().equals(tmpDoc.getID())) {
					logger.warn("Duplicate identifier " + doc.getID());
				}
				if (doc.getContent().contains(tmpDoc.getContent()) || tmpDoc.getContent().contains(doc.getContent())) {
					logger.warn("Duplicate content " + doc.getID() + " and " + tmpDoc.getID());
					existingDoc = doc;
					break;
				}
			}
			// add a new element
			if (existingDoc == null) {
				docs.add(tmpDoc);
			}
			// update the doc list if appropriate
			if (existingDoc != null && tmpDoc.getContent().length() > existingDoc.getContent().length()) {
				docs.remove(existingDoc);
				docs.add(tmpDoc);
			}
		}
	}

	public static void parseTasa(String path) {
		// determine number of documents
		int total_docs_to_process = 0;
		for (File dir : (new File(path)).listFiles()) {
			if (dir.isDirectory())
				total_docs_to_process += dir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						if (pathname.getName().endsWith(".txt"))
							return true;
						return false;
					}
				}).length;
		}
		logger.info("Processing " + total_docs_to_process + " documents in total");

		int current_doc_to_process = 0;

		for (File dir : (new File(path)).listFiles()) {
			if (dir.isDirectory()) {
				// determine all txt files
				File[] listFiles = dir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						if (pathname.getName().endsWith(".txt"))
							return true;
						return false;
					}
				});
				String line = "";
				for (File f : listFiles) {
					// process each file
					String content = "";
					String filename = f.getName();
					String id = filename.substring(0, filename.indexOf("-DRP-")).trim();
					Double DRPscore = Double.valueOf(
							filename.substring(filename.indexOf("-DRP-") + 5, filename.indexOf(".txt")).trim());
					FileInputStream inputFile;
					InputStreamReader ir;
					try {
						inputFile = new FileInputStream(f);
						ir = new InputStreamReader(inputFile, "UTF-8");
						BufferedReader in = new BufferedReader(ir);
						while ((line = in.readLine()) != null) {
							content += line + "\n";
						}
						if ((++current_doc_to_process) % 1000 == 0)
							logger.info("Finished processing " + (current_doc_to_process) + " documents of "
									+ total_docs_to_process);
						GenericTasaDocument tmpDoc = new GenericTasaDocument(id, DRPscore, 0, content, null);
						tmpDoc.writeContent(path);
						in.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		// SplitTASA.parseTasa("/Users/mihaidascalu/SeparateTasa");

		try {
			SplitTASA.parseTasaFromSingleFile("tasa.txt", "/Users/mihaidascalu/Documents/Corpora/TASA");
			// SplitTASA.parseTasaEquitableDistribution("tasa.txt",
			// "/Users/mihaidascalu/Corpora/TASA");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
