package services.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ExcelLoader;

public class XlsToArffConverter {
	public static void convertXlsToArff(String path) {
		String outputPath = path.replaceAll("xls", "arff");

		ExcelLoader excelLoader = new ExcelLoader();
		try {
			excelLoader.setSource(new FileInputStream(path));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		excelLoader.setMissingValue("?");

		Instances data = null;
		try {
			data = excelLoader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// save ARFF
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		try {
			saver.setFile(new File(outputPath));
			// saver.setDestination(new File(outputPath));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
