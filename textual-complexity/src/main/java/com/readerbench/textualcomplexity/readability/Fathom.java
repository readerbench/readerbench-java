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
package com.readerbench.textualcomplexity.readability;

import com.readerbench.coreservices.rhythm.Syllable;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Readability and general measurements of English text.
 * </p>
 * 
 * <p>
 * Ported from perl's Lingua::EN::Fathom by Kim Ryan.
 * </p>
 * 
 * <p>
 * <b>COPYRIGHT</b><br>
 * Distributed under the same terms as Perl.
 * </p>
 * 
 * @author Kim Ryan
 * @author Larry Ogrodnek &lt;ogrodnek@gmail.com&gt;
 * 
 * @version $Revision: 1.1 $ $Date: 2009/11/04 12:20:08 $
 */
public final class Fathom {
	private static final String[] abbreviations = new String[] {
			// personal titles
			"Mr", "Mrs", "M", "Dr", "Prof", "Det", "Insp",
			// Commercial abbreviations
			"Pty", "PLC", "Ltd", "Inc",
			// Other abbreviations
			"etc", "vs", };

	public static Stats analyze(final File f) throws IOException {
		return analyze(new FileInputStream(f));
	}

	public static Stats analyze(final InputStream s) throws IOException {
		return analyze(new InputStreamReader(s));
	}

	public static Stats analyze(final Reader r) throws IOException {
		final BufferedReader br = new BufferedReader(r);

		try {
			final Stats ret = new Stats();

			String line;

			while ((line = br.readLine()) != null) {
				analyzeWords(ret, line);
			}

			return ret;
		} finally {
			br.close();
		}
	}

	public static Stats analyze(final String s) {
		return analyzeWords(new Stats(), s);
	}

	public static Stats analyze(final Stats stats, final String s) {
		return analyzeWords(stats, s);
	}

	// Word found, such as: twice, BOTH, a, I'd, non-plussed ..
	// Ignore words like K12, &, X.Y.Z ...

	private static final Pattern WORD_PAT = Pattern
			.compile("\\b([a-z][-'a-z]*)\\b");

	private static final Pattern VOWELS = Pattern.compile("[aeiouy]");

	private static final Pattern VALID_HYPHENS = Pattern
			.compile("[a-z]{2,}-[a-z]{2,}");

	private static final Pattern END_SENTENCE = Pattern
			.compile("\\b\\s*[.!?]\\s*\\b");

	private static final Pattern END_SENTENCE_END_LINE = Pattern
			.compile("\\b\\s*[.!?]\\s*$");

	public static Stats analyzeWords(final Stats _stats, final String _s) {
		final Stats stats = _stats == null ? new Stats() : _stats;

		String s = _s.toLowerCase().trim();

		Matcher m = WORD_PAT.matcher(s);

		while (m.find()) {
			final String word = m.group(1);

			// Try to filter out acronyms and abbreviations by accepting
			// words with a vowel sound. This won't work for GPO etc.
			if (!VOWELS.matcher(word).find()) {
				continue;
			}

			// Test for valid hyphenated word like be-bop
			if (word.indexOf('-') > 0
					&& (!VALID_HYPHENS.matcher(word).matches())) {
				continue;
			}

			// word frequency count
			stats.addWord(word);

			final int syl = Syllable.syllable(word);

			stats.numSyllables += syl;

			// Required for Fog index, count non hyphenated words of 3 or more
			// syllables. Should add check for proper names in here as well
			if (syl > 2 && word.indexOf('-') < 0) {
				stats.numComplexWords++;
			}
		}

		// replace common abbreviations to ease the search for end of sentence.
		s = replaceAbbr(s);

		// clean out quotes for same reason
		s.replaceAll("[\"']", "");

		// Search for '.', '?' or '!' to end a sentence.
		m = END_SENTENCE.matcher(s);

		stats.numSentences++;

		while (m.find()) {
			stats.numSentences++;
		}

		// Check for final sentence, with no following words.
		m = END_SENTENCE_END_LINE.matcher(s);

		if (m.find()) {
			stats.numSentences++;
		}

		return stats;
	}

	private static final String replaceAbbr(final String s) {
		String ret = s;

		for (final String a : abbreviations) {
			ret = ret.replaceAll("\\s" + a + "\\.\\s", a);
		}

		return ret;
	}

	/**
	 * @author mihai.dascalu
	 */
	public static final class Stats {
		/**
		 * @uml.property name="numWords"
		 */
		private int numWords = 0;
		/**
		 * @uml.property name="numSentences"
		 */
		private int numSentences = 0;
		/**
		 * @uml.property name="numTextLines"
		 */
		private final int numTextLines = 0;
		/**
		 * @uml.property name="numBlankLines"
		 */
		private final int numBlankLines = 0;
		/**
		 * @uml.property name="numSyllables"
		 */
		private int numSyllables = 0;
		/**
		 * @uml.property name="numComplexWords"
		 */
		private int numComplexWords = 0;

		/**
		 * @uml.property name="uniqueWords"
		 */
		private final Map<String, Integer> uniqueWords = new HashMap<String, Integer>();

		public Stats() {
		}

		@Override
		public String toString() {
			return String
					.format("Stats:[words: %d, sentences: %d, text: %d, blank: %d, syllables: %d, complex: %d]",
							this.numWords, this.numSentences,
							this.numTextLines, this.numBlankLines,
							this.numSyllables, this.numComplexWords);
		}

		private void addWord(final String s) {
			final Integer i = this.uniqueWords.get(s);
			this.uniqueWords.put(s, i == null ? 1 : 1 + i.intValue());
			this.numWords++;
		}

		/**
		 * @return
		 * @uml.property name="numBlankLines"
		 */
		public int getNumBlankLines() {
			return this.numBlankLines;
		}

		/**
		 * @return
		 * @uml.property name="numSentences"
		 */
		public int getNumSentences() {
			return this.numSentences;
		}

		/**
		 * @return
		 * @uml.property name="numTextLines"
		 */
		public int getNumTextLines() {
			return this.numTextLines;
		}

		/**
		 * @return
		 * @uml.property name="numWords"
		 */
		public int getNumWords() {
			return this.numWords;
		}

		/**
		 * @return
		 * @uml.property name="numComplexWords"
		 */
		public int getNumComplexWords() {
			return this.numComplexWords;
		}

		/**
		 * @return
		 * @uml.property name="numSyllables"
		 */
		public int getNumSyllables() {
			return this.numSyllables;
		}

		/**
		 * @return
		 * @uml.property name="uniqueWords"
		 */
		public Map<String, Integer> getUniqueWords() {
			return Collections.unmodifiableMap(this.uniqueWords);
		}
	}
}
