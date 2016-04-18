package runtime.timeSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.cscl.CSCLCriteria;
import services.commons.Formatting;

public class KeyStrokeLoginTimeSeries {
	static Logger logger = Logger.getLogger(KeyStrokeLoginTimeSeries.class);
	private String path;
	private int timebox; // seconds
	private int writingTime; // seconds

	public KeyStrokeLoginTimeSeries(String path, int timebox, int writingTime) {
		super();
		this.path = path;
		this.timebox = timebox;
		this.writingTime = writingTime;
	}

	public void parseDataFile() {
		File myFile = new File(path + "/KL_analysis.xlsx");

		try {
			FileInputStream fis = new FileInputStream(myFile);
			// Finds the workbook instance for XLSX file
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

			// Return first sheet from the XLSX workbook
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = mySheet.iterator();

			int rowNo = 1;
			// ignore header line
			if (rowIterator.hasNext())
				rowIterator.next();

			String prevStudId = null;
			String prevEssay = null;
			int timeSeriesLength = writingTime / timebox;

			double[] timeSeries = new double[timeSeriesLength];
			logger.info("Computing time series with " + timebox + " seconds timeboxes having in total "
					+ timeSeriesLength + " entries...");

			// write output header
			BufferedWriter out = new BufferedWriter(new FileWriter(path + "/output_" + timebox + "s.csv", false));
			out.write("StudID");

			for (int i = 0; i < timeSeriesLength; i++) {
				out.write(",Timebox " + (i + 1));
			}
			for (CSCLCriteria crit : CSCLCriteria.values()) {
				out.write("," + crit.getDescription());
			}
			out.write("\n");

			Set<String> studs = new TreeSet<String>();
			String studID = null, essay = null;
			long timestamp;
			int tts;

			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				studID = row.getCell(1).getStringCellValue();

				essay = row.getCell(5).getStringCellValue();

				timestamp = (long) row.getCell(4).getNumericCellValue();

				tts = (int) row.getCell(7).getNumericCellValue();

				// check if new student
				if ((prevStudId == null || !studID.equals(prevStudId))
						&& (prevEssay == null || !essay.equals(prevEssay))) {
					// display current results
					if (prevStudId != null) {
						printStud(prevStudId, timeSeriesLength, timeSeries, out);
					}
					// initialize a new student
					prevStudId = studID;
					prevEssay = essay;
					timeSeries = new double[timeSeriesLength];
				}
				int slot = tts / (timebox * 1000);
				if (slot < 0 || slot >= timeSeriesLength) {
					logger.error("Improper timestamp for " + studID + ": " + timestamp + " error of "
							+ (tts - writingTime * 1000) / 1000 + " seconds");
					if (tts > (writingTime + 30) * 1000)
						studs.add(studID);
				} else {
					timeSeries[slot]++;
				}

				if (rowNo % 10000 == 0) {
					logger.info("Finished processing " + rowNo + " rows...");
				}
				rowNo++;
			}
			printStud(studID, timeSeriesLength, timeSeries, out);
			logger.error("Students with problems");
			for (String stud : studs) {
				logger.error(stud);
			}
			out.close();
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Finished processing all rows...");
	}

	private void printStud(String prevStudId, int timeSeriesLength, double[] timeSeries, BufferedWriter out)
			throws IOException {
		out.write(prevStudId);
		for (int i = 0; i < timeSeriesLength; i++) {
			out.write("," + timeSeries[i]);
		}

		for (CSCLCriteria crit : CSCLCriteria.values()) {
			out.write("," + Formatting.formatNumber(CSCLCriteria.getValue(crit, timeSeries)));
		}
		out.write("\n");
	}

	public static void main(String[] args) {
		KeyStrokeLoginTimeSeries ksl = new KeyStrokeLoginTimeSeries("resources/in/KL", 300, 25 * 60);
		ksl.parseDataFile();
	}
}
