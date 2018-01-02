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
package com.readerbench.services.ageOfExposure;

import com.readerbench.services.converters.GenericTasaDocument;
import com.readerbench.services.converters.SplitTASA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SplitTASAincrementalFiles {

	private static final Logger LOGGER = LoggerFactory.getLogger(SplitTASAincrementalFiles.class);

	public static void parseTasaFromSingleFile(String input, String path, boolean usePOStagging,
			boolean annotateWithPOS) throws FileNotFoundException, IOException {
		createFolders(path);
		List<GenericTasaDocument> docs = SplitTASA.getTASAdocs(input, path);

		StringBuilder[] outputs = new StringBuilder[SplitTASA.NO_GRADE_LEVELS];
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			outputs[i] = new StringBuilder();
		}

		int complexityClass;
		StringBuilder content;

		for (GenericTasaDocument doc : docs) {
			complexityClass = GenericTasaDocument.get13GradeLevel(doc.getDRPscore());
			if (complexityClass >= 1 && complexityClass <= SplitTASA.NO_GRADE_LEVELS) {
				content = doc.getProcessedContent(usePOStagging, annotateWithPOS);
				for (int i = complexityClass - 1; i < SplitTASA.NO_GRADE_LEVELS; i++) {
					if (content != null)
						outputs[i].append(content + "\n");
				}
			}
		}

		// write all files
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path + "/grade" + i + "/alltexts[1-" + (i + 1) + "].txt")), "UTF-8"),
					32768);
			out.write(outputs[i].toString());
			out.close();
		}
	}

	private static void createFolders(String path) {
		// delete all potential class folders as well
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			File dir = new File(path + "/grade" + i);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					f.delete();
				dir.delete();
			}
			dir.mkdir();
		}
	}
	
	public static void createFilesMixed(String path, String output) throws IOException {
		createFoldersMixed(output);
		
		int[] tokens = {
			429000, 779000, 1088000, 1447000,
			2038000, 2668000, 2867000, 3063000,
			3270000, 3661000, 4024000, 4194000,
			5431000
		};
		
		ArrayList<ArrayList<String>> allDocs = new ArrayList<ArrayList<String>>();
		ArrayList<String> docs;
		
		// read all docs
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			FileInputStream inputFile = new FileInputStream(path + "/grade" + i + "/alltexts[1-" + (i + 1) + "].txt");
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line = "";
			
			docs = new ArrayList<String>();
			
			while ((line = in.readLine()) != null) {
				docs.add(line);
			}
			
			in.close();
			allDocs.add(docs);
		}
		
		// pick random docs
		ArrayList<String> mixedFile = new ArrayList<String>();
		ArrayList<String> auxList;
		String auxString;
		int totalTokens = 0, idx = 0;
		
		HashSet<String> docSet = new HashSet<String>();
		
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			FileOutputStream outputFile = new FileOutputStream(output + "/mixed" + i + "/alltextsmixed" + (i + 1) + ".txt");
			OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
			BufferedWriter out = new BufferedWriter(ow);
			
			while (totalTokens <= tokens[i]) {
				auxList = allDocs.get(idx % SplitTASA.NO_GRADE_LEVELS);
				
				while (true) {
					auxString = auxList.get(ThreadLocalRandom.current().nextInt(0, auxList.size()));
					
					if (!docSet.contains(auxString)) {
						docSet.add(auxString);
						break;
					}
				}
				
				totalTokens += auxString.split("[ \\.\n]+").length;
				mixedFile.add(auxString);
				idx++;
			}
			
			for (int j = 0; j < mixedFile.size(); j++) {
				out.write(mixedFile.get(j));
				out.newLine();
			}
			out.close();
		}
	}
	
	private static void createFoldersMixed(String path) {
		// delete all potential class folders as well
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			File dir = new File(path + "/mixed" + i);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					f.delete();
				dir.delete();
			}
			dir.mkdir();
		}
	}

	public static void main(String[] args) {

		/*
		try {
			SplitTASAincrementalFiles.parseTasaFromSingleFile("tasa.txt", "resources/in/AoE", false, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		try {
			SplitTASAincrementalFiles.createFilesMixed("resources/in/AoE HDP full", "resources/in/AoE Mixed");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
