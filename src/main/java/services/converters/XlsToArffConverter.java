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
