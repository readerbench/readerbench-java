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
package com.readerbench.processingservice.importdata;

import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.AbstractDocumentTemplate.BlockTemplate;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import com.readerbench.processingservice.exportdata.ExportDocumentToXML;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Txt2XmlConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Txt2XmlConverter.class);

    private final static DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();

    private int currentBlock = 0;
    private org.w3c.dom.Document doc;
    private DocumentBuilder builder;
    private final Lang lang;

    public Txt2XmlConverter(Lang lang) {
        this.lang = lang;
        try {
            builder = DBF.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void processContent(String title, String content, Lang lang, String path) {
        // perform processing and save the new document
        StringTokenizer st = new StringTokenizer(content, "\n");
        int crtBlock = 0;
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        while (st.hasMoreTokens()) {
            BlockTemplate block = docTmp.new BlockTemplate();
            block.setId(crtBlock++);
            block.setContent(Jsoup.parse(st.nextToken().trim()).text());
            docTmp.getBlocks().add(block);
        }

        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, new ArrayList<>(), new ArrayList<>());
        Document d = pipeline.createDocumentFromTemplate(docTmp);
        d.setTitleText(title);
        d.setDate(new Date());
        ExportDocumentToXML export = new ExportDocumentToXML();
        export.exportXML(d, path);
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
                ex.printStackTrace();
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

    public void processMetaContent(String title, Queue<String> content, String path, Element parent) throws SAXException, IOException {
        // perform processing and save the new document
        AbstractDocumentTemplate docTmp = null;
        while (!content.isEmpty()) {
            String line = content.poll();
            if (line.startsWith("<section")) {
                org.w3c.dom.Document sectionDoc = builder.parse(line + "</section>");
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
            DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, new ArrayList<>(), new ArrayList<>());
            Document d = pipeline.createDocumentFromTemplate(docTmp);
            addToXML(d, doc, parent);
        }
    }

    public static void addToXML(Document d, org.w3c.dom.Document doc, Element parent) {
        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                Element pEl = doc.createElement("p");
                if (b.getIndex() != -1) {
                    pEl.setAttribute("id", b.getIndex() + "");
                }
                if (b.isFollowedByVerbalization()) {
                    pEl.setAttribute("verbalization_after", "true");
                }
                pEl.setTextContent(d.getBlocks().get(i).getText());
                parent.appendChild(pEl);
            }
        }
    }

    public void parseTxtFiles(String path, Lang lang, String encoding, boolean meta) {
        // determine number of documents
        if (!new File(path).isDirectory()) {
            return;
        }
        int total_docs_to_process = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt")).length;
        LOGGER.info("Processing {} documents in total", total_docs_to_process);

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
//                            clean HTML tags
//                            content += Jsoup.parse(line.trim()).text() + "\n";
                        content += line.trim() + "\n";
                    }
                }

                if ((++current_doc_to_process) % 1000 == 0) {
                    LOGGER.info("Finished processing {} documents of {}", new Object[]{current_doc_to_process, total_docs_to_process});
                }
                if (meta) {
                    converter.processMetaContent(f.getName().replaceAll("\\.txt", ""),
                            new String(content.getBytes("UTF-8"), "UTF-8"), f.getPath().replace(".txt", ".xml"));
                } else {
                    converter.processContent(f.getName().replaceAll("\\.txt", ""),
                            new String(content.getBytes("UTF-8"), "UTF-8"), lang, f.getPath().replace(".txt", ".xml"));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException | SAXException | TransformerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Finished processing all files...");
    }

    public void parseMergedTxtFiles(String path, Lang lang, String encoding) {
        // determine number of documents
        if (!new File(path).isDirectory()) {
            return;
        }

        // determine all txt files
        File[] listFiles = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt"));

        for (File f : listFiles) {
            LOGGER.info("Processing {}", f.getPath());
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
            Pattern pMatildaAvaleur = Pattern.compile("^XPANE[0-9]+[A-Z]{}[0-9]+");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding))) {
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        Matcher m = p.matcher(line);
                        Matcher mMatildaAvaleur = pMatildaAvaleur.matcher(line);
                        if (m.find()) {
                            // flush previous content
                            if (content.length() > 0) {
                                String destination = dir.getPath() + "/" + title + ".xml";
                                processContent(title.replaceAll("[0-9]", "").trim(),
                                        new String(content.getBytes("UTF-8"), "UTF-8"), lang, destination);
                            }
                            title = line.trim();
                            content = "";
                        } else if (mMatildaAvaleur.find()) {
                            // flush previous content
                            if (content.length() > 0) {
                                String destination = dir.getPath() + "/" + title + ".xml";
                                processContent(title.trim(),
                                        new String(content.getBytes("UTF-8"), "UTF-8"), lang, destination);
                            }
                            title = line.trim();
                            content = "";
                        } else {
                            content += line.trim() + "\n";
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Finished processing all files.");
    }
}
