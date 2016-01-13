package data.document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import services.nlp.parsing.Parsing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class Document extends AbstractDocument implements Comparable<Document> {

	private static final long serialVersionUID = 9219491499980439567L;
	private List<String> authors = new LinkedList<String>();
	private String uri;
	private String source;
	private String complexityLevel;
	private Date date;
	private int noVerbalizationBreakPoints;
	private List<Word> initialTopics = new LinkedList<Word>();
	private double keywordAbstractOverlap;

	public Document(String path, LSA lsa, LDA lda, Lang lang) {
		super(path, lsa, lda, lang);
		authors = new LinkedList<String>(); 
	}
	
	public Document(String path, AbstractDocumentTemplate docTmp, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging,
			boolean cleanInput) {
		this(path, lsa, lda, lang);
		this.setText(docTmp.getText());
		setDocTmp(docTmp);
		Parsing.getParser(lang).parseDoc(docTmp, this, usePOSTagging, cleanInput);
	}

	public static Document load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean cleanInput) {
		// load also LSA vector space and LDA model
		LSA lsa = LSA.loadLSA(pathToLSA, lang);
		LDA lda = LDA.loadLDA(pathToLDA, lang);
		return load(new File(pathToDoc), lsa, lda, lang, usePOSTagging, cleanInput);
	}

	public static Document load(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean cleanInput) {
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			InputSource input = new InputSource(new FileInputStream(docFile));
			input.setEncoding("UTF-8");
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);

			Element doc = dom.getDocumentElement();

			Element el;
			NodeList nl;
			int noBreakPoints = 0;
			// determine contents
			AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
			nl = doc.getElementsByTagName("p");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);
					BlockTemplate block = contents.new BlockTemplate();
					if (el.hasAttribute("id")) {
						try {
							block.setId(Integer.parseInt(el.getAttribute("id")));
						} catch (Exception e) {
							block.setId(i);
						}
					} else {
						block.setId(i);
					}
					block.setRefId(0);
					if (el.hasAttribute("verbalization_after")) {
						block.setVerbId(noBreakPoints);
						noBreakPoints++;
					}
					// block.setContent(StringEscapeUtils.escapeXml(el.getFirstChild()
					// .getNodeValue()));
					if (el.getFirstChild() != null && el.getFirstChild().getNodeValue() != null
							&& el.getFirstChild().getNodeValue().trim().length() > 0) {
						block.setContent(el.getFirstChild().getNodeValue());
						contents.getBlocks().add(block);
					}
				}
			}
			Document d = new Document(docFile.getAbsolutePath(), contents, lsa, lda, lang, usePOSTagging, cleanInput);
			d.setNoVerbalizationBreakPoints(noBreakPoints);

			// determine title
			nl = doc.getElementsByTagName("title");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), lsa, lda, lang,
						usePOSTagging);
			}

			// determine meta
			nl = doc.getElementsByTagName("meta");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue()); // to
																					// check
																					// with
																					// XML
			}

			// get source
			nl = doc.getElementsByTagName("source");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setSource(((Element) nl.item(0)).getFirstChild().getNodeValue());
			}

			// get authors
			nl = doc.getElementsByTagName("author");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					if (((Element) nl.item(i)).getFirstChild() != null
							&& ((Element) nl.item(i)).getFirstChild().getNodeValue() != null)
						d.getAuthors().add(((Element) nl.item(i)).getFirstChild().getNodeValue());
				}
			}

			// get complexity level
			nl = doc.getElementsByTagName("complexity_level");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setComplexityLevel(((Element) nl.item(0)).getFirstChild().getNodeValue());
			}

			// get genre
			nl = doc.getElementsByTagName("genre");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
			}

			// get URL
			nl = doc.getElementsByTagName("uri");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
			}

			// get date
			nl = doc.getElementsByTagName("date");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				el = (Element) nl.item(0);
				Date date = null;
				try {
					DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
					date = df.parse(((Element) nl.item(0)).getFirstChild().getNodeValue());
				} catch (ParseException e) {
					DateFormat df2 = new SimpleDateFormat("dd.mm.yyyy");
					try {
						date = df2.parse(((Element) nl.item(0)).getFirstChild().getNodeValue());
					} catch (ParseException e2) {
					}
				}
				d.setDate(date);
			}

			// get topics
			nl = doc.getElementsByTagName("Topic");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					if (((Element) nl.item(i)).getFirstChild() != null
							&& ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
						String wordToAdd = ((Element) nl.item(i)).getFirstChild().getNodeValue().toLowerCase();
						d.getInitialTopics().add(Word.getWordFromConcept(wordToAdd, lang));
					}
				}
			}
			return d;
		} catch (Exception e) {
			logger.error("Error evaluating input file " + docFile.getPath() + " - " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		// save serialized object - only path for LSA / LDA
		stream.defaultWriteObject();
		if (getLSA() == null) {
			stream.writeObject("");
		} else
			stream.writeObject(getLSA().getPath());
		if (getLDA() == null) {
			stream.writeObject("");
		} else {
			stream.writeObject(getLDA().getPath());
		}
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		// load serialized object
		stream.defaultReadObject();
		String lsaPath = (String) stream.readObject();
		String ldaPath = (String) stream.readObject();
		LSA lsa = null;
		LDA lda = null;
		if (lsaPath != null && lsaPath.length() > 0)
			lsa = LSA.loadLSA(lsaPath, this.getLanguage());
		if (ldaPath != null && ldaPath.length() > 0)
			lda = LDA.loadLDA(ldaPath, this.getLanguage());
		// rebuild LSA / LDA
		rebuildSemanticSpaces(lsa, lda);
	}

	public void exportXML(String path) {
		try {
			org.w3c.dom.Document dom = generateDOMforXMLexport(path);

			Element docEl = dom.createElement("document");
			if (getLanguage() != null) {
				docEl.setAttribute("language", getLanguage().toString());
			}
			dom.appendChild(docEl);

			Element metaEl = dom.createElement("meta");
			docEl.appendChild(metaEl);

			Element genreEl = dom.createElement("genre");
			genreEl.setTextContent(getGenre());
			metaEl.appendChild(genreEl);

			Element titleEl = dom.createElement("title");
			titleEl.setTextContent(getTitleText());
			metaEl.appendChild(titleEl);

			Element authorsEl = dom.createElement("authors");
			metaEl.appendChild(authorsEl);

			for (String author : this.authors) {
				Element authorEl = dom.createElement("author");
				authorEl.setTextContent(author);
				authorsEl.appendChild(authorEl);
			}

			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			Element dateEl = dom.createElement("date");
			dateEl.setTextContent(new StringBuilder(formatter.format(date)).toString());
			metaEl.appendChild(dateEl);

			Element sourceEl = dom.createElement("source");
			sourceEl.setTextContent(this.source);
			metaEl.appendChild(sourceEl);

			Element complexityEl = dom.createElement("complexity_level");
			complexityEl.setTextContent(this.complexityLevel);
			metaEl.appendChild(complexityEl);

			Element uriEl = dom.createElement("uri");
			uriEl.setTextContent(this.uri);
			metaEl.appendChild(uriEl);

			Element bodyEl = dom.createElement("body");
			docEl.appendChild(bodyEl);

			for (int i = 0; i < getBlocks().size(); i++) {
				if (getBlocks().get(i) != null) {
					Element pEl = dom.createElement("p");
					pEl.setAttribute("id", i + "");
					if (getBlocks().get(i).isFollowedByVerbalization())
						pEl.setAttribute("verbalization_after", "true");
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
	 * @param path
	 * @param dom
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 * @throws UnsupportedEncodingException
	 */
	protected void writeDOMforXMLexport(String path, org.w3c.dom.Document dom)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, FileNotFoundException,
			TransformerException, UnsupportedEncodingException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty("encoding", "UTF-8");
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// create string from xml tree
		OutputStream out = new FileOutputStream(path);
		DOMSource source = new DOMSource(dom);
		trans.transform(source, new StreamResult((new OutputStreamWriter(out, "UTF-8"))));
	}

	/**
	 * @param path
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected org.w3c.dom.Document generateDOMforXMLexport(String path)
			throws ParserConfigurationException, SAXException, IOException {
		// generate a corresponding XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		File output = new File(path);
		if (!output.exists()) {
			try {
				output.createNewFile();
				BufferedWriter in = new BufferedWriter(new FileWriter(output));
				in.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<document/>");
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document dom = db.parse(output);

		// determine existing files
		NodeList nl = dom.getElementsByTagName("document");
		if (nl != null && nl.getLength() > 0) {
			for (int i = nl.getLength() - 1; i >= 0; i--) {
				dom.removeChild((Element) nl.item(i));
			}
		}
		return dom;
	}

	public void computeAll(String pathToComplexityModel, int[] selectedComplexityFactors, boolean saveOutput) {
		super.computeAll(pathToComplexityModel, selectedComplexityFactors);
		// writing exports if document
		// if chat there are additional computations to perform
		if (saveOutput) {
			exportDocument();
			exportDocumentAdvanced();
			saveSerializedDocument();
		}
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getComplexityLevel() {
		return complexityLevel;
	}

	public void setComplexityLevel(String complexityLevel) {
		this.complexityLevel = complexityLevel;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getNoVerbalizationBreakPoints() {
		return noVerbalizationBreakPoints;
	}

	public void setNoVerbalizationBreakPoints(int noVerbalizationBreakPoints) {
		this.noVerbalizationBreakPoints = noVerbalizationBreakPoints;
	}

	public String getFullDescription() {
		String descr = "";
		if (this.getPath() != null) {
			descr += this.getPath() + "_";
		}
		if (this.getTitleText() != null) {
			descr += this.getTitleText() + "_";
		}
		if (this.getLSA() != null) {
			descr += this.getLSA().getPath() + "_";
		}
		if (this.getLDA() != null) {
			descr += this.getLDA().getPath() + "_";
		}
		if (this.getAuthors() != null) {
			for (String author : this.getAuthors())
				descr += author + "_";
		}
		return descr;
	}

	@Override
	public int hashCode() {
		return this.getFullDescription().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == null || obj == null)
			return false;
		Document d = (Document) obj;
		return this.getFullDescription().equals(d.getFullDescription());
	}

	@Override
	public int compareTo(Document d) {
		String compare1 = "", compare2 = "";
		if (this.getPath() != null && d.getPath() != null) {
			compare1 += this.getPath() + "_";
			compare2 += d.getPath() + "_";
		}
		if (this.getTitleText() != null && d.getTitleText() != null) {
			compare1 += this.getTitleText() + "_";
			compare2 += d.getTitleText() + "_";
		}
		if (this.getLSA() != null && d.getLSA() != null) {
			compare1 += this.getLSA().getPath() + "_";
			compare2 += d.getLSA().getPath() + "_";
		}
		if (this.getLDA() != null && d.getLDA() != null) {
			compare1 += this.getLDA().getPath() + "_";
			compare2 += d.getLDA().getPath() + "_";
		}
		if (this.getAuthors() != null && d.getAuthors() != null) {
			for (String author : this.getAuthors())
				compare1 += author + "_";
			for (String author : d.getAuthors())
				compare2 += author + "_";
		}
		return compare1.compareTo(compare2);
	}

	public List<Word> getInitialTopics() {
		return initialTopics;
	}

	public void setInitialTopics(List<Word> initialTopics) {
		this.initialTopics = initialTopics;
	}

	public double getKeywordAbstractOverlap() {
		return keywordAbstractOverlap;
	}

	public void setKeywordAbstractOverlap(double keywordAbstractOverlap) {
		this.keywordAbstractOverlap = keywordAbstractOverlap;
	}
}
