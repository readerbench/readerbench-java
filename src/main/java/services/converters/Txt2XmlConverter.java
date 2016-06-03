package services.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import data.Lang;

public class Txt2XmlConverter {
	static Logger logger = Logger.getLogger(Txt2XmlConverter.class);

	public static void processContent(String title, String content, Lang lang, String path) {
		// perform processing and save the new document
		StringTokenizer st = new StringTokenizer(content, "\n");
		int crtBlock = 0;
		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
		while (st.hasMoreTokens()) {
			BlockTemplate block = docTmp.new BlockTemplate();
			block.setId(crtBlock++);
			block.setContent(st.nextToken().trim());
			docTmp.getBlocks().add(block);
		}
		Document d = new Document(null, docTmp, null, null, lang, false, false);
		d.setTitleText(title);
		d.setDate(new Date());

		d.exportXML(path);
	}

	public static void parseTxtFiles(String prefix, String path, Lang lang, String encoding) {
		// determine number of documents
		if (!new File(path).isDirectory())
			return;
		int total_docs_to_process = 0;
		total_docs_to_process = new File(path).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".txt"))
					return true;
				return false;
			}
		}).length;
		logger.info("Processing " + total_docs_to_process + " documents in total");

		int current_doc_to_process = 0;

		// determine all txt files
		File[] listFiles = new File(path).listFiles(new FileFilter() {
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
			FileInputStream inputFile;
			InputStreamReader ir;
			try {
				inputFile = new FileInputStream(f);
				ir = new InputStreamReader(inputFile, encoding);
				BufferedReader in = new BufferedReader(ir);
				while ((line = in.readLine()) != null) {
					if (line.trim().length() > 0)
						content += line.trim() + "\n";
				}
				if ((++current_doc_to_process) % 1000 == 0)
					logger.info("Finished processing " + (current_doc_to_process) + " documents of "
							+ total_docs_to_process);
				processContent(prefix + f.getName().replaceAll("\\.txt", ""),
						new String(content.getBytes("UTF-8"), "UTF-8"), lang, f.getPath().replace(".txt", ".xml"));
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("Finished processing all files.");
	}

	public static void parseMergedTxtFiles(String path, Lang lang, String encoding) {
		// determine number of documents
		if (!new File(path).isDirectory())
			return;

		// determine all txt files
		File[] listFiles = new File(path).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".txt"))
					return true;
				return false;
			}
		});

		for (File f : listFiles) {
			logger.info("Processing " + f.getPath());
			// see if there are folders with genre names
			File dir = new File(path + "/" + f.getName().replaceAll("\\.txt", ""));
			if (dir.exists()) {
				for (File tmp : dir.listFiles())
					tmp.delete();
				dir.delete();
			}
			dir.mkdir();

			// process each file
			String content = "";
			String title = "";
			String line = "";
			FileInputStream inputFile;
			InputStreamReader ir;
			Pattern p = Pattern.compile("^[0-9]+");

			try {
				inputFile = new FileInputStream(f);
				ir = new InputStreamReader(inputFile, encoding);
				BufferedReader in = new BufferedReader(ir);
				while ((line = in.readLine()) != null) {
					if (line.trim().length() > 0) {
						Matcher m = p.matcher(line);
						if (m.find()) {
							// flush previous content
							if (content.length() > 0) {
								String destination = dir.getPath() + "/" + title + ".xml";
								processContent(title.replaceAll("[0-9]", "").trim(),
										new String(content.getBytes("UTF-8"), "UTF-8"), lang, destination);
							}
							title = line.trim();
						} else {
							content += line.trim() + "\n";
						}
					}
				}
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("Finished processing all files.");
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		// Txt2XmlConverter.parseTxtFiles("Think aloud ",
		// "in/SEvsTA/Class1/TA", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Self-explanation ",
		// "in/SEvsTA/Class1/SE", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Competition essay - ",
		// "in/essays/competition_en/texts", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("FYP essay - ",
		// "in/essays/essays_FYP_en/texts", Lang.eng,
		// "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Images essay - ",
		// "in/essays/images_en/texts", Lang.eng,
		// "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Competition essay - ",
		// "in/essays/competition_en/texts", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("DC 2009 essay - ",
		// "in/essays/DC_essays_2009_en/texts", Lang.eng, "windows-1250");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed essay - ",
		// "in/essays/msu_timed_en/texts", Lang.eng, "windows-1250");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed essay - ",
		// "in/essays/posttest_fall_2009/texts", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed essay - ",
		// "in/essays/pretest_spring_2010/texts", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Familiarity essay - ", "in/texts 2
		// for familiarity", Lang.eng, "UTF-8");
		// Txt2XmlConverter.parseMergedTxtFiles("resources/in/essays/chaprou_fr",
		// Lang.fr, "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Eminescu - ", "resources/in/Eminescu
		// vs Bratianu/Eminescu 1877 - 1880", Lang.ro,
		// "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Bratianu - ", "resources/in/Eminescu
		// vs Bratianu/Bratianu 1857 - 1875", Lang.ro,
		// "UTF-8");
		Txt2XmlConverter.parseTxtFiles("Comenius main letter collection - ", "resources/in/comenius_la/01", Lang.la,
				"UTF-8");
		Txt2XmlConverter.parseTxtFiles("Comenius letters to Hartlibians - ", "resources/in/comenius_la/02", Lang.la,
				"UTF-8");
		Txt2XmlConverter.parseTxtFiles("Comenius letters to German didacticians - ", "resources/in/comenius_la/03",
				Lang.la, "UTF-8");
		Txt2XmlConverter.parseTxtFiles("Comenius letters to Gdansk circle - ", "resources/in/comenius_la/04", Lang.la,
				"UTF-8");
	}
}
