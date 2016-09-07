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

import javax.jms.ObjectMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import data.AbstractDocument.SaveType;
import data.Lang;

public class CorpusAssessmentMaster extends Master {

    static final Logger logger = Logger.getLogger(CorpusAssessmentMaster.class);

    public static final String PATH_TO_LSA = "resources/config/EN/LSA/TASA";
    public static final String PATH_TO_LDA = "resources/config/EN/LDA/TASA";
    public static final Lang PROCESSING_LANGUAGE = Lang.en;
    public static final boolean USE_POS_TAGGING = true;
    public static final boolean COMPUTE_DIALOGISM = true;
    public static final boolean CLEAN_INPUT = true;
    public static final boolean PROCESS_INPUT = true;
    public static final String PATH_TO_COMPLEXITY_MODEL = null;// "in/corpus_complexity_tasa_en";
    public static final int[] SELECTED_COMPLEXITY_FACTORS = null;
    public static final SaveType SAVE_OUTPUT = SaveType.SERIALIZED_AND_CSV_EXPORT;

    private List<File> files;
    private File checkpoint;
    // private LSA lsa;
    // private LDA lda;

    public CorpusAssessmentMaster(String rootPath) {
        super();
        // load also LSA vector space and LDA model
        // lsa = LSA.loadLSA(PATH_TO_LSA, PROCESSING_LANGUAGE);
        // lda = LDA.loadLDA(PATH_TO_LDA, PROCESSING_LANGUAGE);
        logger.info("Master analysing all files in " + rootPath);
        files = new LinkedList<File>();

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".xml") && !f.getName().equals("checkpoint.xml");
            }
        };

        // verify checkpoint
        List<String> alreadyAnalysedFiles = new LinkedList<String>();
        checkpoint = new File(rootPath + "/checkpoint.xml");
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
            Document dom = null;
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
        File dir = new File(rootPath);
        File[] filesTODO = dir.listFiles(filter);
        for (File f : filesTODO) {
            if (!alreadyAnalysedFiles.contains(f.getName())) {
                files.add(f);
            }
        }
        noTasks = files.size();
    }

    public void sendTask(String addr) throws Exception {
        if (!files.isEmpty()) {
            synchronized (files) {
                File f = files.remove(0);
                TaskMsg tMsg = new TaskMsg(new Object[]{f.getAbsolutePath()}, false);
                logger.info("Master assigning " + tMsg);
                ObjectMessage msg = sessionTask.get(addr).createObjectMessage(tMsg);

                master.get(addr).send(msg);
                if (transacted) {
                    sessionTask.get(addr).commit();
                }
            }
        } else {
            TaskMsg tMsg = new TaskMsg(null, true);
            logger.info("Master ending communication with " + addr);
            ObjectMessage msg = sessionTask.get(addr).createObjectMessage(tMsg);

            master.get(addr).send(msg);
            if (transacted) {
                sessionTask.get(addr).commit();
            }
            cleanup(addr);
        }
    }

    public void reassignTask(String task) {
        synchronized (files) {
            files.add(new File(task));
        }
    }

    public void checkpoint() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        List<String> alreadyAnalysedFiles = new LinkedList<String>();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(checkpoint);

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

            Element completedFiles = (Element) dom.getElementsByTagName("completedFiles").item(0);
            if (completedFiles == null) {
                completedFiles = dom.createElement("completedFiles");
            }

            for (String addr : completedTasks.keySet()) {
                for (String path : completedTasks.get(addr)) {
                    String fileName = (new File(path)).getName();
                    if (!alreadyAnalysedFiles.contains(fileName)) {
                        Element file = dom.createElement("file");
                        file.setAttribute("name", fileName);
                        file.setAttribute("processingTime", (endTime.get(path) - startTime.get(path)) / 1000 + "");
                        completedFiles.appendChild(file);
                    }

                }
            }
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

    public void analyseResult(Object result) {
        // DAO.Document d = (DAO.Document) result;
        // if (d != null) {
        // d.rebuildSemanticSpaces(lsa, lda);
        // logger.info("Successfully received file " + d.getPath());
        // }
    }

    public void performBeforeComplete() {
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        Master MasterTool = new CorpusAssessmentMaster("in/forum_Nic");
        MasterTool.run();
    }
}
