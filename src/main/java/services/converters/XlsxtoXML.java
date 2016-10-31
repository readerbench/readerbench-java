/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Exceptions;
import services.converters.lifeConverter.Dialog;
import services.converters.lifeConverter.Person;
import services.converters.lifeConverter.Turn;
import services.converters.lifeConverter.Utterance;

/**
 *
 * @author sergiu
 */
public class XlsxtoXML {
    
    public static void main(String[] args) {
        XlsxtoXML.parse(1, 0, 2, "Nume.xlsx", "Antenna" );
        
    }
    
    public static void parse(int nameColID, int timeColID, int msgColID, String fileName, String topic) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//            System.out.println("Name " + args[0] + " Time " + args[1] + " Message " + args[2]);
//            System.out.println("File " + args[3] + " Topic " + args[4]);
            File file;
            file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            String person;
            
            Dialog temp = new Dialog(new LinkedList<Person>(), new ArrayList<String>(), new ArrayList<Turn>(), 19);

            XSSFWorkbook myWorkBook = new XSSFWorkbook(file);
            ArrayList<String> people = new ArrayList<String>();
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            System.out.println(mySheet);
            int genid = 1;
            int next_person = 0;
            try {
                Iterator<Row> iter = mySheet.rowIterator();
                iter.next();
                while (iter.hasNext()) {
                    Row r = iter.next();
                    System.out.println("Ceva    ");
                    if(!people.contains(r.getCell(nameColID).getStringCellValue().replace(" ", ""))) {
                        temp.getParticipants().add(new Person(r.getCell(nameColID).getStringCellValue().replace(" ", "")));
                        people.add(r.getCell(nameColID).getStringCellValue().replace(" ", ""));
                    }
                    temp.getBody().add(new Turn(r.getCell(nameColID).getStringCellValue().replace(" ", ""), new Utterance(genid, 0, sdf.format(r.getCell(timeColID).getDateCellValue()), r.getCell(msgColID).getStringCellValue())));
                    genid++;
                    //System.out.println(r.getCell(1).getStringCellValue().replace(" ", "") + " " + r.getCell(0).getDateCellValue().toString() + " " + r.getCell(2).getStringCellValue());
                }
            }
            catch(NullPointerException ex) {
                System.out.println("You ought to clean up this xlsx");
            }
            finally {
                temp.getTopics().add(topic);
                jaxbMarshaller.marshal(temp, new File(fileName.replace(".xlsx", ".xml")));

            }
        }
        catch(JAXBException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvalidFormatException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
