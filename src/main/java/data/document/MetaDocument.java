/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.document;

import data.AbstractDocument;
import static data.AnalysisElement.logger;
import data.Lang;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import services.complexity.ComplexityIndices;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author Stefan
 */
public class MetaDocument extends AbstractDocument {

    public static enum DocumentLevel {
        Book, Chapter, Section, Subsection
    };

    private List<AbstractDocument> children = new ArrayList<>();
    private DocumentLevel level;

    public MetaDocument(String path, LSA lsa, LDA lda, Lang lang) {
        super(path, lsa, lda, lang);
        //authors = new LinkedList<String>();
    }
    
    private Stream<Document> getLeavesStream() {
        return children.stream().flatMap(child -> {
            if (child instanceof MetaDocument)
                return ((MetaDocument)child).getLeavesStream();
            return Stream.of((Document)child);
        });
    }
    
    public List<Document> getLeaves() {
        return getLeavesStream().collect(Collectors.toList());
    }
    
    @Override
    public void computeAll(boolean computeDialogism, String pathToComplexityModel, int[] selectedComplexityFactors) {
        List<Document> leaves = getLeaves();
        for (Document doc : leaves)
            doc.computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors);
        for (int i = 0; i < complexityIndices.length; i ++) {
            final int index = i;
            complexityIndices[i] = leaves.parallelStream()
                    .map(Document::getComplexityIndices)
                    .mapToDouble(indices -> indices[index])
                    .average().orElse((double)ComplexityIndices.IDENTITY);
                    
        }
        
	}

	public static MetaDocument load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang, 
            boolean usePOSTagging, boolean cleanInput,
            DocumentLevel maxLevel, int maxDepth) {
        // load also LSA vector space and LDA model
        LSA lsa = LSA.loadLSA(pathToLSA, lang);
        LDA lda = LDA.loadLDA(pathToLDA, lang);
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(pathToDoc));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            return load(pathToDoc, doc, lsa, lda, lang, usePOSTagging, cleanInput, maxLevel, maxDepth);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Error evaluating input file " + pathToDoc + " - " + e.getMessage());
            e.printStackTrace();
        }
        return null;

    }

    public static MetaDocument load(String inputPath, Element root, 
            LSA lsa, LDA lda, Lang lang, 
            boolean usePOSTagging, boolean cleanInput,
            DocumentLevel maxLevel, int maxDepth) {
        MetaDocument doc = new MetaDocument(inputPath, lsa, lda, lang);
        doc.level = getDocumentLevelOfElement(root);
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (!(childNodes.item(i) instanceof Element)) continue;
            Element subsection = (Element)childNodes.item(i);
            if (!subsection.getTagName().equals("section")) continue;
            DocumentLevel dl = getDocumentLevelOfElement(subsection);
            if (maxDepth > 0 && maxLevel.compareTo(dl) <= 0 && 
                    subsection.getElementsByTagName("section").getLength() > 0) {
                doc.children.add(load(inputPath, subsection, lsa, lda, lang, usePOSTagging, cleanInput, maxLevel, maxDepth - 1));
            }
            else {
                doc.children.add(Document.load(inputPath, root, lsa, lda, lang, usePOSTagging, cleanInput));
            }
        }
        return doc;
    }
    
    public static DocumentLevel getDocumentLevelOfElement(Element elem) {
        String level = elem.getAttribute("type");
        switch (level.toLowerCase()) {
            case "book":
                return DocumentLevel.Book;
            case "chapter":
                return DocumentLevel.Chapter;
            case "subsection":
                return DocumentLevel.Subsection;
            default:
                return DocumentLevel.Section;
        }
    }
}
