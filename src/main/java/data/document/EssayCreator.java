package data.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class EssayCreator extends Document {
	private static final long serialVersionUID = 9219491499980439568L;
	private List<String> authors = new LinkedList<String>();

	public EssayCreator(String path, LSA lsa, LDA lda, Lang lang) {
		super(path, lsa, lda, lang);
		authors = new LinkedList<String>();
	}

	public EssayCreator(String path, AbstractDocumentTemplate docTmp, LSA lsa, LDA lda, Lang lang,
			boolean usePOSTagging, boolean cleanInput) {
		super(path, docTmp, lsa, lda, lang, usePOSTagging, cleanInput);
	}

	public static EssayCreator load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, boolean cleanInput) {
		// load also LSA vector space and LDA model
		LSA lsa = LSA.loadLSA(pathToLSA, lang);
		LDA lda = LDA.loadLDA(pathToLDA, lang);
		return load(new File(pathToDoc), lsa, lda, lang, usePOSTagging, cleanInput);
	}

	public static EssayCreator load(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging,
			boolean cleanInput) {
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
					block.setId(Integer.parseInt(el.getAttribute("id")));
					block.setRefId(0);
					if (el.hasAttribute("verbalization_after")) {
						block.setVerbId(noBreakPoints);
						noBreakPoints++;
					}
					// block.setContent(StringEscapeUtils.escapeXml(el.getFirstChild()
					// .getNodeValue()));
					block.setContent(el.getFirstChild().getNodeValue());
					contents.getBlocks().add(block);
				}

			}
			EssayCreator d = new EssayCreator(docFile.getAbsolutePath(), contents, lsa, lda, lang, usePOSTagging,
					cleanInput);
			d.setNoVerbalizationBreakPoints(noBreakPoints);

			// determine title
			nl = doc.getElementsByTagName("title");
			if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
				d.setDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), lsa, lda, lang,
						usePOSTagging);
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

		// set uri
		Element uriEl = dom.createElement("uri");
		uriEl.setTextContent(getURI());
		metaEl.appendChild(uriEl);

		// set date
		Element dateEl = dom.createElement("date_of_verbalization");
		DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
		dateEl.setTextContent(formatter.format(new Date()));
		metaEl.appendChild(dateEl);

		// set comprehension score
		Element comprehenstionEl = dom.createElement("comprehension_score");
		comprehenstionEl.setTextContent("");
		metaEl.appendChild(comprehenstionEl);

		// set fluency
		Element fluencyEl = dom.createElement("fluency");
		fluencyEl.setTextContent("");
		metaEl.appendChild(fluencyEl);

		return docEl;
	}

	public void exportXMLasEssay(String path, List<String> paragraphs) {
		try {
			org.w3c.dom.Document dom = generateDOMforXMLexport(path);

			Element docEl = writeDocumentDescriptors(dom);

			Element bodyEl = dom.createElement("essay_body");
			docEl.appendChild(bodyEl);

			if (paragraphs != null && paragraphs.size() > 0) {
				for (int i = 0; i < paragraphs.size(); i++) {
					if (paragraphs.get(i) != null) {
						Element pEl = dom.createElement("p");
						pEl.setAttribute("id", i + "");
						pEl.setTextContent(paragraphs.get(i));
						bodyEl.appendChild(pEl);
					}
				}
			} else {
				for (int i = 0; i < getBlocks().size(); i++) {
					if (getBlocks().get(i) != null) {
						Element pEl = dom.createElement("p");
						pEl.setAttribute("id", i + "");
						pEl.setTextContent(getBlocks().get(i).getText());
						bodyEl.appendChild(pEl);
					}
				}
			}

			writeDOMforXMLexport(path, dom);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public List<String> readFromTxt(String path) {

		List<String> paragraphs = new ArrayList<String>();

		String content = null;
		try {
			Scanner scan = new Scanner(new File(path));
			content = scan.useDelimiter("\\Z").next();
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		StringTokenizer tokenizer = new StringTokenizer(content, "\n");
		while (tokenizer.hasMoreTokens()) {
			paragraphs.add(tokenizer.nextToken());
		}
		return paragraphs;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
}