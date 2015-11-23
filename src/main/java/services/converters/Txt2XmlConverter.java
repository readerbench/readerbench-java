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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class Txt2XmlConverter {
	static Logger logger = Logger.getLogger(Txt2XmlConverter.class);

	public static void processContent(String title, String content, String path) {
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
		Document d = new Document(null, docTmp, null, null, Lang.eng, false, false);
		d.setTitleText(title);
		d.setDate(new Date());

		d.exportXML(path);
	}

	public static void parseTxtFiles(String name, String path, String encoding) {
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
				processContent(name + " essay - " + f.getName().replaceAll("\\.txt", ""),
						new String(content.getBytes("UTF-8"), "UTF-8"), f.getPath().replace(".txt", ".xml"));
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
		// Txt2XmlConverter.parseTxtFiles("Think aloud",
		// "in/SEvsTA/Class1/TA","UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Self-explanation",
		// "in/SEvsTA/Class1/SE","UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Competition",
		// "in/essays/competition_en/texts", "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("FYP",
		// "in/essays/essays_FYP_en/texts",
		// "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Images", "in/essays/images_en/texts",
		// "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("Competition",
		// "in/essays/competition_en/texts", "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("DC 2009",
		// "in/essays/DC_essays_2009_en/texts", "windows-1250");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed",
		// "in/essays/msu_timed_en/texts", "windows-1250");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed",
		// "in/essays/posttest_fall_2009/texts", "UTF-8");
		// Txt2XmlConverter.parseTxtFiles("MSU Timed",
		// "in/essays/pretest_spring_2010/texts", "UTF-8");
		Txt2XmlConverter.parseTxtFiles("Familiarity", "in/texts 2 for familiarity", "UTF-8");
	}
}
