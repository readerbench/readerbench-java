package services.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.ResourceLoader;

import org.apache.commons.lang3.StringUtils;
import webService.ReaderBenchServer;

public class PdfToTextConverter {

	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	
	// number of pages
	private Integer pages;

	// number of paragraphs
	private Integer paragraphs;

	private Integer sentences;

	private Integer words;

	private Integer contentWords;
	
	public PdfToTextConverter() {
		this.imagesPerPage = new HashMap<Integer, Integer>();
		this.colorsPerPage = new HashMap<Integer, Integer>();
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
	
	// average number of images per page
	private double avgImagesPerPage;

	// number of colors per page
	private Map<Integer, Integer> colorsPerPage;
	
	// average number of colors per page
	private double avgColorsPerPage;
		
	// number of colors
	private Integer colors;

	public Integer getColors() {
		return colors;
	}

	public void setColors(Integer colors) {
		this.colors = colors;
	}

	public double getAvgImagesPerPage() {
		return avgImagesPerPage;
	}

	public void setAvgImagesPerPage(double avgImagesPerPage) {
		this.avgImagesPerPage = avgImagesPerPage;
	}

	public double getAvgColorsPerPage() {
		return avgColorsPerPage;
	}

	public void setAvgColorsPerPage(double avgColorsPerPage) {
		this.avgColorsPerPage = avgColorsPerPage;
	}

	// Extract text from PDF Document
	public String pdftoText(String fileName, boolean isFile) {
		PDFParser parser;
		String parsedText = null;
		;
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = null;
		try {
			if (!isFile) {
				
				URL url;

				FileSystemManager fsManager = VFS.getManager();
				FileObject fileobject = fsManager.resolveFile(fileName);
				fileobject.createFile();

				FileContent filecontent = fileobject.getContent();
				parser = new PDFParser(filecontent.getInputStream());

				/*
				 * java.util.Date date = new java.util.Date(); Timestamp
				 * timestamp = new Timestamp(date.getTime());
				 * 
				 * file = new File("resources/temporar" + timestamp + ".pdf");
				 * 
				 * if (!file.exists()) { file.createNewFile(); } filecontent.get
				 * 
				 * FileOutputStream out = new FileOutputStream(file);
				 * out.write(filecontent.getOutputStream()); out.close();
				 * 
				 * file = new File(filecontent.toString());
				 * System.out.println(filecontent.toString());
				 */
				URLDecoder.decode(fileName, "UTF-8"); // use this instead
				url = new URL(fileName);
				file = new File(url.getFile());

			} else {
				file = new File(fileName);
				if (!file.isFile()) {
					System.err.println("File " + fileName + " does not exist.");
					return null;
				}

				parser = new PDFParser(new FileInputStream(file));
			}
			
			PrintWriter out = new PrintWriter(fileName.replace(".pdf", ".txt"), "UTF-8");

			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdfStripper.setLineSeparator(" ");
			pdfStripper.setParagraphEnd("\n");
			pdDoc = new PDDocument(cosDoc);

			// get number of pages
			this.pages = pdDoc.getNumberOfPages();

			// get number of images
			// get number of colors
			List<PDPage> list = pdDoc.getDocumentCatalog().getAllPages();
			PDFStreamEngine engine = new PDFStreamEngine(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PageDrawer.properties", false));
			// PageDrawer pd = new PageDrawer();
			// PDGraphicsState graphicState = pd.getGraphicsState();
			//List<Float> colors = new ArrayList<Float>();
			logger.info("Incep procesarea paginilor");
			int k = 1;
			ColorTextStripper stripper = new ColorTextStripper(out);
			this.images = 0;
			this.colors = 0;
			// iterate through pages
			for (PDPage page : list) {
				
				//out.write(page.getContents().getByteArray());
			    
				PDResources pdResources = page.getResources();
				// get number of images on this page and save them for later normalization				
				int images = pdResources.getImages().size();
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
				int colors = stripper.getCharsPerColor().size();
				this.colorsPerPage.put(k, colors);
				
				// extract text on this page
				
				// use the following to extract text on page if the above technique does not work
				/*pdfStripper.setStartPage(k);
				pdfStripper.setEndPage(k);
				String textOnPage = pdfStripper.getText(pdDoc);*/
				
				k++;
			}
			
			double sum = 0;
			for (Map.Entry<Integer, Integer> entry : this.imagesPerPage.entrySet()) {
			    Integer images = entry.getValue();
			    sum += images;
			}
			logger.info("Calculez average images per page, unde sum = " + sum + " si pages = " + this.imagesPerPage.size());
			this.avgImagesPerPage = sum / (this.imagesPerPage.size() * 1.0);
			logger.info("Am calculat la " + this.avgImagesPerPage);
			
			sum = 0;
			for (Map.Entry<Integer, Integer> entry : this.colorsPerPage.entrySet()) {
			    Integer colors = entry.getValue();
			    sum += colors;
			}
			this.avgColorsPerPage = sum / (this.colorsPerPage.size() * 1.0);
			
			String text = stripper.getText(pdDoc);
			//logger.info("Culori textuale: " + text);
			logger.info("Numar culori document: " + stripper.getCharsPerColor().size());
			
			this.colors = stripper.getCharsPerColor().size();

			parsedText = pdfStripper.getText(pdDoc);
			// replace all single \n's with space; multiple \ns means new paragraph
			parsedText = parsedText.replaceAll("([^\n]+)([\n])([^\n ]+)", "$1 $3");
			
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
			out.write(parsedText);
			
			out.close();
			
			pdDoc.close();

		} catch (IOException e) {
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			return null;
		} catch (Exception e) {
			System.err.println("An exception occured in parsing the PDF Document." + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
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
