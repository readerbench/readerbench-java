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
package runtime.cscl;

import data.AbstractDocument;
import data.Lang;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Participant;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.complexity.dialogism.AvgNoVoices;
import services.complexity.dialogism.VoicesAvgSpan;
import services.complexity.dialogism.VoicesMaxSpan;
import services.converters.lifeConverter.Dialog;
import services.converters.lifeConverter.Person;
import services.converters.lifeConverter.Turn;
import services.converters.lifeConverter.Utterance;
import services.processing.SerialProcessing;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class CreativityTest {

    private static final Logger LOGGER = Logger.getLogger("");

    public static void parse(File file, int nameColID, int timeColID, int msgColID) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Dialog temp = new Dialog(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);

            XSSFWorkbook myWorkBook = new XSSFWorkbook(file);
            ArrayList<String> people = new ArrayList<>();
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            int genid = 1;
            DataFormatter formatter = new DataFormatter();
            try {
                Iterator<Row> iter = mySheet.rowIterator();
                iter.next();
                while (iter.hasNext()) {
                    Row r = iter.next();
                    String name = formatter.formatCellValue(r.getCell(nameColID)).trim();
                    if (!people.contains(name)) {
                        temp.getParticipants().add(new Person(name));
                        people.add(name);
                    }
                    String time = formatter.formatCellValue(r.getCell(timeColID));

                    //clean input
                    String message = Jsoup.parse(formatter.formatCellValue(r.getCell(msgColID))).text();

                    if (name.length() > 0 && name.length() > 0 && time.length() > 0) {
                        temp.getBody().add(new Turn(name, new Utterance(genid, 0, time, message)));
                        genid++;
                    }
                }
            } catch (NullPointerException ex) {
                LOGGER.log(Level.INFO, "File {0} requires cleaning ...", file.getName());
                Exceptions.printStackTrace(ex);
            } finally {
                jaxbMarshaller.marshal(temp, new File(file.getAbsolutePath().replace(".xlsx", ".xml")));
            }
        } catch (JAXBException | IOException | InvalidFormatException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void processFolder(String folder, boolean restartProcessing, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging) {
        File dir = new File(folder);

        if (dir.isDirectory()) {
            //regenerate all corresponding xml files
            File[] excelFiles = dir.listFiles((File pathname) -> pathname.getName().endsWith(".xlsx"));
            for (File excelFile : excelFiles) {
                CreativityTest.parse(excelFile, 1, 0, 2);
            }
            if (restartProcessing) {
                // remove checkpoint file
                File checkpoint = new File(dir.getPath() + "/checkpoint.xml");
                if (checkpoint.exists()) {
                    checkpoint.delete();
                }
            }
            SerialProcessing.processCorpus(dir.getAbsolutePath(), pathToLSA, pathToLDA, lang, usePOSTagging,
                    true, true, AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
            processConversations(dir.getAbsolutePath());
        }

        LOGGER.info("Finished processsing all files ...");
    }

    public static void processConversations(String path) {
        LOGGER.log(Level.INFO, "Loading all files in {0}", path);

        FileFilter filter = (File f) -> f.getName().endsWith(".ser");
        File[] filesTODO = (new File(path)).listFiles(filter);

        File output = new File(path + "/measurements.csv");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            out.write("SEP=,\nFilename,AVG(Social KB), ABS(Social KB), AVG(Dialogism), Voices, Avg voices, Avg voice span, Max voice span");
            for (File f : filesTODO) {
                Conversation c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
                if (c.getParticipants().size() != 2) {
                    LOGGER.log(Level.WARNING, "Incorrect number of participants for {0}", f.getPath());
                } else {
                    Participant p1 = c.getParticipants().get(0);
                    Participant p2 = c.getParticipants().get(1);
                    out.write("\n" + f.getName().replace(".ser", "")
                            + "," + Formatting.formatNumber((p1.getIndices().get(CSCLIndices.SOCIAL_KB) + p2.getIndices().get(CSCLIndices.SOCIAL_KB)) / 2)
                            + "," + Formatting.formatNumber(Math.abs(p1.getIndices().get(CSCLIndices.SOCIAL_KB) - p2.getIndices().get(CSCLIndices.SOCIAL_KB)))
                            + "," + Formatting.formatNumber((p1.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE) + p2.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)) / 2)
                            + "," + c.getVoices().size()
                            + "," + Formatting.formatNumber(new AvgNoVoices().compute(c))
                            + "," + Formatting.formatNumber(new VoicesAvgSpan().compute(c))
                            + "," + Formatting.formatNumber(new VoicesMaxSpan().compute(c))
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        CreativityTest.processFolder("resources/in/creativity/separated tasks", true, "resources/config/EN/LSA/TASA_LAK", "resources/config/EN/LDA/TASA_LAK", Lang.en, true);
    }
}
