package data.document;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import data.AbstractDocumentTemplate;
import data.Block;
import services.complexity.ComplexityIndices;
import services.readingStrategies.ReadingStrategies;
import view.widgets.selfexplanation.summary.SummaryView;

public class Summary extends Metacognition {

	private static final long serialVersionUID = -3087279898902437719L;

	public Summary(String path, AbstractDocumentTemplate docTmp, Document initialReadingMaterial, boolean usePOSTagging,
			boolean cleanInput) {
		super(path, docTmp, initialReadingMaterial, usePOSTagging, cleanInput);
	}

	public Summary(String content, Document initialReadingMaterial, boolean usePOSTagging, boolean cleanInput) {
		super(null, AbstractDocumentTemplate.getDocumentModel(content), initialReadingMaterial, usePOSTagging,
				cleanInput);
	}

	public static Summary loadEssay(String pathToDoc, Document initialReadingMaterial, boolean usePOSTagging,
			boolean cleanInput) {
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			InputSource input = new InputSource(new FileInputStream(new File(pathToDoc)));
			input.setEncoding("UTF-8");

			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);
			Element doc = dom.getDocumentElement();
			// determine contents
			AbstractDocumentTemplate contents = extractDocumentContent(doc, "p");
			if (contents.getBlocks().size() == 0)
				return null;

			logger.info("Building essay internal representation");
			Summary meta = new Summary(pathToDoc, contents, initialReadingMaterial, usePOSTagging, cleanInput);

			extractDocumentDescriptors(doc, meta);
			return meta;
		} catch (Exception e) {
			logger.error("[Error evaluating input file: " + pathToDoc + ", error:" + e.toString() + "]");
			return null;
		}
	}

	public void exportXML(String path) {
		try {
			org.w3c.dom.Document dom = generateDOMforXMLexport(path);

			Element docEl = writeDocumentDescriptors(dom);

			Element bodyEl = dom.createElement("summary_body");
			docEl.appendChild(bodyEl);

			for (int i = 0; i < getBlocks().size(); i++) {
				if (getBlocks().get(i) != null) {
					Element pEl = dom.createElement("p");
					pEl.setAttribute("id", i + "");
					pEl.setTextContent(getBlocks().get(i).getText());
					bodyEl.appendChild(pEl);
				}
			}

			writeDOMforXMLexport(path, dom);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void computeAll(boolean saveOutput) {
		ReadingStrategies.detReadingStrategies(this);
		computeDiscourseAnalysis();
		ComplexityIndices.computeComplexityFactors(this);
		determineComprehesionIndeces();

		if (saveOutput) {
			saveSerializedDocument();
		}
		logger.info("Finished processing summary");
	}

	public static void main(String[] args) {
		Document d = (Document) Document.loadSerializedDocument("resources/in/Matilda & Avaleur/Matilda.ser");

		Summary s = new Summary(
				"Y a donc quelqu'un qui qui entre et qui qui dit salut salut salut et........... (est-ce que tu as compris autre chose ? ) ben après ça recommence une deuxième fois donc après la la la petite fille de elle va éteindre la télé \n .......... après la la mère elle dit que au papa à leur papa de d’aller voir parce qu'elle croyait",
				d, true, true);

		s.computeAll(false);

		SummaryView view = new SummaryView(s);
		view.setVisible(true);

		for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
			System.out.println(ReadingStrategies.STRATEGY_NAMES[i]+"\t"+s.getAutomaticReadingStrategies()[0][i]);
		}

		for (Block b : s.getBlocks()) {
			System.out.println(b.getAlternateText());
		}
	}
}
