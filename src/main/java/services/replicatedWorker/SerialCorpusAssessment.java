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
package services.replicatedWorker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import data.AbstractDocument;
import data.AbstractDocument.SaveType;
import data.Lang;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.SimilarityType;

public class SerialCorpusAssessment {

    static final Logger logger = Logger.getLogger("");

    private static void checkpoint(File checkpoint, File newFile, long processingTime) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(checkpoint);

            Element completedFiles = (Element) dom.getElementsByTagName("completedFiles").item(0);
            if (completedFiles == null) {
                completedFiles = dom.createElement("completedFiles");
            }

            Element file = dom.createElement("file");
            file.setAttribute("name", newFile.getName());
            file.setAttribute("processingTime", (processingTime / 1000) + "");
            completedFiles.appendChild(file);

            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(dom);
            trans.transform(source, result);

            BufferedWriter out = new BufferedWriter(new FileWriter(checkpoint));

            out.write(sw.toString());

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processCorpus(String rootPath, String pathToLSA, String pathToLDA, Lang lang,
            boolean usePOSTagging, boolean computeDialogism, boolean cleanInput, SaveType saveOutput) {
        logger.info("Analysing all files in \"" + rootPath + "\"");
        List<File> files = new LinkedList<>();

        FileFilter filter = (File f) -> f.getName().endsWith(".xml") && !f.getName().equals("checkpoint.xml");

        // verify checkpoint
        List<String> alreadyAnalysedFiles = new LinkedList<>();
        File checkpoint = new File(rootPath + "/checkpoint.xml");
        if (!checkpoint.exists()) {
            try {
                checkpoint.createNewFile();
                BufferedWriter in = new BufferedWriter(new FileWriter(checkpoint));
                in.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<completedFiles/>");
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document dom = null;
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                dom = db.parse(checkpoint);
                Element doc = dom.getDocumentElement();
                NodeList nl;
                Element el;

                // determine existing files
                nl = doc.getElementsByTagName("file");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        el = (Element) nl.item(i);
                        alreadyAnalysedFiles.add(el.getAttribute("name"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // determine solely unprocessed files
        File dir = new File(rootPath);
        File[] filesTODO = dir.listFiles(filter);
        for (File f : filesTODO) {
            if (!alreadyAnalysedFiles.contains(f.getName())) {
                files.add(f);
            }
        }

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, pathToLSA);
        modelPaths.put(SimilarityType.LDA, pathToLDA);
        // process all remaining files
        for (File f : files) {
            try {
                logger.info("Processing file " + f.getName());
                Long start = System.currentTimeMillis();
                AbstractDocument.loadGenericDocument(f.getAbsolutePath(), modelPaths, lang, usePOSTagging,
                        computeDialogism, null, null, cleanInput, saveOutput);
                Long end = System.currentTimeMillis();

                // update checkpoint
                checkpoint(checkpoint, f, end - start);
                logger.info("Successfully finished processing file " + f.getName());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
