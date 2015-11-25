package services.replicatedWorker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import data.cscl.Conversation;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class SerialCorpusAssessment {
	static Logger logger = Logger.getLogger(SerialCorpusAssessment.class);

	private static void checkpoint(File checkpoint, File newFile,
			long processingTime) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(checkpoint);

			Element completedFiles = (Element) dom.getElementsByTagName(
					"completedFiles").item(0);
			if (completedFiles == null) {
				completedFiles = dom.createElement("completedFiles");
			}

			Element file = dom.createElement("file");
			file.setAttribute("name", newFile.getName());
			file.setAttribute("processingTime", (processingTime / 1000) + "");
			completedFiles.appendChild(file);

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(dom);
			trans.transform(source, result);

			BufferedWriter out = new BufferedWriter(new FileWriter(checkpoint));

			out.write(sw.toString());

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void processCorpus(String rootPath, String pathToLSA,
			String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean cleanInput, String pathToComplexityModel,
			int[] selectedComplexityFactors, boolean saveOutput) {
		logger.info("Analysing all files in \"" + rootPath + "\"");
		List<File> files = new LinkedList<File>();

		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".xml")
						&& !f.getName().equals("checkpoint.xml");
			}
		};

		// verify checkpoint
		List<String> alreadyAnalysedFiles = new LinkedList<String>();
		File checkpoint = new File(rootPath + "/checkpoint.xml");
		if (!checkpoint.exists()) {
			try {
				checkpoint.createNewFile();
				BufferedWriter in = new BufferedWriter(new FileWriter(
						checkpoint));
				in.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<completedFiles/>");
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document dom = null;
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				dom = db.parse(checkpoint);
				Element doc = dom.getDocumentElement();
				NodeList nl;
				Element el;

				// determine existing files
				nl = doc.getElementsByTagName("file");
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						el = (Element) nl.item(i);
						alreadyAnalysedFiles.add(el.getAttribute("name"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// determine solely unprocessed files
		File dir = new File(rootPath);
		File[] filesTODO = dir.listFiles(filter);
		for (File f : filesTODO) {
			if (!alreadyAnalysedFiles.contains(f.getName()))
				files.add(f);
		}

		LSA lsa = LSA.loadLSA(pathToLSA, lang);
		LDA lda = LDA.loadLDA(pathToLDA, lang);

		// process all remaining files
		for (File f : files) {
			try {
				logger.info("Processing file " + f.getName());
				Long start = System.currentTimeMillis();
				Conversation c = Conversation
						.load(f, lsa, lda, lang, usePOSTagging, cleanInput);
				c.computeAll(pathToComplexityModel, selectedComplexityFactors,
						saveOutput);
				Long end = System.currentTimeMillis();

				// update checkpoint
				checkpoint(checkpoint, f, end - start);
				logger.info("Successfully finished processing file "
						+ f.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		SerialCorpusAssessment.processCorpus("in/forum_Nic",
				"resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.eng, true,
				true, null, null, true);
	}
}
