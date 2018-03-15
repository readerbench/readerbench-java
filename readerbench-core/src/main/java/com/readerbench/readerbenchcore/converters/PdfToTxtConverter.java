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
package com.readerbench.readerbenchcore.converters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.StreamSupport;

public class PdfToTxtConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfToTxtConverter.class);
    
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
        CVPDFTextStripper pdfStripper;
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
            
            try (PrintWriter txtWriter = new PrintWriter(fileName.replace(".pdf", ".txt"), "UTF-8")) {
                pdfParser.parse();
                cosDoc = pdfParser.getDocument();
                pdfStripper = new CVPDFTextStripper();
                
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
                stripper.getText(pdDoc);
                noColors = stripper.getCharsPerColor().size();
                parsedText = pdfStripper.getText(pdDoc);
                noParagraphs = pdfStripper.getCvStructure().getParagraphs();
                pdfStripper.getCvStructure().getSentences(parsedText);
                txtWriter.write(parsedText);
            }

            Map<String, Integer> simpleFonts = new HashMap<>();
            Iterator<String> it = pdfStripper.getCharsPerFont().keySet().iterator();
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
                    simpleFonts.put(fontNameSimple, simpleFonts.get(fontNameSimple) + pdfStripper.getCharsPerFont().get(fontKey));
                } else {
                    simpleFonts.put(fontNameSimple, pdfStripper.getCharsPerFont().get(fontKey));
                }

                // determine bold, italic and bold italic chars
                if (fontKey.toLowerCase().contains("bolditalic")) {
                    noBoldItalicChars += pdfStripper.getCharsPerFont().get(fontKey);
                } else if (fontKey.toLowerCase().contains("bold")) {
                    noBoldChars += pdfStripper.getCharsPerFont().get(fontKey);
                } else if (fontKey.toLowerCase().contains("italic")) {
                    noItalicChars += pdfStripper.getCharsPerFont().get(fontKey);
                }
                noTotalChars += pdfStripper.getCharsPerFont().get(fontKey);
            }
            it.remove();

            noFontTypes = pdfStripper.getCharsPerFont().size();
            noFontSizes = pdfStripper.getCharsPerSize().size();
            noFontTypesSimple = simpleFonts.size();
            if (noTotalChars > 0) pctItalicChars = noItalicChars * 1.0 / noTotalChars;
            else pctItalicChars = 0.0;
            if (noTotalChars > 0) pctBoldChars = noBoldChars * 1.0 / noTotalChars;
            else pctBoldChars = 0.0;
            if (noBoldItalicChars > 0) pctBoldItalicChars = noBoldItalicChars * 1.0 / noBoldItalicChars;
            else pctBoldItalicChars = 0.0;
            if (pdfStripper.getCharsPerSize().size() > 0) {
                maxFontSize = Collections.max(pdfStripper.getCharsPerSize().keySet());
                minFontSize = Collections.min(pdfStripper.getCharsPerSize().keySet());
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
                line.trim();
                for (String sectionTitle : lowerSectionTitles) {
                    if(line.equals(sectionTitle)) return true;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
}
