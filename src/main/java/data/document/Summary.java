package data.document;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import data.AbstractDocumentTemplate;
import data.Block;
import data.discourse.SemanticCohesion;
import services.complexity.ComplexityIndices;
import services.readingStrategies.ReadingStrategies;
import view.widgets.selfexplanation.summary.SummaryView;

public class Summary extends Metacognition {

	private static final long serialVersionUID = -3087279898902437719L;

	public Summary(String path, AbstractDocumentTemplate docTmp,
			Document initialReadingMaterial, boolean usePOSTagging,
			boolean cleanInput) {
		super(path, docTmp, initialReadingMaterial, usePOSTagging, cleanInput);
	}

	public Summary(String content, Document initialReadingMaterial,
			boolean usePOSTagging, boolean cleanInput) {
		super(null, AbstractDocumentTemplate.getDocumentModel(content),
				initialReadingMaterial, usePOSTagging, cleanInput);
	}

	public static Summary loadEssay(String pathToDoc,
			Document initialReadingMaterial, boolean usePOSTagging,
			boolean cleanInput) {
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			InputSource input = new InputSource(new FileInputStream(new File(
					pathToDoc)));
			input.setEncoding("UTF-8");

			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);
			Element doc = dom.getDocumentElement();
			// determine contents
			AbstractDocumentTemplate contents = extractDocumentContent(doc, "p");
			if (contents.getBlocks().size() == 0)
				return null;

			logger.info("Building essay internal representation");
			Summary meta = new Summary(pathToDoc, contents,
					initialReadingMaterial, usePOSTagging, cleanInput);

			extractDocumentDescriptors(doc, meta);
			return meta;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[Error evaluating input file: " + pathToDoc
					+ ", error:" + e.toString() + "]");
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

	public void computeAll(boolean computeDialogism, boolean saveOutput) {
		ReadingStrategies.detReadingStrategies(this);
		computeDiscourseAnalysis(computeDialogism);
		ComplexityIndices.computeComplexityFactors(this);
		determineComprehesionIndices();

		if (saveOutput) {
			saveSerializedDocument();
		}
		logger.info("Finished processing summary");
	}

	public void determineComprehesionIndices() {
		logger.info("Identyfing all comprehension prediction indices");
		double[] indices = new double[ComplexityIndices.NO_COMPLEXITY_INDICES
				+ ReadingStrategies.NO_READING_STRATEGIES
				+ SemanticCohesion.NO_COHESION_DIMENSIONS];

		int i = 0;
		for (; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++)
			indices[i] = this.getComplexityIndices()[i];

		indices[i + ReadingStrategies.PARAPHRASE] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.PARAPHRASE];
		indices[i + ReadingStrategies.CAUSALITY] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.CAUSALITY];
		indices[i + ReadingStrategies.BRIDGING] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.BRIDGING];
		indices[i + ReadingStrategies.TEXT_BASED_INFERENCES] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.TEXT_BASED_INFERENCES];
		indices[i + ReadingStrategies.INFERRED_KNOWLEDGE] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.INFERRED_KNOWLEDGE];
		indices[i + ReadingStrategies.META_COGNITION] += this
				.getAutomaticReadingStrategies()[0][ReadingStrategies.META_COGNITION];

		i += ReadingStrategies.NO_READING_STRATEGIES;

		// add average cohesion

		// for summaries
		SemanticCohesion coh = new SemanticCohesion(this, this.getReferredDoc());
		for (int k = 0; k < SemanticCohesion.NO_COHESION_DIMENSIONS; k++)
			indices[i + k] = coh.getSemanticDistances()[k];

		this.setComprehensionIndices(indices);
	}

	public static void main(String[] args) {
		Document d = (Document) Document
				.loadSerializedDocument("resources/in/Matilda & Avaleur/Matilda.ser");

		Summary s = new Summary(
				"je crois qu'il y a donc la famille de Matilda ben ils sont en train de manger et soudain y a quelqu'un qui entre en disant salut salut salut. Après ils croient que c'étaient des voleurs alors ils prennent des armes et ils vont vers le voleur. Y en a qui croient que c'est des voleurs mais le père il croit pas. Et la femme et Matilda ils croient elles croient que c'est des voleurs. Et après Matilda elle dit que c'est un fantôme et qu'il hante la salle. Et après ils sortent tous du salon.",
				d, true, true);

		s.computeAll(true, false);

		SummaryView view = new SummaryView(s);
		view.setVisible(true);

		for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
			System.out.println(ReadingStrategies.STRATEGY_NAMES[i] + "\t"
					+ s.getAutomaticReadingStrategies()[0][i]);
		}

		for (Block b : s.getBlocks()) {
			System.out.println(b.getAlternateText());
		}

		System.out.println(s.getAlternateText());
	}
}
