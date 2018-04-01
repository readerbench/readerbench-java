/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import services.converters.lifeConverter.Dialog;
import services.converters.lifeConverter.Person;
import services.converters.lifeConverter.Turn;
import services.converters.lifeConverter.Utterance;

/**
 *
 * @author sergiu
 */
public class BarnesConverter {

    static final Logger LOGGER = Logger.getLogger("");

    public static void parse(String fileName, int nameColID, int timeColID, int msgColID) {
        try {
            File file;
            file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Dialog dialog = new Dialog(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
            XSSFWorkbook myWorkBook = new XSSFWorkbook(file);
            ArrayList<String> people = new ArrayList<>();
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            int genid = 1;
            int current = (int) mySheet.getRow(1).getCell(2).getNumericCellValue();
            DataFormatter formatter = new DataFormatter();
            try {
                Iterator<Row> iter = mySheet.rowIterator();
                iter.next();
                while (iter.hasNext()) {
                    Row r = iter.next();
                    if (r.getCell(2).getNumericCellValue() != current) {
                        jaxbMarshaller.marshal(dialog, new File(file.getParent() + "/Thread_" + current + ".xml"));
                        dialog = new Dialog(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
                        people = new ArrayList<>();
                        genid = 1;
                        current = (int) r.getCell(2).getNumericCellValue();
                    }
                    String name = formatter.formatCellValue(r.getCell(nameColID)).trim();
                    if (!name.equals("NA")) {
                        if (!people.contains(name)) {
                            dialog.getParticipants().add(new Person(name));
                            people.add(name);
                        }
                        //clean input
                        String message = Jsoup.parse(formatter.formatCellValue(r.getCell(msgColID)).trim()).text();

                        String time = formatter.formatCellValue(r.getCell(timeColID)).trim();

                        dialog.getBody().add(new Turn(name, new Utterance(genid, 0, time, message)));
                        genid++;
                    }
                }
            } catch (NullPointerException ex) {
                Exceptions.printStackTrace(ex);
                LOGGER.info("Excel needs cleanup!");
            } finally {
                jaxbMarshaller.marshal(dialog, new File("Thread_" + current + ".xml"));
            }
        } catch (JAXBException | IOException | InvalidFormatException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        BarnesConverter.parse("resources/in/Barnes_MOOC/Barnes language data.xlsx", 0, 3, 6);
    }
}
