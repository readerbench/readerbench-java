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
package runtime.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openide.util.Exceptions;
import static runtime.cv.CVAnalyzer.LOGGER;

import services.converters.PdfToTxtConverter;

public class PdfToText {

    @Test
    public void test() {
        System.out.println("Starting PDF read ...");
        PdfToTxtConverter pdfConverter = new PdfToTxtConverter("http://www.pdf995.com/samples/pdf.pdf", false);
        pdfConverter.process();
        System.out.println("Extracted text:\n" + pdfConverter.getParsedText());
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.err.println("Path to the folder containing PDF files MUST be provided!");
            System.exit(-1);
        }
        try {
            // iterate through all PDF CV files
            Files.walk(Paths.get(args[0])).forEach(filePath -> {
                // TODO: replace with mimetype check
                if (filePath.toString().contains(".pdf")) {
                    String fileName = filePath.getFileName().toString();
                    PdfToTxtConverter pdfToTextConverter = new PdfToTxtConverter(filePath.toString(), true);
                    pdfToTextConverter.process();
                    File txtFile = new File(args[0] + fileName.replace(".pdf", ".txt"));
                    try {
                        FileUtils.writeStringToFile(txtFile, pdfToTextConverter.getParsedText(), "UTF-8");
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "Exception: {0}", ex.getMessage());
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            LOGGER.log(Level.INFO, "Finished converting PDF files to txt files");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception: {0}", ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }
}
