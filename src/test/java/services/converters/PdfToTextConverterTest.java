package services.converters;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PdfToTextConverterTest {

	private static Logger logger = Logger.getLogger(PdfToTextConverter.class);
	private final PrintStream stdout = System.out;
	private final ByteArrayOutputStream output = new ByteArrayOutputStream();
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}
	
	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test
	public void test() {
		System.out.println("alo");
		fail("test");
		
		logger.info("Starting test");
		/*try {
			System.out.println("Start processing");
			//PdfToTextConverter pttc = new PdfToTextConverter("resources/cv/fox.pdf");
			//System.out.println(pttc.pdftoText("resources/cv/fox.pdf"));
		} catch (IOException e) {
			fail("IOException");
			e.printStackTrace();
		}*/
		
		String text = PdfToTextConverter.pdftoText("resources/cv/fox.pdf");
		System.out.println("Textul este = " + text);
		
		assertEquals("hello", outContent.toString());
		
	}

}
