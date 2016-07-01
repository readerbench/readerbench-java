package services.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Txt2XmlConverter {

    static Logger logger = Logger.getLogger(Txt2XmlConverter.class);

    private int currentBlock = 0;
    private org.w3c.dom.Document doc;
    private DocumentBuilder builder;

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
        Document d = new Document(null, docTmp, null, null, lang, false, false);
        d.setTitleText(title);
        d.setDate(new Date());

        d.exportXML(path);
    }

    private void addTextToElement(AbstractDocumentTemplate docTmp, Lang lang, Element parent) {
        Document d = new Document(null, docTmp, null, null, lang, false, false);
        d.addToXML(doc, parent);
    }

    public void processMetaContent(
            String title,
            Queue<String> content,
            Lang lang,
            String path,
            Element parent) throws SAXException, IOException {
        // perform processing and save the new document
        AbstractDocumentTemplate docTmp = null;
        while (!content.isEmpty()) {
            if (docTmp != null) {
                addTextToElement(docTmp, lang, parent);
            }
            String line = content.poll();
            if (line.startsWith("<section")) {
                org.w3c.dom.Document sectionDoc = builder.parse(new StringInputStream(line));
                Element section = sectionDoc.getDocumentElement();
                processMetaContent(title, content, lang, path, section);
                parent.appendChild(section);
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
        for (File f : listFiles) {
            // process each file
            String content = "";
            FileInputStream inputFile;
            InputStreamReader ir;
            try {
                inputFile = new FileInputStream(f);
                ir = new InputStreamReader(inputFile, encoding);
                BufferedReader in = new BufferedReader(ir);
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        content += line.trim() + "\n";
                    }
                }
                if ((++current_doc_to_process) % 1000 == 0) {
                    logger.info("Finished processing " + (current_doc_to_process) + " documents of "
                            + total_docs_to_process);
                }
                processContent(prefix + f.getName().replaceAll("\\.txt", ""),
                        new String(content.getBytes("UTF-8"), "UTF-8"), lang, f.getPath().replace(".txt", ".xml"));
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Finished processing all files.");
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
            FileInputStream inputFile;
            InputStreamReader ir;
            Pattern p = Pattern.compile("^[0-9]+");

            try {
                inputFile = new FileInputStream(f);
                ir = new InputStreamReader(inputFile, encoding);
                BufferedReader in = new BufferedReader(ir);
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
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Finished processing all files.");
    }
}
