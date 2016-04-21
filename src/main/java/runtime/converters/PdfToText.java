package runtime.converters;

import org.junit.Test;

import services.converters.PdfToTextConverter;

public class PdfToText {

	@Test
	public void test() {
		System.out.println("Starting PDF read ...");
		//String text = PdfToTextConverter.pdftoText("resources/cv/fox.pdf");
		//String text = PdfToTextConverter.pdftoText("resources/cv/286-45-CVMAIL-108079-CV-1_CV_ZANGRILLI_Camille_novembre_2015.pdf");
		//String text = PdfToTextConverter.pdftoText("resources/papers/MS_training_SE_1999.pdf", true);
		
		PdfToTextConverter pdfConverter = new PdfToTextConverter();
		String text = pdfConverter.pdftoText("http://www.pdf995.com/samples/pdf.pdf", false);
		
		System.out.println("Textul este = " + text);
	}

}
