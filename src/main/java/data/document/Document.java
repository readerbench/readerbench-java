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
import data.Block;
import data.Word;
import data.Lang;
import data.lexicalChains.DisambiguationGraph;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import services.semanticModels.ISemanticModel;

public class Document extends AbstractDocument implements Comparable<Document> {

    private static final long serialVersionUID = 9219491499980439567L;

    private List<String> authors = new LinkedList<>();
    private String uri;
    private String source;
    private String complexityLevel;
    private Date date;
    private int noVerbalizationBreakPoints;
    private List<Word> initialTopics = new LinkedList<>();
    private double keywordAbstractOverlap;

    public Document(String path, LSA lsa, LDA lda, Lang lang) {
        super(path, lsa, lda, lang);
        authors = new LinkedList<>();
    }

    public Document(String path, AbstractDocumentTemplate docTmp, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging,
            boolean cleanInput) {
        this(path, lsa, lda, lang);
        this.setText(docTmp.getText());
        setDocTmp(docTmp);
        Parsing.getParser(lang).parseDoc(docTmp, this, usePOSTagging, cleanInput);
    }

    public Document(AbstractDocumentTemplate docTmp, ISemanticModel semModel, boolean usePOSTagging,
            boolean cleanInput) {
        LSA lsa = null;
        LDA lda = null;
        if (semModel instanceof LSA) {
            lsa = (LSA) semModel;
        } else if (semModel instanceof LDA) {
            lda = (LDA) semModel;
        } else {
            logger.error("Inappropriate semantic model used for assessment: " + semModel.getPath());
        }

        setLanguage(semModel.getLanguage());
        setLSA(lsa);
        setLDA(lda);
        setDisambiguationGraph(new DisambiguationGraph(semModel.getLanguage()));
        setText(docTmp.getText());

        setDocTmp(docTmp);
        Parsing.getParser(semModel.getLanguage()).parseDoc(docTmp, this, usePOSTagging, cleanInput);
    }

    public static Document load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
            boolean cleanInput) {
        // load also LSA vector space and LDA model
        LSA lsa = LSA.loadLSA(pathToLSA, lang);
        LDA lda = LDA.loadLDA(pathToLDA, lang);
        return load(new File(pathToDoc), lsa, lda, lang, usePOSTagging, cleanInput);
    }

    public static Document load(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean cleanInput) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(docFile));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            return load(docFile.getAbsolutePath(), doc, lsa, lda, lang, usePOSTagging, cleanInput);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Error evaluating input file " + docFile.getPath() + " - " + e.getMessage());
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public static Document load(String inputPath, Element root, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean cleanInput) {
        // parse the XML file
        Element el;
        NodeList nl;
        int noBreakPoints = 0;
        // determine contents
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        nl = root.getElementsByTagName("p");
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
        Document d = new Document(inputPath, contents, lsa, lda, lang, usePOSTagging, cleanInput);
        d.setNoVerbalizationBreakPoints(noBreakPoints);

        // determine title
        nl = root.getElementsByTagName("title");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setDocumentTitle(((Element) nl.item(0)).getFirstChild().getNodeValue(), lsa, lda, lang,
                    usePOSTagging);
        }

        // determine meta
        nl = root.getElementsByTagName("meta");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue()); // to
            // check
            // with
            // XML
        }

        // get source
        nl = root.getElementsByTagName("source");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setSource(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }

        // get authors
        nl = root.getElementsByTagName("author");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                if (((Element) nl.item(i)).getFirstChild() != null
                        && ((Element) nl.item(i)).getFirstChild().getNodeValue() != null) {
                    d.getAuthors().add(((Element) nl.item(i)).getFirstChild().getNodeValue());
                }
            }
        }

        // get complexity level
        nl = root.getElementsByTagName("complexity_level");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setComplexityLevel(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }

        // get genre
        nl = root.getElementsByTagName("genre");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setGenre(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }

        // get URL
        nl = root.getElementsByTagName("uri");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            d.setURI(((Element) nl.item(0)).getFirstChild().getNodeValue());
        }

        // get date
        nl = root.getElementsByTagName("date");
        if (nl != null && nl.getLength() > 0 && ((Element) nl.item(0)).getFirstChild() != null) {
            el = (Element) nl.item(0);
            Date date = null;
            try {
                DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
                date = df.parse(el.getFirstChild().getNodeValue());
            } catch (ParseException e) {
                DateFormat df2 = new SimpleDateFormat("dd.mm.yyyy");
                try {
                    date = df2.parse(el.getFirstChild().getNodeValue());
                } catch (ParseException e2) {
                }
            }
            d.setDate(date);
        }

        // get topics
        nl = root.getElementsByTagName("Topic");
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

    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        // save serialized object - only path for LSA / LDA
        stream.defaultWriteObject();
        if (getLSA() == null) {
            stream.writeObject("");
        } else {
            stream.writeObject(getLSA().getPath());
        }
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
        if (lsaPath != null && lsaPath.length() > 0) {
            lsa = LSA.loadLSA(lsaPath, this.getLanguage());
        }
        if (ldaPath != null && ldaPath.length() > 0) {
            lda = LDA.loadLDA(ldaPath, this.getLanguage());
        }
        // rebuild LSA / LDA
        rebuildSemanticSpaces(lsa, lda);
    }

    public void addToXML(org.w3c.dom.Document doc, Element parent) {
        for (int i = 0; i < getBlocks().size(); i++) {
            Block b = getBlocks().get(i);
            if (b != null) {
                Element pEl = doc.createElement("p");
                pEl.setAttribute("id", b.getIndex() + "");
                if (b.isFollowedByVerbalization()) {
                    pEl.setAttribute("verbalization_after", "true");
                }
                pEl.setTextContent(getBlocks().get(i).getText());
                parent.appendChild(pEl);
            }
        }
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
                    if (getBlocks().get(i).isFollowedByVerbalization()) {
                        pEl.setAttribute("verbalization_after", "true");
                    }
                    pEl.setTextContent(getBlocks().get(i).getText());
                    bodyEl.appendChild(pEl);
                }
            }

            writeDOMforXMLexport(path, dom);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            logger.error(e.getMessage());
            Exceptions.printStackTrace(e);
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
        DOMSource domSource = new DOMSource(dom);
        trans.transform(domSource, new StreamResult((new OutputStreamWriter(out, "UTF-8"))));
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
                try (BufferedWriter in = new BufferedWriter(new FileWriter(output))) {
                    in.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<document/>");
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
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

    public void computeAll(boolean computeDialogism, String pathToComplexityModel, int[] selectedComplexityFactors,
            SaveType saveOutput) {
        super.computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors);
        // writing exports if document
        // if chat there are additional computations to perform
        switch (saveOutput) {
            case SERIALIZED:
                saveSerializedDocument();
                break;
            case SERIALIZED_AND_CSV_EXPORT:
                saveSerializedDocument();
                exportDocument();
                break;
            case FULL:
                exportDocument();
                exportDocumentAdvanced();
                saveSerializedDocument();
                break;
            default:
                break;
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
            descr = this.getAuthors().stream().map((author) -> author + "_").reduce(descr, String::concat);
        }
        if (this.getText() != null) {
            descr += this.getText() + "_";
        }
        return descr;
    }

    @Override
    public int hashCode() {
        return this.getFullDescription().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null) {
            return false;
        }
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
            compare1 = this.getAuthors().stream().map((author) -> author + "_").reduce(compare1, String::concat);
            compare2 = d.getAuthors().stream().map((author) -> author + "_").reduce(compare2, String::concat);
        }
        if (this.getText() != null && d.getText() != null) {
            compare1 += this.getText() + "_";
            compare2 += d.getText() + "_";
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
