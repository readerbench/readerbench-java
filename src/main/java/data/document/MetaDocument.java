/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.document;

import data.AbstractDocument;
import static data.AnalysisElement.logger;
import data.Lang;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import services.complexity.ComplexityIndices;
import static services.complexity.ComplexityIndices.NO_COMPLEXITY_INDICES;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author Stefan
 */
public class MetaDocument extends Document {

    public static enum DocumentLevel {
        Document, Chapter, Section, Subsection
    };

    private List<AbstractDocument> children = new ArrayList<>();
    private DocumentLevel level;

    public MetaDocument(String path, LSA lsa, LDA lda, Lang lang) {
        super(path, lsa, lda, lang);
        //authors = new LinkedList<String>();
    }

    private Stream<Document> getLeavesStream() {
        Stream doc;
        if (getBlocks().isEmpty()) {
            doc = Stream.empty();
        }
        else {
            doc = Stream.of(this);
        }
        return Stream.concat(doc, children.stream().flatMap(child -> {
            if (child instanceof MetaDocument) {
                MetaDocument md = (MetaDocument) child;
                return md.getLeavesStream();
            }
            return Stream.of((Document) child);
        }));
    }

    public List<Document> getLeaves() {
        return getLeavesStream().collect(Collectors.toList());
    }
    
    @Override
    public void computeAll(boolean computeDialogism, String pathToComplexityModel, int[] selectedComplexityFactors) {
        computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors, true);
    }
    

    public void computeAll(boolean computeDialogism, String pathToComplexityModel, int[] selectedComplexityFactors, boolean recursive) {
        if (!recursive) {
            super.computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors);
            return;
        }
        List<Document> leaves = getLeaves();
        for (Document doc : leaves) {
            if (doc instanceof MetaDocument) {
                ((MetaDocument)doc).computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors, false);
            }
            else {
                doc.computeAll(computeDialogism, pathToComplexityModel, selectedComplexityFactors);
            }
        }
        if (complexityIndices == null) setComplexityIndices(new double[NO_COMPLEXITY_INDICES]);
		for (int i = 0; i < complexityIndices.length; i++) {
            final int index = i;
            complexityIndices[i] = leaves.parallelStream()
                    .map(Document::getComplexityIndices)
                    .mapToDouble(indices -> indices[index])
                    .filter(x -> x != -1)
                    .average().orElse((double) ComplexityIndices.IDENTITY);

        }

    }

    public static MetaDocument load(File file, LSA lsa, LDA lda, Lang lang,
            boolean usePOSTagging, boolean cleanInput,
            DocumentLevel maxLevel, int maxDepth) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            InputSource input = new InputSource(new FileInputStream(file));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = (Element) dom.getElementsByTagName("document").item(0);
            Element root = (Element) doc.getElementsByTagName("section").item(0);
            return load(file.getPath(), root, lsa, lda, lang, usePOSTagging, cleanInput, maxLevel, maxDepth);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Error evaluating input file " + file.getPath() + " - " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static MetaDocument load(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang,
            boolean usePOSTagging, boolean cleanInput,
            DocumentLevel maxLevel, int maxDepth) {
        // load also LSA vector space and LDA model
        LSA lsa = (pathToLSA == null) ? null : LSA.loadLSA(pathToLSA, lang);
        LDA lda = (pathToLDA == null) ? null : LDA.loadLDA(pathToLDA, lang);
        return load(new File(pathToDoc), lsa, lda, lang, usePOSTagging, cleanInput, maxLevel, maxDepth);
    }

    public static MetaDocument load(String inputPath, Element root,
            LSA lsa, LDA lda, Lang lang,
            boolean usePOSTagging, boolean cleanInput,
            DocumentLevel maxLevel, int maxDepth) {
        MetaDocument doc = new MetaDocument(inputPath, lsa, lda, lang);
        doc.level = getDocumentLevelOfElement(root);
        doc.setTitleText(root.getAttribute("title"));
        NodeList childNodes = root.getChildNodes();
        boolean extraText = false;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (!(childNodes.item(i) instanceof Element)) {
                continue;
            }
            Element subsection = (Element) childNodes.item(i);
            if (!subsection.getTagName().equals("section")) {
                extraText = true;
                continue;
            }
            DocumentLevel dl = getDocumentLevelOfElement(subsection);
            if (maxDepth > 0 && maxLevel.compareTo(dl) >= 0) {
                doc.children.add(load(inputPath, subsection, lsa, lda, lang, usePOSTagging, cleanInput, maxLevel, maxDepth - 1));
                root.removeChild(subsection);
            } else {
                extraText = true;
            }
        }
        if (extraText) {
            doc.addInfo(Document.load(inputPath, root, lsa, lda, lang, usePOSTagging, cleanInput));
        }

        return doc;
    }

    public void addInfo(Document doc) {
        for (Method getter : Document.class.getMethods()) {
            if (getter.getName().startsWith("get") && getter.getParameterCount() == 0) {
                try {
                    Method setter = MetaDocument.class.getMethod("s" + getter.getName().substring(1), getter.getReturnType());
                    setter.invoke(this, getter.invoke(doc));
                } catch (NoSuchMethodException ex) {
                    //Exceptions.printStackTrace(ex);
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }
    }
    
    @Override
    public int getNoBlocks() {
        return super.getNoBlocks() + 
                children.stream().mapToInt(AbstractDocument::getNoBlocks).sum();
    }
    
    @Override
    public int getNoSentences() {
        return super.getNoSentences() + 
                children.stream().mapToInt(AbstractDocument::getNoSentences).sum();
    }
    
    @Override
    public int getNoWords() {
        return super.getNoWords() + 
                children.stream().mapToInt(AbstractDocument::getNoWords).sum();
    }  
    
    @Override
    public int getNoContentWords() {
        return super.getNoContentWords() + 
                children.stream().mapToInt(AbstractDocument::getNoContentWords).sum();
    }  

    public static DocumentLevel getDocumentLevelOfElement(Element elem) {
        String level = elem.getAttribute("type");
        switch (level.toLowerCase()) {
            case "document":
                return DocumentLevel.Document;
            case "chapter":
                return DocumentLevel.Chapter;
            case "subsection":
                return DocumentLevel.Subsection;
            default:
                return DocumentLevel.Section;
        }
    }

    public static void main(String[] args) {
        MetaDocument doc = load("resources/in/ViBOA_nl/analysis_gabi.gutu/0_11_6_uu_3463419_0_0_183.xml",
                null, null, Lang.nl, true, true, MetaDocument.DocumentLevel.Section, 5);
        System.out.println(doc);
    }
}
