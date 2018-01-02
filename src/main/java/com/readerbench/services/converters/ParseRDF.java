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
package com.readerbench.services.converters;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.readerbench.services.nlp.listOfWords.Dictionary;
import com.readerbench.services.semanticModels.LSA.LSA;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class ParseRDF {

	private static final int MIN_NO_OCCURRENCES = 5;

	public static void parse(String inPath, String outPath) {
		// parse the XML file

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(outPath)), "UTF-8"), 32768);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			InputSource input = new InputSource(new FileInputStream(new File(
					inPath)));
			input.setEncoding("UTF-8");
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);

			Element doc = dom.getDocumentElement();

			// determine whether the document is a document or a chat
			NodeList nl;
			Element el;
			Map<String, Integer> newConcepts = new TreeMap<String, Integer>();
			nl = doc.getElementsByTagName("body");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);

					if (el.getFirstChild() != null) {
						String[] texts = el.getFirstChild().getNodeValue()
								.toLowerCase().trim().split("\n");
						for (String text : texts) {
							text = text.trim();
							if (text.length() > LSA.LOWER_BOUND) {
								out.write(text + "\n");

								StringTokenizer st = new StringTokenizer(text,
										" \\.,:;!?-+[](){}'’“”\"");

								// determine new concepts
								while (st.hasMoreTokens()) {
									String word = st.nextToken()
											.replaceAll("[^a-z]", "").trim();
									if (word.length() > 0
											&& !Dictionary
													.getDictionaryEn()
													.getWords().contains(word)) {
										if (newConcepts.containsKey(word))
											newConcepts.put(word,
													newConcepts.get(word) + 1);
										else
											newConcepts.put(word, 1);
									}
								}
							}
						}
					}
				}
			}

			// write new concepts
			for (Entry<String, Integer> entry : newConcepts.entrySet()) {
				if (entry.getValue() >= MIN_NO_OCCURRENCES)
					System.out.println(entry.getKey() + "-" + entry.getValue());
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ParseRDF.parse("in/LAK_corpus/LAK-DATASET-DUMP.rdf",
				"in/LAK_corpus/LAK_dataset.txt");
	}
}
