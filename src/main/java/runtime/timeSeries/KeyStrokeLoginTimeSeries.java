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
package runtime.timeSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.cscl.CSCLCriteria;
import services.commons.Formatting;
import services.commons.VectorAlgebra;

public class KeyStrokeLoginTimeSeries {
	static Logger logger = Logger.getLogger(KeyStrokeLoginTimeSeries.class);

	private static final CSCLCriteria[] CRITERIA_MA = { CSCLCriteria.STDEV, CSCLCriteria.SLOPE, CSCLCriteria.ENTROPY,
			CSCLCriteria.UNIFORMITY, CSCLCriteria.LOCAL_EXTREME };
	private static final CSCLCriteria[] CRITERIA_KALMAN = { CSCLCriteria.AVERAGE, CSCLCriteria.STDEV,
			CSCLCriteria.SLOPE, CSCLCriteria.ENTROPY, CSCLCriteria.LOCAL_EXTREME };
	private String path;
	private int timeframe; // seconds
	private int writingTime; // seconds

	public KeyStrokeLoginTimeSeries(String path, int timeframe, int writingTime) {
		super();
		this.path = path;
		this.timeframe = timeframe;
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
			int timeSeriesLength = writingTime / timeframe;

			double[] timeSeries = new double[timeSeriesLength];
			logger.info("Computing time series with " + timeframe + " seconds timeboxes having in total "
					+ timeSeriesLength + " entries...");

			// write output header
			BufferedWriter out = new BufferedWriter(new FileWriter(path + "/output_" + timeframe + "s.csv", false));
			out.write("StudID");

			for (int i = 0; i < timeSeriesLength; i++) {
				out.write(",Timeframe " + (i + 1));
			}
			for (CSCLCriteria crit : CSCLCriteria.values()) {
				out.write("," + crit.getDescription());
			}

			out.write(
					",NZ Start-End,NZ Start-Max,NZ Max-End,NZ Slope Start-Max,NZ Slope Max-End, NZ Slope Start-Mid, NZ Slope Mid-End");

			// moving average
			for (int i = 0; i < timeSeriesLength; i++) {
				out.write(",MA T" + (i + 1));
			}
			for (CSCLCriteria crit : CRITERIA_MA) {
				out.write(",MA " + crit.getDescription());
			}

			// moving average
			for (int i = 0; i < timeSeriesLength; i++) {
				out.write(",Kalman T" + (i + 1));
			}
			for (CSCLCriteria crit : CRITERIA_KALMAN) {
				out.write(",Kalman " + crit.getDescription());
			}
			out.write("\n");

			// start processing students
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
				int slot = tts / (timeframe * 1000);
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
			myWorkBook.close();
			out.close();
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Finished processing all rows...");
	}

	private double[] getSubVector(double v[], int start, int end) {
		double[] result = new double[end - start + 1];

		for (int i = start; i <= end; i++)
			result[i - start] = v[i];
		return result;
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

		int start = -1, max = -1, end = -1;
		double maxVal = 0;

		// determine max, as well as first/last occurrence !=0;
		for (int i = 0; i < timeSeriesLength; i++) {
			if (timeSeries[i] > 0 && start == -1)
				start = i;
			if (timeSeries[i] > maxVal) {
				maxVal = timeSeries[i];
				max = i;
			}
			if (timeSeries[i] > 0)
				end = i;
		}

		out.write("," + ((double) (end - start + 1)) / (timeSeriesLength));
		out.write("," + ((double) (max - start + 1)) / (timeSeriesLength));
		out.write("," + ((double) (end - max + 1)) / (timeSeriesLength));
		double[] tmp = getSubVector(timeSeries, Math.max(0, start - 1), max);
		out.write("," + ((tmp.length > 1) ? Formatting.formatNumber(VectorAlgebra.slope(tmp)) : ","));

		tmp = getSubVector(timeSeries, max, Math.min(end, timeSeriesLength - 1));
		out.write("," + ((tmp.length > 1) ? Formatting.formatNumber(VectorAlgebra.slope(tmp)) : ","));

		out.write(
				"," + Formatting.formatNumber(VectorAlgebra.slope(getSubVector(timeSeries, start, (start + end) / 2))));

		out.write("," + Formatting.formatNumber(VectorAlgebra.slope(getSubVector(timeSeries, (start + end) / 2, end))));

		// apply moving average
		double[] MAseries = VectorAlgebra.movingAverage(timeSeries, 3);

		for (int i = 0; i < timeSeriesLength; i++) {
			out.write("," + MAseries[i]);
		}

		for (CSCLCriteria crit : CRITERIA_MA) {
			out.write("," + Formatting.formatNumber(CSCLCriteria.getValue(crit, MAseries)));
		}

		// get sub-series with start & end !=0 and apply
		double[] nonZero = getSubVector(timeSeries, start, end);
		double[] kalman = VectorAlgebra.applyKalmanFilter(nonZero);
		for (int i = 0; i < start; i++)
			out.write(",0");
		for (int i = 0; i < kalman.length; i++)
			out.write("," + Formatting.formatNumber(kalman[i]));
		for (int i = end + 1; i < timeSeriesLength; i++)
			out.write(",0");

		for (CSCLCriteria crit : CRITERIA_KALMAN) {
			out.write("," + Formatting.formatNumber(CSCLCriteria.getValue(crit, kalman)));
		}

		out.write("\n");
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		KeyStrokeLoginTimeSeries ksl = new KeyStrokeLoginTimeSeries("resources/in/KL", 300, 25 * 60);
		ksl.parseDataFile();
	}
}
