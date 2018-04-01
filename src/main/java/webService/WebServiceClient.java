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
package webService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author Mihai Dascalu
 *
 */
public class WebServiceClient {

	/**
	 * @param query
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static HttpGet getTopicsRequest(
			String query,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging
			) throws UnsupportedEncodingException {
		return new HttpGet(
				"http://localhost:" + 
				ReaderBenchServer.PORT + 
				"/getTopics?q=" + URLEncoder.encode(query, "UTF-8") + 
				"&lsa=" + URLEncoder.encode(pathToLSA, "UTF-8") + 
				"&lda=" + URLEncoder.encode(pathToLDA, "UTF-8") + 
				"&lang=" + URLEncoder.encode(lang, "UTF-8") + 
				"&postagging=" + usePOSTagging
				);
	}

	/**
	 * @param query
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static HttpGet getSentimentRequest(
			String query,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging
			) throws UnsupportedEncodingException {
		return new HttpGet(
				"http://localhost:" + 
				ReaderBenchServer.PORT + 
				"/getSentiment?q=" + URLEncoder.encode(query, "UTF-8") + 
				"&lsa=" + URLEncoder.encode(pathToLSA, "UTF-8") + 
				"&lda=" + URLEncoder.encode(pathToLDA, "UTF-8") + 
				"&lang=" + URLEncoder.encode(lang, "UTF-8") + 
				"&postagging=" + usePOSTagging
				);
	}

	/**
	 * @param query
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static HttpGet getComplexityRequest(
			String query,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging
			) throws UnsupportedEncodingException {
		return new HttpGet(
				"http://localhost:" + 
				ReaderBenchServer.PORT + 
				"/getComplexity?q=" + URLEncoder.encode(query, "UTF-8") + 
				"&lsa=" + URLEncoder.encode(pathToLSA, "UTF-8") + 
				"&lda=" + URLEncoder.encode(pathToLDA, "UTF-8") + 
				"&lang=" + URLEncoder.encode(lang, "UTF-8") + 
				"&postagging=" + usePOSTagging
				);
	}

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	public static void processQuery(HttpGet request) {
		try {
			HttpClient client = new DefaultHttpClient();

			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = fact.newDocumentBuilder();
			Document doc = db.parse(new InputSource(rd));

			printDocument(doc, System.out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		
		Logger.getLogger("").setLevel(Level.INFO); // changing log level

		try {
			String query = "Life is extremely good!\nIn this chapter, I shall investigate Wittgenstein's private language argument, that is, the argument to be found in Philosophical Investigations. Roughly, this argument is intended to show that a language knowable to one person and only that person is impossible; in other words, a language which another person cannot understand isn't a language. Given the prolonged debate sparked by these passages, one must have good reason to bring it up again. I have: Wittgenstein's attack on private languages has regularly been misinterpreted. Moreover, it has been misinterpreted in a way that draws attention away from the real force of his arguments and so undercuts the philosophical significance of these passages.\nWhat is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance).";
			String lsa = "";
			String lda = "";
			String lang = "";
			boolean postagging = false;

			processQuery(getTopicsRequest(query, lsa, lda, lang, postagging));
			processQuery(getSentimentRequest(query, lsa, lda, lang, postagging));
			processQuery(getComplexityRequest(query, lsa, lda, lang, postagging));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
