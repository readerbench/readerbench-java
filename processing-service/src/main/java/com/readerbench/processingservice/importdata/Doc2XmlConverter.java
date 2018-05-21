/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.processingservice.importdata;

import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author ReaderBench
 */
public class Doc2XmlConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Doc2XmlConverter.class);

    private final static DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();

    private org.w3c.dom.Document doc;
    private final DocumentBuilder builder;
    private final Lang lang;

    public Doc2XmlConverter(Lang lang) throws ParserConfigurationException {
        this.lang = lang;
        builder = DBF.newDocumentBuilder();
    }

    private static String levelToSectionType(int level) {
        switch (level) {
            case 0:
                return "document";
            case 1:
                return "section";
            default:
                return "subsection";
        }
    }

    public static void parseDocFiles(String path, Lang lang) {
        try {
            // determine number of documents
            if (!new File(path).isDirectory()) {
                return;
            }
            int total_docs_to_process = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".docx")).length;
            LOGGER.info("Processing {} documents in total", total_docs_to_process);

            int current_doc_to_process = 0;

            // determine all txt files
            File[] listFiles = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".docx"));
            Doc2XmlConverter converter = new Doc2XmlConverter(lang);
            for (File f : listFiles) {
                if ((++current_doc_to_process) % 1000 == 0) {
                    LOGGER.info("Finished processing {} documents of {}", new Object[]{current_doc_to_process, total_docs_to_process});
                }

                converter.processMetaDocFile(f, f.getPath().replace(".docx", ".xml"));
            }
            LOGGER.info("Finished processing all files.");
        } catch (FileNotFoundException | UnsupportedEncodingException | SAXException | TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processMetaDocFile(File inputFile, String outputPath) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        XWPFDocument file = new XWPFDocument(new FileInputStream(inputFile));
        LinkedList<Paragraph> content = file.getParagraphs().stream()
                .filter(p -> !p.getParagraphText().isEmpty())
                .map(p -> new Paragraph(p))
                .collect(Collectors.toCollection(LinkedList::new));
        cleanContent(content);
        if (content.isEmpty()) {
            System.out.println("Error on: " + inputFile.getPath());
            return;
        }

        File output = new File(outputPath);
        if (output.exists()) {
            output.delete();
        }
        output.createNewFile();
        try (BufferedWriter in = new BufferedWriter(new FileWriter(output))) {
            in.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<document/>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        doc = builder.parse(output);
        Element root = (Element) doc.getElementsByTagName("document").item(0);
        root.setAttribute("language", lang.name());

        //set title
        Paragraph titleParagraph = content.peek();
        String title = "UNKNOWN";
        if (titleParagraph.getLevel() == 0) {
            title = titleParagraph.getText();
        }
        Element titleElem = doc.createElement("title");
        titleElem.setTextContent(title);
        root.appendChild(titleElem);

        //set authors
        String[] nameSplit = inputFile.getName().split("_");
        String author = nameSplit[3] + "_" + nameSplit[4];
        Element authors = doc.createElement("authors");
        Element authorEl = doc.createElement("author");
        authorEl.setTextContent(author);
        authors.appendChild(authorEl);
        root.appendChild(authors);

        if (titleParagraph.getLevel() != 0) {
            Element section = doc.createElement("section");
            section.setAttribute("title", title);
            section.setAttribute("type", "document");
            root.appendChild(section);
            root = section;
        }
        processMetaDocFile(content, root, -1);

        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty("encoding", "UTF-8");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // create string from xml tree
        OutputStream out = new FileOutputStream(outputPath);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, new StreamResult((new OutputStreamWriter(out, "UTF-8"))));
    }

    public void processMetaDocFile(LinkedList<Paragraph> paragraphs, Element parent, int level) throws SAXException, IOException {
        // perform processing and save the new document
        AbstractDocumentTemplate docTmp = null;
        while (!paragraphs.isEmpty()) {
            Paragraph paragraph = paragraphs.poll();
            String text = paragraph.getText().trim();
            if (text.isEmpty()) {
                continue;
            }
            String style = paragraph.getStyle();
            if (style != null) {
                int nextLevel = paragraph.getLevel();
                if (nextLevel > level) {
                    Element section = doc.createElement("section");
                    section.setAttribute("type", levelToSectionType(nextLevel));
                    section.setAttribute("title", text);
                    processMetaDocFile(paragraphs, section, nextLevel);
                    parent.appendChild(section);
                    docTmp = null;
                    continue;
                }
                if (nextLevel != -1) {
                    paragraphs.addFirst(paragraph);
                    break;
                }
            }
            if (docTmp == null) {
                docTmp = new AbstractDocumentTemplate();
            }
            AbstractDocumentTemplate.BlockTemplate block = docTmp.new BlockTemplate();
            block.setContent(text);
            docTmp.getBlocks().add(block);
        }
        if (docTmp != null) {
            DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, new ArrayList<>(), new ArrayList<>());
            Document d = pipeline.createDocumentFromTemplate(docTmp);
            Txt2XmlConverter.addToXML(d, doc, parent);
        }
    }

    private void cleanContent(LinkedList<Paragraph> content) {
        if (content.size() > 1) {
            Paragraph p1 = content.poll();
            Paragraph p2 = content.poll();

            if (p1.getLevel() != -1 && p1.getLevel() == p2.getLevel()) {
                p1.combine(p2);
                content.addFirst(p1);
                cleanContent(content);
                return;
            }
            content.addFirst(p2);
            cleanContent(content);
            content.addFirst(p1);
        }
    }

    public static class Paragraph {

        private String text;
        private String style;
        private int level;

        public Paragraph(XWPFParagraph p) {
            text = p.getParagraphText();
            style = p.getStyle();
            level = -1;
            if (style != null) {
                style = style.toLowerCase();
                if (style.startsWith("title")) {
                    level = 0;
                }
                if (style.startsWith("heading")) {
                    level = Integer.parseInt(style.split("heading")[1]);
                }
            }
        }

        public void combine(Paragraph other) {
            text += "\n" + other.text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }
}
