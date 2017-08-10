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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import data.CVStructure;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.datavec.api.berkeley.Triple;
import org.openide.util.Exceptions;

public class PdfToTxtConverter {

    private static final Logger LOGGER = Logger.getLogger("");
    
    private String fileName;
    private String url;
    private String parsedText;
    private String cleanedText;

    private Integer noPages;
    private Integer noParagraphs;
    private Integer noSentences;
    private Integer noWords;
    private Integer noContentWords;
    private Integer noImages;
    private Integer noColors;
    private Integer noFontTypes;          // font types with text styles
    private Integer noFontTypesSimple;    // font types withouth text styles
    private Integer noFontSizes;
    private Double minFontSize;
    private Double maxFontSize;
    private Integer noTotalChars;
    private Integer noBoldChars;
    private Double pctBoldChars;
    private Integer noItalicChars;
    private Double pctItalicChars;
    private Integer noBoldItalicChars;
    private Double pctBoldItalicChars;
    private Map<String, String> socialNetworkLinks;
    
    public PdfToTxtConverter(String resourcePath, boolean isFile) {
        if (isFile) fileName = resourcePath;
        else url = resourcePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParsedText() {
        return parsedText;
    }

    public String getCleanedText() {
        return cleanedText;
    }
    
    public Integer getNoPages() {
        return noPages;
    }

    public Integer getNoParagraphs() {
        return noParagraphs;
    }
    
    public Integer getNoSentences() {
        return noSentences;
    }
    
    public Integer getNoWords() {
        return noWords;
    }
    
    public Integer getNoContentWords() {
        return noContentWords;
    }
    
    public Integer getNoImages() {
        return noImages;
    }

    public Integer getNoColors() {
        return noColors;
    }

    public Integer getNoFontTypes() {
        return noFontTypes;
    }

    public Integer getNoFontTypesSimple() {
        return noFontTypesSimple;
    }

    public Integer getNoFontSizes() {
        return noFontSizes;
    }

    public Double getMinFontSize() {
        return minFontSize;
    }

    public Double getMaxFontSize() {
        return maxFontSize;
    }

    public Integer getNoTotalChars() {
        return noTotalChars;
    }

    public Integer getNoBoldChars() {
        return noBoldChars;
    }

    public Double getPctBoldChars() {
        return pctBoldChars;
    }

    public Integer getNoItalicChars() {
        return noItalicChars;
    }

    public Double getPctItalicChars() {
        return pctItalicChars;
    }

    public Integer getNoBoldItalicChars() {
        return noBoldItalicChars;
    }

    public Double getPctBoldItalicChars() {
        return pctBoldItalicChars;
    }

    public Map<String, String> getSocialNetworkLinks() {
        return socialNetworkLinks;
    }

    /**
     * Extract text from PDF resource.
     */
    public void process() {
        PDFParser pdfParser;
        PDFTextStripper pdfStripper;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        File tmpFile;
        try {
            if (getUrl() != null && !getUrl().isEmpty()) {
                // decode URL
                URL requestUrl = new URL(URLDecoder.decode(getUrl(), "UTF-8"));
                
                // create tmp file and save URL contets
                StringBuilder sb = new StringBuilder();
                sb.append("tmp/").append(System.currentTimeMillis()).append('_');
                sb.append(FilenameUtils.getBaseName(getUrl())).append('.');
                sb.append(FilenameUtils.getExtension(getUrl()));
                fileName = sb.toString();
                sb.setLength(0);
                tmpFile = new File(fileName);
                FileUtils.copyURLToFile(requestUrl, tmpFile);
            } else {
                // process local file
                tmpFile = new File(fileName);
                if (!tmpFile.isFile()) {
                    System.err.println("File " + fileName + " does not exist.");
                    System.exit(-1);
                }
            }
            
            pdfParser = new PDFParser(new RandomAccessBufferedFileInputStream(new FileInputStream(tmpFile)));
            Map<Double, Integer> charsPerSize;
            Map<String, Integer> charsPerFont;
            
            try (PrintWriter txtWriter = new PrintWriter(fileName.replace(".pdf", ".txt"), "UTF-8")) {
                charsPerSize = new HashMap<>();
                charsPerFont = new HashMap<>();
                pdfParser.parse();
                cosDoc = pdfParser.getDocument();
                CVStructure cvStructure = new CVStructure();
                pdfStripper = new PDFTextStripper() {

                    @Override
                    protected void processTextPosition(TextPosition text) {

                        String fontName = text.getFont().getName();

//                        if(text.getUnicode().hashCode() != 32) {
//                            String toWrite = text.getUnicode() + " (" + text.getUnicode().hashCode() + "), x=" +
//                                    text.getX() + ", y=" + text.getY() + ", Height=" + text.getHeight() +
//                                    ", width=" + text.getWidth() + ", font=" + text.getFontSizeInPt() + ", font=" + text.getFont().toString()
//                                    + "\n";
//                            try {
//                                Files.write(Paths.get("./fileName.txt"), toWrite.getBytes(), StandardOpenOption.APPEND);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        if(text.getUnicode().hashCode() != 32 && !fontName.contains("Wingdings")) {
                            if (text.getY() != cvStructure.getLastYCoord()) {
                                int start = 0;
                                for( int i = 0; i < fontName.length(); i++) {
                                    if(fontName.charAt(i) == '+') {
                                        start = i + 1;
                                    }
                                }
                                cvStructure.addYCoord(new Triple(text.getY(), text.getFontSizeInPt(), fontName.substring(start)));
                            }
                        }

                        if (!charsPerFont.containsKey(fontName)) {
                            charsPerFont.put(fontName, 1);
                        } else {
                            charsPerFont.put(fontName, charsPerFont.get(fontName) + 1);
                        }
                        
                        double fontSize = text.getFontSize();
                        if (!charsPerSize.containsKey(fontSize)) {
                            charsPerSize.put(fontSize, 1);
                        } else {
                            charsPerSize.put(fontSize, charsPerSize.get(fontSize) + 1);
                        }
                        super.processTextPosition(text);
                    }
                };
                
                // settings to fix paragraph selection issues
                pdfStripper.setLineSeparator(" ");
                pdfStripper.setParagraphEnd("\n");
                
                pdDoc = new PDDocument(cosDoc);
                noPages = pdDoc.getNumberOfPages();
                
                noImages = 0;
                for (PDPage page : pdDoc.getPages()) {
                    PDResources pdResources = page.getResources();
                    // get number of images on this page
                    int images = (int) StreamSupport.stream(pdResources.getXObjectNames().spliterator(), true)
                            .filter(x -> pdResources.isImageXObject(x))
                            .count();
                    noImages += images;
                }
                
                ColorTextStripper stripper = new ColorTextStripper(txtWriter);
                noColors = stripper.getCharsPerColor().size();
                parsedText = pdfStripper.getText(pdDoc);
                noParagraphs = cvStructure.getParagraphs();
                cvStructure.getSentences(parsedText);
                txtWriter.write(parsedText);
            }

            Map<String, Integer> simpleFonts = new HashMap<>();
            Iterator<String> it = charsPerFont.keySet().iterator();
            noBoldChars = 0;
            noItalicChars = 0;
            noBoldItalicChars = 0;
            noTotalChars = 0;
            while (it.hasNext()) {
                String fontKey = it.next();
                String fontNameSimple;

                // determine simple fonts
                if (fontKey.contains("+")) {
                    if (fontKey.contains("-")) {
                        if (fontKey.toLowerCase().contains("bold")) {
                            fontNameSimple = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("d") + 1);
                        } else if (fontKey.toLowerCase().contains("italic")) {
                            fontNameSimple = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("c") + 1);
                        } else {
                            fontNameSimple = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"));
                        }
                    } else if (fontKey.contains(",")) {
                        fontNameSimple = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf(","));
                    } else {
                        fontNameSimple = fontKey.substring(fontKey.indexOf("+") + 1);
                    }
                } else {
                    if (fontKey.contains("-")) {
                        if (fontKey.toLowerCase().contains("bold")) {
                            fontNameSimple = fontKey.substring(0, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("d") + 1);
                        } else if (fontKey.toLowerCase().contains("italic")) {
                            fontNameSimple = fontKey.substring(0, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("c") + 1);
                        } else {
                            fontNameSimple = fontKey.substring(0, fontKey.indexOf("-"));
                        }
                    } else if (fontKey.contains(",")) {
                        fontNameSimple = fontKey.substring(0, fontKey.indexOf(","));
                    } else {
                        fontNameSimple = fontKey;
                    }
                }
                if (simpleFonts.containsKey(fontNameSimple)) {
                    simpleFonts.put(fontNameSimple, simpleFonts.get(fontNameSimple) + charsPerFont.get(fontKey));
                } else {
                    simpleFonts.put(fontNameSimple, charsPerFont.get(fontKey));
                }

                // determine bold, italic and bold italic chars
                if (fontKey.toLowerCase().contains("bolditalic")) {
                    noBoldItalicChars += charsPerFont.get(fontKey);
                } else if (fontKey.toLowerCase().contains("bold")) {
                    noBoldChars += charsPerFont.get(fontKey);
                } else if (fontKey.toLowerCase().contains("italic")) {
                    noItalicChars += charsPerFont.get(fontKey);
                }
                noTotalChars += charsPerFont.get(fontKey);
            }
            it.remove();

            noFontTypes = charsPerFont.size();
            noFontSizes = charsPerSize.size();
            noFontTypesSimple = simpleFonts.size();
            if (noTotalChars > 0) pctItalicChars = noItalicChars * 1.0 / noTotalChars;
            else pctItalicChars = 0.0;
            if (noTotalChars > 0) pctBoldChars = noBoldChars * 1.0 / noTotalChars;
            else pctBoldChars = 0.0;
            if (noBoldItalicChars > 0) pctBoldItalicChars = noBoldItalicChars * 1.0 / noBoldItalicChars;
            else pctBoldItalicChars = 0.0;
            if (charsPerSize.size() > 0) {
                maxFontSize = Collections.max(charsPerSize.keySet());
                minFontSize = Collections.min(charsPerSize.keySet());
            } else {
                maxFontSize = 0.0;
                minFontSize = 0.0;
            }

            pdDoc.close();
        } catch (IOException e) {
            System.err.println("Unable to open PDF Parser. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An exception occured in parsing the PDF Document." + e.getMessage());
        } finally {
            try {
                if (cosDoc != null) {
                    cosDoc.close();
                }
                if (pdDoc != null) {
                    pdDoc.close();
                }
            } catch (IOException e) {
                System.err.println("Unable to close PDF Parser. " + e.getMessage());
            }
        }
    }

    /**
     * Removes the lines of the processed text that contain at least one 
     * occurrence of a word in a list
     *
     * @param words A set of words
     * @return The cleaned string
     */
    public void removeLines(Set<String> words) {
        StringBuilder sbNewText = new StringBuilder();
        try {
            BufferedReader bufferReader = new BufferedReader(new StringReader(parsedText));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                boolean cleanLine = true;
                for (String word : words) {
                    if (line.contains(word)) {
                        cleanLine = false;
                        break;
                    }
                }
                if (cleanLine) {
                    sbNewText.append(line).append("\n");
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        cleanedText = sbNewText.toString();
    }

    /**
     * Extracts social network links from the parsed text given a list of social
     * networks to look for
     * @param socialNetworks    The list of social networks to look for
     */
    public void extractSocialLinks(Set<String> socialNetworks) {
        socialNetworkLinks = new HashMap<>();
        try {
            BufferedReader bufferReader = new BufferedReader(new StringReader(parsedText));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                for (String socialNetwork : socialNetworks) {
                    if (socialNetworkLinks.get(socialNetwork) == null) {
                        int pos;
                        if (-1 != (pos = line.toLowerCase().indexOf(socialNetwork.toLowerCase()))) {
                            int start = line.toLowerCase().substring(0, pos).lastIndexOf("http");
                            int end = line.indexOf(' ', pos);
                            socialNetworkLinks.put(socialNetwork, line.substring(start, end));
                            break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Checks whether the parsed text contains at least one string of a set 
     * (aimed for section titles)
     * @param sectionTitles The list of strings (section titles) to look for
     * @return  true or false, whether the parsed text contains at least one of 
     *          the strings in the set
     */
    public boolean sectionExists(Set<String> sectionTitles) {
        String lowerParsedText = parsedText.toLowerCase();
        Set<String> lowerSectionTitles = new HashSet<>();
        sectionTitles.forEach((sectionTitle) -> {
            lowerSectionTitles.add(sectionTitle.toLowerCase());
        });
        try {
            BufferedReader bufferReader = new BufferedReader(new StringReader(lowerParsedText));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                for (String sectionTitle : lowerSectionTitles) {
                    if(line.contains(sectionTitle)) return true;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
}
