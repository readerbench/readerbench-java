package services.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.ResourceLoader;

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

	// number of colors
	private Integer colors;

	public Integer getColors() {
		return colors;
	}

	public void setColors(Integer colors) {
		this.colors = colors;
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

			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
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
			for (PDPage page : list) {
			    
				PDResources pdResources = page.getResources();
				this.images = pdResources.getImages().size();
				
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
				 
			}
			
			ColorTextStripper stripper = new ColorTextStripper();
			String text = stripper.getText(pdDoc);
			logger.info("Culori textuale: " + text);
			logger.info("Numar culori document: " + stripper.getCharsPerColor().size());
			
			this.colors = stripper.getCharsPerColor().size();

			// pdfStripper.setStartPage(1);
			// pdfStripper.setEndPage(5);
			parsedText = pdfStripper.getText(pdDoc);
			System.out.println(parsedText);

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
