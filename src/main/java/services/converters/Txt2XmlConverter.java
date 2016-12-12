/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import data.Lang;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
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
import org.apache.tools.ant.filters.StringInputStream;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import services.semanticModels.ISemanticModel;

public class Txt2XmlConverter {

    static final Logger logger = Logger.getLogger("");
    private final static DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();

    private int currentBlock = 0;
    private org.w3c.dom.Document doc;
    private final DocumentBuilder builder;
    private final Lang lang;

    public Txt2XmlConverter(Lang lang) throws ParserConfigurationException {
        this.lang = lang;
        builder = DBF.newDocumentBuilder();
    }

    public static void processContent(String title, String content, Lang lang, String path) {
        // perform processing and save the new document
        StringTokenizer st = new StringTokenizer(content, "\n");
        int crtBlock = 0;
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        while (st.hasMoreTokens()) {
            BlockTemplate block = docTmp.new BlockTemplate();
            block.setId(crtBlock++);
            block.setContent(st.nextToken().trim());
            docTmp.getBlocks().add(block);
        }
        Document d = new Document(null, docTmp, new ArrayList<>(), lang, false);
        d.setTitleText(title);
        d.setDate(new Date());
        d.exportXML(path);
    }

    public void processMetaContent(String title, String content, String path) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        String[] lines = content.split("\n");
        Queue<String> queue = new LinkedList<>();
        for (String line : lines) {
            queue.offer(line);
        }
        File output = new File(path);
        if (!output.exists()) {
            output.createNewFile();
            try (BufferedWriter in = new BufferedWriter(new FileWriter(output))) {
                in.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<document/>");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        doc = builder.parse(output);
        Element root = (Element) doc.getElementsByTagName("document").item(0);
        root.setAttribute("language", lang.name());
        Element titleElem = doc.createElement("title");
        titleElem.setTextContent(title);
        root.appendChild(titleElem);

        processMetaContent(title, queue, path, root);

        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty("encoding", "UTF-8");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // create string from xml tree
        OutputStream out = new FileOutputStream(path);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, new StreamResult((new OutputStreamWriter(out, "UTF-8"))));
    }

    private void addTextToElement(AbstractDocumentTemplate docTmp, Lang lang, Element parent) {
        Document d = new Document(null, docTmp, new ArrayList<>(), lang, false);
        d.addToXML(doc, parent);
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

    public void processMetaDocFile(
            LinkedList<Paragraph> paragraphs,
            Element parent,
            int level) throws SAXException, IOException {
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
            BlockTemplate block = docTmp.new BlockTemplate();
            block.setContent(text);
            docTmp.getBlocks().add(block);
        }
        if (docTmp != null) {
            addTextToElement(docTmp, lang, parent);
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

    public void processMetaDocFile(File inputFile, String outputPath) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        currentBlock = 0;
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
            Exceptions.printStackTrace(ex);
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

    public void processMetaContent(
            String title,
            Queue<String> content,
            String path,
            Element parent) throws SAXException, IOException {
        // perform processing and save the new document
        AbstractDocumentTemplate docTmp = null;
        while (!content.isEmpty()) {
            String line = content.poll();
            if (line.startsWith("<section")) {
                org.w3c.dom.Document sectionDoc = builder.parse(new StringInputStream(line + "</section>"));
                Element section = sectionDoc.getDocumentElement();
                Element newSection = doc.createElement("section");
                NamedNodeMap attributes = section.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    newSection.setAttribute(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
                }
                processMetaContent(title, content, path, newSection);
                parent.appendChild(newSection);
                docTmp = null;
                continue;
            }
            if (line.startsWith("</section")) {
                break;
            }
            if (docTmp == null) {
                docTmp = new AbstractDocumentTemplate();
            }
            BlockTemplate block = docTmp.new BlockTemplate();
            block.setId(currentBlock++);
            block.setContent(line);
            docTmp.getBlocks().add(block);
        }
        if (docTmp != null) {
            addTextToElement(docTmp, lang, parent);
        }

    }

    public static void parseTxtFiles(String prefix, String path, Lang lang, String encoding) {
        parseTxtFiles(prefix, path, lang, encoding, false);
    }

    public static void parseTxtFiles(String prefix, String path, Lang lang, String encoding, boolean meta) {
        try {
            // determine number of documents
            if (!new File(path).isDirectory()) {
                return;
            }
            int total_docs_to_process = new File(path).listFiles((File pathname)
                    -> pathname.getName().endsWith(".txt")
            ).length;
            logger.info("Processing " + total_docs_to_process + " documents in total");

            int current_doc_to_process = 0;

            // determine all txt files
            File[] listFiles = new File(path).listFiles((File pathname)
                    -> pathname.getName().endsWith(".txt"));
            String line;
            Txt2XmlConverter converter = new Txt2XmlConverter(lang);
            for (File f : listFiles) {
                converter.currentBlock = 0;
                // process each file
                String content = "";
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding))) {
                    while ((line = in.readLine()) != null) {
                        if (line.trim().length() > 0) {
                            content += line.trim() + "\n";
                        }
                    }
                    if ((++current_doc_to_process) % 1000 == 0) {
                        logger.info("Finished processing " + (current_doc_to_process) + " documents of "
                                + total_docs_to_process);
                    }
                    if (meta) {
                        converter.processMetaContent(prefix + f.getName().replaceAll("\\.txt", ""),
                                new String(content.getBytes("UTF-8"), "UTF-8"), f.getPath().replace(".txt", ".xml"));
                    } else {
                        processContent(prefix + f.getName().replaceAll("\\.txt", ""),
                                new String(content.getBytes("UTF-8"), "UTF-8"), lang, f.getPath().replace(".txt", ".xml"));
                    }
                } catch (FileNotFoundException e) {
                    Exceptions.printStackTrace(e);
                } catch (UnsupportedEncodingException e) {
                    Exceptions.printStackTrace(e);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (SAXException | TransformerException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            logger.info("Finished processing all files.");
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void parseDocFiles(String path, Lang lang) {
        try {
            // determine number of documents
            if (!new File(path).isDirectory()) {
                return;
            }
            int total_docs_to_process = new File(path).listFiles((File pathname)
                    -> pathname.getName().endsWith(".docx")
            ).length;
            logger.info("Processing " + total_docs_to_process + " documents in total");

            int current_doc_to_process = 0;

            // determine all txt files
            File[] listFiles = new File(path).listFiles((File pathname)
                    -> pathname.getName().endsWith(".docx"));
            Txt2XmlConverter converter = new Txt2XmlConverter(lang);
            for (File f : listFiles) {
                if ((++current_doc_to_process) % 1000 == 0) {
                    logger.info("Finished processing " + (current_doc_to_process) + " documents of "
                            + total_docs_to_process);
                }

                converter.processMetaDocFile(f, f.getPath().replace(".docx", ".xml"));
            }
            logger.info("Finished processing all files.");
        } catch (FileNotFoundException e) {
            Exceptions.printStackTrace(e);
        } catch (UnsupportedEncodingException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (SAXException | TransformerException | ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void parseMergedTxtFiles(String path, Lang lang, String encoding) {
        // determine number of documents
        if (!new File(path).isDirectory()) {
            return;
        }

        // determine all txt files
        File[] listFiles = new File(path).listFiles(
                (File pathname) -> pathname.getName().endsWith(".txt"));

        for (File f : listFiles) {
            logger.info("Processing " + f.getPath());
            // see if there are folders with genre names
            File dir = new File(path + "/" + f.getName().replaceAll("\\.txt", ""));
            if (dir.exists()) {
                for (File tmp : dir.listFiles()) {
                    tmp.delete();
                }
                dir.delete();
            }
            dir.mkdir();

            // process each file
            String content = "";
            String title = "";
            String line;
            Pattern p = Pattern.compile("^[0-9]+");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding))) {
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            // flush previous content
                            if (content.length() > 0) {
                                String destination = dir.getPath() + "/" + title + ".xml";
                                processContent(title.replaceAll("[0-9]", "").trim(),
                                        new String(content.getBytes("UTF-8"), "UTF-8"), lang, destination);
                            }
                            title = line.trim();
                        } else {
                            content += line.trim() + "\n";
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Exceptions.printStackTrace(e);
            } catch (UnsupportedEncodingException e) {
                Exceptions.printStackTrace(e);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        logger.info("Finished processing all files.");
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

    public static void main(String[] args) throws ParserConfigurationException, SAXException, TransformerException {
        //parseDocFiles("resources/in/ViBOA_nl/analysis/", Lang.nl);
        parseMergedTxtFiles("resources/in/Philippe/DEPP/corrEssays/avaleur", Lang.fr, "UTF-8");
        parseMergedTxtFiles("resources/in/Philippe/DEPP/corrEssays/matilda", Lang.fr, "UTF-8");
//        parseDocFiles("resources/in/ViBOA_nl/design task 1/", Lang.nl);
//        parseDocFiles("resources/in/ViBOA_nl/design task 2/", Lang.nl);
//        parseDocFiles("resources/in/ViBOA_nl/evaluation task 1/", Lang.nl);
//        parseDocFiles("resources/in/ViBOA_nl/evaluation task 2/", Lang.nl);
//        parseDocFiles("resources/in/ViBOA_nl/final task/", Lang.nl);
//        parseDocFiles("resources/in/ViBOA_nl/Test/", Lang.nl);

    }
}
