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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import webService.ReaderBenchServer;

public class PdfToTextConverter {

    private static Logger logger = Logger.getLogger("");

    // number of pages
    private Integer pages;

    // number of paragraphs
    private Integer paragraphs;

    private Integer sentences;

    private Integer words;

    private Integer contentWords;

    public PdfToTextConverter() {
        this.imagesPerPage = new HashMap<>();
        this.colorsPerPage = new HashMap<>();
    }

    public Integer getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(Integer paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Integer getSentences() {
        return sentences;
    }

    public void setSentences(Integer sentences) {
        this.sentences = sentences;
    }

    public Integer getWords() {
        return words;
    }

    public void setWords(Integer words) {
        this.words = words;
    }

    public Integer getContentWords() {
        return contentWords;
    }

    public void setContentWords(Integer contentWords) {
        this.contentWords = contentWords;
    }

    // number of images
    private Integer images;

    // number of images per page
    private Map<Integer, Integer> imagesPerPage;

    // number of colors per page
    private Map<Integer, Integer> colorsPerPage;

    // number of colors
    private Integer colors;

    private Integer fontTypes; // contains font types with text styles
    private Integer fontTypesSimple; // contains font types withouth text styles
    private Integer fontSizes;
    private Float minFontSize;
    private Float maxFontSize;
    private Integer totalCharacters;
    private Integer boldCharacters;
    private Integer italicCharacters;
    private Integer boldItalicCharacters;

    public Integer getFontTypes() {
        return fontTypes;
    }

    public void setFontTypes(Integer fontTypes) {
        this.fontTypes = fontTypes;
    }

    public Integer getFontTypesSimple() {
        return fontTypesSimple;
    }

    public void setFontTypesSimple(Integer fontTypesSimple) {
        this.fontTypesSimple = fontTypesSimple;
    }

    public Integer getFontSizes() {
        return fontSizes;
    }

    public void setFontSizes(Integer fontSizes) {
        this.fontSizes = fontSizes;
    }

    public Float getMinFontSize() {
        return minFontSize;
    }

    public void setMinFontSize(Float minFontSize) {
        this.minFontSize = minFontSize;
    }

    public Float getMaxFontSize() {
        return maxFontSize;
    }

    public void setMaxFontSize(Float maxFontSize) {
        this.maxFontSize = maxFontSize;
    }

    public Integer getTotalCharacters() {
        return totalCharacters;
    }

    public void setTotalCharacters(Integer totalCharacters) {
        this.totalCharacters = totalCharacters;
    }

    public Integer getBoldCharacters() {
        return boldCharacters;
    }

    public void setBoldCharacters(Integer boldCharacters) {
        this.boldCharacters = boldCharacters;
    }

    public Integer getItalicCharacters() {
        return italicCharacters;
    }

    public void setItalicCharacters(Integer italicCharacters) {
        this.italicCharacters = italicCharacters;
    }

    public Integer getBoldItalicCharacters() {
        return boldItalicCharacters;
    }

    public void setBoldItalicCharacters(Integer boldItalicCharacters) {
        this.boldItalicCharacters = boldItalicCharacters;
    }

    public Integer getColors() {
        return colors;
    }

    public void setColors(Integer colors) {
        this.colors = colors;
    }

    final StringBuffer extractedText = new StringBuffer();

    // Extract text from PDF Document
    public String pdftoText(String fileName, boolean isFile) {
        PDFParser parser;
        String parsedText = null;
        PDFTextStripper pdfStripper = null;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        File file;
        try {
            if (!isFile) {

                URL url;
                URLDecoder.decode(fileName, "UTF-8");
                url = new URL(fileName);

                StringBuilder sb = new StringBuilder();
                sb.append("tmp/");
                sb.append(System.currentTimeMillis());
                sb.append('_');
                sb.append(FilenameUtils.getBaseName(url.toString()));
                sb.append('.');
                sb.append(FilenameUtils.getExtension(url.toString()));
                fileName = sb.toString();
                file = new File(fileName);
                FileUtils.copyURLToFile(url, file);

                parser = new PDFParser(new RandomAccessBufferedFileInputStream(new FileInputStream(file)));

            } else {
                file = new File(fileName);
                if (!file.isFile()) {
                    System.err.println("File " + fileName + " does not exist.");
                    return null;
                }

                parser = new PDFParser(new RandomAccessBufferedFileInputStream(new FileInputStream(file)));
            }

            PrintWriter out = new PrintWriter(fileName.replace(".pdf", ".txt"), "UTF-8");

            // structures used for extraction
            Map<Float, Integer> fontSizes = new HashMap<>();
            Map<String, Integer> fontStats = new HashMap<>();

            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition text) {
                    String fontName = text.getFont().getName();
                    float fontSize = text.getFontSize();
                    if (!fontStats.containsKey(fontName)) {
                        fontStats.put(fontName, 1);
                    } else {
                        fontStats.put(fontName, fontStats.get(fontName) + 1);
                    }
                    if (!fontSizes.containsKey(fontSize)) {
                        fontSizes.put(fontSize, 1);
                    } else {
                        fontSizes.put(fontSize, fontSizes.get(fontSize) + 1);
                    }
                    super.processTextPosition(text);
                }
            };
            pdfStripper.setLineSeparator(" ");
            pdfStripper.setParagraphEnd("\n");
            pdDoc = new PDDocument(cosDoc);

            // get number of pages
            this.pages = pdDoc.getNumberOfPages();

            // get number of images
            // get number of colors
            //PDFStreamEngine engine = new PDFStreamEngine(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PageDrawer.properties", false));
            // PageDrawer pd = new PageDrawer();
            // PDGraphicsState graphicState = pd.getGraphicsState();
            //List<Float> colors = new ArrayList<Float>();
            logger.info("Incep procesarea paginilor");
            int k = 1;
            ColorTextStripper stripper = new ColorTextStripper(out);
            this.images = 0;
            this.colors = 0;
            // iterate through pages
            for (PDPage page : pdDoc.getPages()) {

                //out.write(page.getContents().getByteArray());
                PDResources pdResources = page.getResources();
                // get number of images on this page and save them for later normalization				
                int images = (int) StreamSupport.stream(pdResources.getXObjectNames().spliterator(), true)
                        .filter(x -> pdResources.isImageXObject(x))
                        .count();
                this.imagesPerPage.put(k, images);

                // add tot total number of images
                this.images += images;

                /*engine.processStream(page, page.findResources(), page.getContents().getStream());
				PDGraphicsState graphicState = engine.getGraphicsState();
				logger.info("Procesez pagina noua");
				
				float colorSpaceValues[] = graphicState.getStrokingColor().getColorSpaceValue();
				logger.info("Culori: " + graphicState.getStrokingColor().getColorSpaceValue());
				logger.info("Am " + colorSpaceValues.length + " culori pe aceasta pagina");
				for (float c : colorSpaceValues) {
					logger.info("Incerc sa adaug culoarea: " + (c * 255));
					Float f = new Float(c * 255);
					if (!colors.contains(f)) {
						logger.info("Am adaugat culoarea " + f);
						colors.add(f);
					}
				}*/
                // get number of colors on this page and save them for later normalization	
                // extract text on this page
                // use the following to extract text on page if the above technique does not work
                /*pdfStripper.setStartPage(k);
				pdfStripper.setEndPage(k);
				String textOnPage = pdfStripper.getText(pdDoc);*/
                k++;
            }

            String text = stripper.getText(pdDoc);
            //logger.info("Culori textuale: " + text);
            logger.info("Numar culori document: " + stripper.getCharsPerColor().size());

            this.colors = stripper.getCharsPerColor().size();

            parsedText = pdfStripper.getText(pdDoc);
            out.write(parsedText);
            out.close();

            Iterator<String> it = fontStats.keySet().iterator();
            Map<String, Integer> simpleFonts = new HashMap<>();

            int totalCharacters = 0, bold = 0, italic = 0, boldItalic = 0;

            while (it.hasNext()) {
                String fontKey = it.next();
                String fontType;

                // Parsing fontNames which contain Bold, Italic in name and
                // adding the number of chars to the actual total
                if (fontKey.contains("+")) {
                    if (fontKey.contains("-")) {
                        if (fontKey.toLowerCase().contains("bold")) {
                            fontType = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("d") + 1);
                            // System.out.println("fontType1.0: " + fontType);
                        } else if (fontKey.toLowerCase().contains("italic")) {
                            fontType = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("c") + 1);
                            // System.out.println("fontType1.1: " + fontType);
                        } else {
                            fontType = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf("-"));
                        }
                    } else if (fontKey.contains(",")) {
                        fontType = fontKey.substring(fontKey.indexOf("+") + 1, fontKey.indexOf(","));
                        // System.out.println("fontType2: " + fontType);
                    } else {
                        fontType = fontKey.substring(fontKey.indexOf("+") + 1);
                        // System.out.println("fontType3: " + fontType);
                    }
                } else {
                    if (fontKey.contains("-")) {
                        if (fontKey.toLowerCase().contains("bold")) {
                            fontType = fontKey.substring(0, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("d") + 1);
                            // System.out.println("fontType4.0: " + fontType);
                        } else if (fontKey.toLowerCase().contains("italic")) {
                            fontType = fontKey.substring(0, fontKey.indexOf("-"))
                                    + fontKey.substring(fontKey.lastIndexOf("c") + 1);
                            // System.out.println("fontType4.1: " + fontType);
                        } else {
                            fontType = fontKey.substring(0, fontKey.indexOf("-"));
                        }
                    } else if (fontKey.contains(",")) {
                        fontType = fontKey.substring(0, fontKey.indexOf(","));
                        // System.out.println("fontType5: " + fontType);
                    } else {
                        fontType = fontKey;
                        // System.out.println("fontType6: " + fontKey);
                    }
                }

                // System.out.println("Remastered " + fontKey.toLowerCase());
                if (simpleFonts.containsKey(fontType)) {
                    // System.out.println("The Value: " +
                    // simpleFonts.get(fontType) + " " +
                    // fontStats.get(fontKey));
                    // fontStats.put(fontKey, simpleFonts.get(fontType) +
                    // fontStats.get(fontKey));
                    simpleFonts.put(fontType, simpleFonts.get(fontType) + fontStats.get(fontKey));
                } else {
                    simpleFonts.put(fontType, fontStats.get(fontKey));
                }

                if (fontKey.toLowerCase().contains("bolditalic")) {
                    // System.out.println("BOLDITALIC Style: " + fontKey + ".
                    // And number of chars: " + fontStats.get(fontKey));
                    boldItalic += fontStats.get(fontKey);
                } else if (fontKey.toLowerCase().contains("bold")) {
                    // System.out.println("BOLD Style: " + fontKey + ". And
                    // number of chars: " + fontStats.get(fontKey));
                    bold += fontStats.get(fontKey);
                } else if (fontKey.toLowerCase().contains("italic")) {
                    // System.out.println("ITALIC Style: " + fontKey + ". And
                    // number of chars: " + fontStats.get(fontKey));
                    italic += fontStats.get(fontKey);
                }
                totalCharacters += fontStats.get(fontKey);
            }

            Iterator<String> fit = simpleFonts.keySet().iterator();
            while (fit.hasNext()) {
                String ffontKey = fit.next();
                System.out.println("font: " + ffontKey + ", and number of chars: " + simpleFonts.get(ffontKey));

            }

            this.setFontTypes(fontStats.size());
            this.setFontSizes(fontSizes.size());
            logger.info("*****Styles*****");
            logger.info("Total: " + totalCharacters + " characters");
            this.setTotalCharacters(totalCharacters);
            logger.info("Bold: " + bold + " characters");
            this.setBoldCharacters(bold);
            logger.info("Italic: " + italic + " characters");
            this.setItalicCharacters(italic);
            logger.info("BoldItalic: " + boldItalic + " characters");
            this.setBoldItalicCharacters(boldItalic);
            logger.info("How many fonts did I find: " + simpleFonts.size());
            this.setFontTypesSimple(simpleFonts.size());

            // Sort FontSizes
            logger.info("\n*****FontSizes*****");

            if (fontSizes.size() > 0) {
                Collections.max(fontSizes.keySet());
                logger.info("MaxFont: " + Collections.max(fontSizes.keySet()));
                this.setMaxFontSize(Collections.max(fontSizes.keySet()));
                logger.info("MinFont: " + Collections.min(fontSizes.keySet()));
                this.setMinFontSize(Collections.min(fontSizes.keySet()));
            } else {
                logger.info("MaxFont: " + "N/A");
                this.setMaxFontSize(new Float(0.0));
                logger.info("MinFont: " + "N/A");
                this.setMinFontSize(new Float(0.0));
            }

            // replace all single \n's with space; multiple \ns means new paragraph
            // OLD method - should be replaced
            //parsedText = parsedText.replaceAll("([^\n]+)([\n])([^\n ]+)", "$1 $3");
            // OLD method ends here
            // NEW method
            // append current line to previous line if previous line does not end with a "." character
            /*String lines[] = parsedText.split("\\r?\\n");
            StringBuilder documentBuilder = new StringBuilder();
            StringBuilder lineBuilder = new StringBuilder();
            logger.info("Extracted " + lines.length + " lines from document.");
            for(String line : lines) {
            	logger.info("Processing line " + line);
            	line = line.trim();
            	if (line.length() == 0) continue;
            	// if there is a dot at the end of the line, stop the string builder
            	if (line.charAt(line.length() - 1) == '.') {
					documentBuilder.append(lineBuilder.toString());
            		documentBuilder.append("\n");
            		lineBuilder.setLength(0);
            	}
            	// if there is no dot at the end of the line, append the next string to this line
            	else {
            		lineBuilder.append(line);
            	}
            }
            if (lineBuilder.length() != 0) {
            	documentBuilder.append(lineBuilder.toString());
            	documentBuilder.append("\n");
            }
            parsedText = documentBuilder.toString();*/
            // NEW method ends here

            /*StringBuilder paragraph = new StringBuilder(); 
			for (int i = 0; i < parsedText.length(); i++) {
				if (parsedText.charAt(i) == '\n') {
					
					// new paragraph
					paragraph = new StringBuilder();
				}
				else {
					paragraph.append(parsedText.charAt(i));
				}
			}*/
            // debug purposes
            pdDoc.close();

        } catch (IOException e) {
            System.err.println("Unable to open PDF Parser. " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An exception occured in parsing the PDF Document." + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (cosDoc != null) {
                    cosDoc.close();
                }
                if (pdDoc != null) {
                    pdDoc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return parsedText;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getImages() {
        return images;
    }

    public void setImages(Integer images) {
        this.images = images;
    }

}
