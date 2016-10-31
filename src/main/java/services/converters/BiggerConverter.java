/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class BiggerConverter{
    
    public static void main(String[] args) {
        try {
            if(args.length < 5) {
                System.out.println("Not enough arguments");
                return;
            }
            int name = Integer.parseInt(args[0]);
            int time = Integer.parseInt(args[1]);
            int msg = Integer.parseInt(args[2]);
            String topics = args[4];
            System.out.println("Name " + args[0] + " Time " + args[1] + " Message " + args[2]);
            System.out.println("File " + args[3] + " Topic " + args[4]);
            File file;
            file = new File("Barnes language data (No duplicates final).xlsx");
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
            int cur = (int) mySheet.getRow(0).getCell(2).getNumericCellValue();
            try {
                Iterator<Row> iter = mySheet.rowIterator();
                iter.next();
                while (iter.hasNext()) {
                    Row r = iter.next();
                    if(r.getCell(2).getNumericCellValue() != cur) {
                        temp.getTopics().add("Thread_" + cur);
                        jaxbMarshaller.marshal(temp, new File("Thread_" + cur + ".xml"));
                        temp = new Dialog(new LinkedList<Person>(), new ArrayList<String>(), new ArrayList<Turn>(), 19);
                        people = new ArrayList<>();
                        genid = 0;
                        cur = (int) r.getCell(2).getNumericCellValue();
                    }
                    if(!people.contains(Integer.toString((int) r.getCell(name).getNumericCellValue()).replace(" ", ""))) {
                        temp.getParticipants().add(new Person(Integer.toString((int) r.getCell(name).getNumericCellValue()).replace(" ", "")));
                        people.add(Integer.toString((int) r.getCell(name).getNumericCellValue()).replace(" ", ""));
                    }
                    temp.getBody().add(new Turn(Integer.toString((int) r.getCell(name).getNumericCellValue()), new Utterance(genid, 0, r.getCell(time).getDateCellValue().toString(), r.getCell(msg).getStringCellValue())));
                    genid++;
                    //System.out.println(Integer.toString((int) r.getCell(name).getNumericCellValue()) + " " + r.getCell(time).getDateCellValue().toString() + " " + r.getCell(msg).getStringCellValue());
                }
            }
            catch(NullPointerException ex) {
                System.out.println("You ought to clean up this xlsx");
            }
            finally {
                temp.getTopics().add("Thread_" + cur);
                jaxbMarshaller.marshal(temp, new File("Thread_" + cur + ".xml"));

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
