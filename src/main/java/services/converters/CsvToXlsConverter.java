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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class CsvToXlsConverter {

    static Logger logger = Logger.getLogger("");

    public static void convertCsvToXls(String path) throws IOException {
        List<List<String>> arrayLines = new ArrayList<List<String>>();
        List<String> arrayValues = new ArrayList<String>();

        String thisLine;
        String outputPath = path.replaceAll("\\.csv", "\\.xls");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // DataInputStream myInput = new DataInputStream(fis);
        BufferedReader myInput = new BufferedReader(new InputStreamReader(fis));
        // arList = new ArrayList();
        while ((thisLine = myInput.readLine()) != null) {
            arrayValues = new ArrayList<String>();
            String strar[] = thisLine.split(",");
            for (int j = 0; j < strar.length; j++) {
                arrayValues.add(strar[j]);
            }
            arrayLines.add(arrayValues);
        }
        try {
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("new sheet");
            for (int k = 0; k < arrayLines.size(); k++) {
                List<String> ardata = arrayLines.get(k);
                HSSFRow row = sheet.createRow(k);
                for (int p = 0; p < ardata.size(); p++) {
                    HSSFCell cell = row.createCell(p);
                    String data = ardata.get(p);
                    if (data.startsWith("=")) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        data = data.replaceAll("\"", "");
                        data = data.replaceAll("=", "");
                        cell.setCellValue(data);
                    } else if (data.startsWith("\"")) {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(data);
                    } else {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(data);
                    }
                }
            }
            FileOutputStream fileOut = new FileOutputStream(outputPath);
            hwb.write(fileOut);
            fileOut.close();
            //hwb.close();
            logger.info("Your xls file has been generated!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
