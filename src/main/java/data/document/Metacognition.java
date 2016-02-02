package data.document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import services.commons.TextPreprocessing;
import services.complexity.ComplexityIndices;
import services.discourse.selfExplanations.VerbalizationAssessment;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.discourse.SemanticCohesion;

public class Metacognition extends Document {

	private static final long serialVersionUID = 3740041983851246989L;

	static Logger logger = Logger.getLogger(Metacognition.class);

	// the Block list for meta-cognitions represents the actual list of
	// verbalizations
	private Document referredDoc; // the initial referred document
	private SemanticCohesion[] blockSimilarities; // similarities with referred
													// document blocks
	private int[][] automaticReadingStrategies;
	private int[][] annotatedReadingStrategies;
	private double annotatedFluency;
	private double annotatedComprehensionScore;
	private int comprehensionClass;
	private double[] comprehensionIndices;
	private List<String> teachers = new LinkedList<String>();

	public Metacognition(String path, AbstractDocumentTemplate docTmp, Document initialReadingMaterial,
			boolean usePOSTagging, boolean cleanInput) {
		// build the corresponding structure of verbalizations
		super(path, docTmp, initialReadingMaterial.getLSA(), initialReadingMaterial.getLDA(),
				initialReadingMaterial.getLanguage(), usePOSTagging, cleanInput);
		this.referredDoc = initialReadingMaterial;
	}

	public static Metacognition loadVerbalization(String pathToDoc, Document initialReadingMaterial,
			boolean usePOSTagging, boolean cleanInput) {
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			File toProcess = new File(pathToDoc);
			logger.info("Processing self-explanation " + toProcess.getName());
			InputSource input = new InputSource(new FileInputStream(toProcess));
			input.setEncoding("UTF-8");

			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);

			Element doc = dom.getDocumentElement();
			NodeList nl;
			// determine contents
			AbstractDocumentTemplate tmp = extractDocumentContent(doc, "verbalization");

			logger.info("Building internal representation");
			Metacognition meta = new Metacognition(pathToDoc, tmp, initialReadingMaterial, usePOSTagging, cleanInput);
			extractDocumentDescriptors(doc, meta);

			// add corresponding links from verbalizations to initial document
			nl = doc.getElementsByTagName("verbalization");
			if (nl != null && nl.getLength() > 0) {
				meta.setAnnotatedReadingStrategies(new int[nl.getLength()][ReadingStrategies.NO_READING_STRATEGIES]);
				for (int i = 0; i < nl.getLength(); i++) {
					Integer after = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("after_p").getNodeValue());
					Integer id = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
					meta.getBlocks().get(id).setRefBlock(initialReadingMaterial.getBlocks().get(after));
					// add annotated scores
					try {
						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.META_COGNITION] = Integer
								.valueOf(nl.item(i).getAttributes().getNamedItem("no_control").getNodeValue());

						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.CAUSALITY] = Integer
								.valueOf(nl.item(i).getAttributes().getNamedItem("no_causality").getNodeValue());

						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.PARAPHRASE] = Integer
								.valueOf(nl.item(i).getAttributes().getNamedItem("no_paraphrase").getNodeValue());

						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.INFERRED_KNOWLEDGE] = Integer
								.valueOf(nl.item(i).getAttributes().getNamedItem("no_inferred").getNodeValue());

						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.BRIDGING] = Integer
								.valueOf(nl.item(i).getAttributes().getNamedItem("no_bridging").getNodeValue());
						meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.TEXT_BASED_INFERENCES] = meta
								.getAnnotatedReadingStrategies()[id][ReadingStrategies.BRIDGING]
								+ meta.getAnnotatedReadingStrategies()[id][ReadingStrategies.CAUSALITY];
					} catch (Exception e) {
						logger.info("Verbalization " + id + " has no annotated reading strategies.");
					}
				}
			}

			return meta;
		} catch (Exception e) {
			logger.error("Error evaluating input file " + pathToDoc + "!");
			e.printStackTrace();
		}
		return null;
	}

	public void exportXML(String path) {
		try {
			org.w3c.dom.Document dom = generateDOMforXMLexport(path);

			Element docEl = writeDocumentDescriptors(dom);

			Element bodyEl = dom.createElement("self_explanations_body");
			docEl.appendChild(bodyEl);

			for (int i = 0; i < getBlocks().size(); i++) {
				if (getBlocks().get(i) != null) {
					Element pEl = dom.createElement("verbalization");
					pEl.setAttribute("id", i + "");
					pEl.setAttribute("after_p", getBlocks().get(i).getRefBlock().getIndex() + "");
					pEl.setAttribute("no_metacognition",
							getAnnotatedReadingStrategies()[i][ReadingStrategies.META_COGNITION] + "");
					pEl.setAttribute("no_causality",
							getAnnotatedReadingStrategies()[i][ReadingStrategies.CAUSALITY] + "");
					pEl.setAttribute("no_paraphrase",
							getAnnotatedReadingStrategies()[i][ReadingStrategies.PARAPHRASE] + "");
					pEl.setAttribute("no_inferred",
							getAnnotatedReadingStrategies()[i][ReadingStrategies.INFERRED_KNOWLEDGE] + "");
					pEl.setAttribute("no_bridging",
							getAnnotatedReadingStrategies()[i][ReadingStrategies.BRIDGING] + "");

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

	/**
	 * @param dom
	 * @return
	 */
	protected Element writeDocumentDescriptors(org.w3c.dom.Document dom) {
		Element docEl = dom.createElement("document");
		if (getLanguage() != null) {
			docEl.setAttribute("language", getLanguage().toString());
		}
		dom.appendChild(docEl);

		Element metaEl = dom.createElement("meta");
		docEl.appendChild(metaEl);

		// set source
		Element sourceEl = dom.createElement("source");
		sourceEl.setTextContent(getSource());
		metaEl.appendChild(sourceEl);

		// set author
		Element authorEl = dom.createElement("author");
		authorEl.setTextContent(getAuthors().toString());
		metaEl.appendChild(authorEl);

		// set teachers
		Element teachersEl = dom.createElement("teachers");
		metaEl.appendChild(teachersEl);
		for (String teacher : getTeachers()) {
			Element teacherEl = dom.createElement("teacher");
			teacherEl.setTextContent(teacher);
			teachersEl.appendChild(teacherEl);
		}

		// set uri
		Element uriEl = dom.createElement("uri");
		uriEl.setTextContent(getURI());
		metaEl.appendChild(uriEl);

		// set date
		Element dateEl = dom.createElement("date_of_verbalization");
		DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
		dateEl.setTextContent(formatter.format(getDate()));
		metaEl.appendChild(dateEl);

		// set comprehension score
		Element comprehenstionEl = dom.createElement("comprehension_score");
		comprehenstionEl.setTextContent(getAnnotatedComprehensionScore() + "");
		metaEl.appendChild(comprehenstionEl);

		// set fluency
		Element fluencyEl = dom.createElement("fluency");
		fluencyEl.setTextContent(getAnnotatedFluency() + "");
		metaEl.appendChild(fluencyEl);

		return docEl;
	}

	/**
	 * @param doc
	 * @param contents
	 * @return
	 */
	protected static AbstractDocumentTemplate extractDocumentContent(Element doc, String tag) {
		Element el;
		NodeList nl;
		AbstractDocumentTemplate tmp = new AbstractDocumentTemplate();

		nl = doc.getElementsByTagName(tag);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				el = (Element) nl.item(i);
				BlockTemplate block = tmp.new BlockTemplate();
				block.setId(Integer.parseInt(el.getAttribute("id")));
				block.setRefId(0);
				block.setContent(TextPreprocessing.doubleCleanVerbalization(el.getFirstChild().getNodeValue()));
				tmp.getBlocks().add(block);
			}
		}
		return tmp;
	}

	/**
	 * @param doc
	 * @param meta
	 * @throws ParseException
	 */
	protected static void extractDocumentDescriptors(Element doc, Metacognition meta) throws ParseException {
		Element el;
		NodeList nl;
		// get author
		nl = doc.getElementsByTagName("author");
		if (nl != null && nl.getLength() > 0) {
			el = (Element) nl.item(0);
			meta.getAuthors().add(el.getFirstChild().getNodeValue());
		}

		// get teachers
		nl = doc.getElementsByTagName("teacher");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				if (((Element) nl.item(i)).getFirstChild() != null)
					meta.getTeachers().add(((Element) nl.item(i)).getFirstChild().getNodeValue());
			}
		}

		// get source
		nl = doc.getElementsByTagName("source");
		if (nl != null && nl.getLength() > 0) {
			el = (Element) nl.item(0);
			meta.setSource(el.getFirstChild().getNodeValue());
		}

		nl = doc.getElementsByTagName("uri");
		if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
			meta.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
		}

		nl = doc.getElementsByTagName("comprehension_score");
		if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
			meta.setAnnotatedComprehensionScore(Double.valueOf(((Element) nl.item(0)).getFirstChild().getNodeValue()));
		}

		nl = doc.getElementsByTagName("fluency");
		if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
			meta.setAnnotatedFluency(Double.valueOf(((Element) nl.item(0)).getFirstChild().getNodeValue()));
		}

		// get date
		nl = doc.getElementsByTagName("date_of_transcription");
		if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
			el = (Element) nl.item(0);
			DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
			Date date = (Date) formatter.parse(((Element) nl.item(0)).getFirstChild().getNodeValue());
			meta.setDate(date);
		}
	}

	protected void determineComprehesionIndeces() {
		logger.info("Identyfing all comprehension prediction indices");
		double[] indices = new double[ComplexityIndices.NO_COMPLEXITY_INDICES + ReadingStrategies.NO_READING_STRATEGIES
				+ 1 + SemanticCohesion.NO_COHESION_DIMENSIONS];

		int i = 0;
		for (; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++)
			indices[i] = this.getComplexityIndices()[i];
		for (int j = 0; j < this.getAutomaticReadingStrategies().length; j++) {
			indices[i + ReadingStrategies.PARAPHRASE] = this
					.getAutomaticReadingStrategies()[j][ReadingStrategies.PARAPHRASE];
			indices[i + ReadingStrategies.CAUSALITY] = this
					.getAutomaticReadingStrategies()[j][ReadingStrategies.CAUSALITY];
			indices[i
					+ ReadingStrategies.BRIDGING] = this.getAutomaticReadingStrategies()[j][ReadingStrategies.BRIDGING];
			indices[i + ReadingStrategies.TEXT_BASED_INFERENCES] = this
					.getAutomaticReadingStrategies()[j][ReadingStrategies.TEXT_BASED_INFERENCES];
			indices[i + ReadingStrategies.INFERRED_KNOWLEDGE] = this
					.getAutomaticReadingStrategies()[j][ReadingStrategies.INFERRED_KNOWLEDGE];
			indices[i + ReadingStrategies.META_COGNITION] = this
					.getAutomaticReadingStrategies()[j][ReadingStrategies.META_COGNITION];
		}
		i += ReadingStrategies.NO_READING_STRATEGIES;
		indices[i++] = this.getAnnotatedFluency();

		// add average cohesion
		if (this instanceof Summary) {
			// for summaries
			SemanticCohesion coh = new SemanticCohesion(this, this.getReferredDoc());
			for (int k = 0; k < SemanticCohesion.NO_COHESION_DIMENSIONS; k++)
				indices[i + k] = coh.getSemanticDistances()[k];
		} else {
			// average value for verbalizations
			int startIndex = 0, endIndex = 0, noSimilarities = 0;
			double[] meanCohesion = new double[SemanticCohesion.NO_COHESION_DIMENSIONS];
			for (int j = 0; j < this.getBlocks().size(); j++) {
				endIndex = this.getBlocks().get(j).getRefBlock().getIndex();
				for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
					SemanticCohesion coh = this.getBlockSimilarities()[refBlockId];
					for (int k = 0; k < SemanticCohesion.NO_COHESION_DIMENSIONS; k++)
						meanCohesion[k] += coh.getSemanticDistances()[k];
					noSimilarities++;
				}
				startIndex = endIndex + 1;
			}
			if (noSimilarities != 0) {
				for (int k = 0; k < SemanticCohesion.NO_COHESION_DIMENSIONS; k++)
					indices[i + k] = meanCohesion[k] / noSimilarities;
			}
		}
		this.setComprehensionIndices(indices);
	}

	public void exportMetacognition() {
		try {
			logger.info("Writing advanced document export");
			File output = new File(getPath().replace(".xml", ".csv"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);

			out.write("Referred document:," + getReferredDoc().getTitleText() + "\n");
			out.write("Author:");
			for (String author : getAuthors())
				out.write("," + author);
			out.write("\n");
			out.write("LSA space:," + getLSA().getPath() + "\n");
			out.write("LDA model:," + getLDA().getPath() + "\n");

			out.write(
					"Text,Paraphrasing,Causality,Bridging,Text-based Inferences,Knowledge-based Inferences,Control,Cos Sim LSA,JSH Sim LDA,Leacok Chodorow,Wu Palmer,Path Sim,Distance,Cohesion\n");

			int[] cummulativeStrategies = new int[ReadingStrategies.NO_READING_STRATEGIES];

			int startIndex = 0;
			int endIndex = 0;
			for (int index = 0; index < getBlocks().size(); index++) {
				endIndex = getBlocks().get(index).getRefBlock().getIndex();
				for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
					// add rows as blocks within the document

					SemanticCohesion coh = getBlockSimilarities()[refBlockId];
					// add block text
					out.write(getReferredDoc().getBlocks().get(refBlockId).getText().replaceAll(",", "") + ",");
					for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
						out.write(",");
					}
					// add cohesion
					out.write(coh.print() + "\n");
				}
				startIndex = endIndex + 1;

				// add corresponding verbalization
				out.write(getBlocks().get(index).getText().replaceAll(",", "") + ",");

				for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
					out.write(getAutomaticReadingStrategies()[index][i] + ",");
					cummulativeStrategies[i] += getAutomaticReadingStrategies()[index][i];
				}
				out.write(",,,,,\n");
			}

			// add final row
			out.write("Overall reading strategies,");

			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				out.write(cummulativeStrategies[i] + ",");
			}
			out.write(",,,,,\n");

			out.close();
			logger.info("Successfully finished writing file " + output.getName());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void computeAll(boolean computeDialogism, boolean saveOutput) {
		VerbalizationAssessment.detRefBlockSimilarities(this);
		ReadingStrategies.detReadingStrategies(this);

		computeDiscourseAnalysis(computeDialogism);
		ComplexityIndices.computeComplexityFactors(this);
		determineComprehesionIndeces();

		if (saveOutput) {
			saveSerializedDocument();
		}
		logger.info("Finished processing self-explanations");
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		// save serialized object - only path for LSA / LDA
		stream.defaultWriteObject();
		stream.writeObject(this.getLSA().getPath());
		stream.writeObject(this.getLDA().getPath());
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		// load serialized object - and rebuild LSA / LDA
		stream.defaultReadObject();
		LSA lsa = LSA.loadLSA((String) stream.readObject(), this.getLanguage());
		LDA lda = LDA.loadLDA((String) stream.readObject(), this.getLanguage());
		// rebuild LSA / LDA
		rebuildSemanticSpaces(lsa, lda);
		referredDoc.rebuildSemanticSpaces(lsa, lda);
	}

	public Document getReferredDoc() {
		return referredDoc;
	}

	public void setReferredDoc(Document referredDoc) {
		this.referredDoc = referredDoc;
	}

	public SemanticCohesion[] getBlockSimilarities() {
		return blockSimilarities;
	}

	public void setBlockSimilarities(SemanticCohesion[] blockSimilarities) {
		this.blockSimilarities = blockSimilarities;
	}

	public int[][] getAutomaticReadingStrategies() {
		return automaticReadingStrategies;
	}

	public void setAutomaticReadingStrategies(int[][] readingStrategies) {
		this.automaticReadingStrategies = readingStrategies;
	}

	public int[][] getAnnotatedReadingStrategies() {
		return annotatedReadingStrategies;
	}

	public void setAnnotatedReadingStrategies(int[][] annotatedReadingStrategies) {
		this.annotatedReadingStrategies = annotatedReadingStrategies;
	}

	public List<String> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<String> teachers) {
		this.teachers = teachers;
	}

	public double getAnnotatedFluency() {
		return annotatedFluency;
	}

	public void setAnnotatedFluency(double annotatedFluency) {
		this.annotatedFluency = annotatedFluency;
	}

	public double getAnnotatedComprehensionScore() {
		return annotatedComprehensionScore;
	}

	public void setAnnotatedComprehensionScore(double annotatedComprehensionScore) {
		this.annotatedComprehensionScore = annotatedComprehensionScore;
	}

	public int getComprehensionClass() {
		return comprehensionClass;
	}

	public void setComprehensionClass(int comprehensionClass) {
		this.comprehensionClass = comprehensionClass;
	}

	public double[] getComprehensionIndices() {
		return comprehensionIndices;
	}

	public void setComprehensionIndices(double[] comprehensionIndices) {
		this.comprehensionIndices = comprehensionIndices;
	}
}
