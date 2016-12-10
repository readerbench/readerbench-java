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
package services.complexity.wordComplexity;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import java.util.function.Function;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.readability.Syllable;
import services.semanticModels.WordNet.OntologySupport;
import vu.wntools.wordnet.WordnetData;

public class WordComplexity extends ComplexityIndex {

    private transient final BiFunction<Word, Lang, ? extends Number> f;
    
    public WordComplexity(ComplexityIndicesEnum index, Lang lang, BiFunction<Word, Lang, ? extends Number> f) {
        super(index, lang);
        this.f = f;
    }
    
    public WordComplexity(ComplexityIndicesEnum index, Lang lang, Function<Word, ? extends Number> f) {
        super(index, lang);
        this.f = ((Word w, Lang l) -> f.apply(w));
    }

	public static int getSyllables(Word word) {
		return Syllable.syllable(word.getLemma());
	}

	public static int getPolysemyCount(Word word) {
		if (OntologySupport.getWordSenses(word) == null) {
			return 0;
		}
		return OntologySupport.getWordSenses(word).size();
	}

	/**
	 * Gets the distance to the root of the hypernym tree. If the word was
	 * disambiguated it starts with the senseId that was determined. Otherwise
	 * it uses the first sense id returned by WordNet. We go up the hypernym
	 * tree always selecting the first hypernym returned by WordNet under the
	 * assumption that it is the most likely one.
	 */
	public static int getMaxDistanceToHypernymTreeRoot(Word word, Lang lang) {
		String senseId;

		// if word was disambiguated
		if (word.getLexicalChainLink() != null) {
			senseId = word.getLexicalChainLink().getSenseId();
		} else {
			// get the first sense for the word
			senseId = OntologySupport.getFirstSense(word);
		}
		WordnetData dictionary = OntologySupport.getDictionary(word);
		ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
		if (dictionary.hyperRelations.containsKey(senseId)) {
			dictionary.getMultipleHyperChain(senseId, targetChains);
			return targetChains.stream().mapToInt(ArrayList::size).max().orElse(0);
		}
		return 0;
	}

	public static double getAverageDistanceToHypernymTreeRoot(Word word, Lang lang) {
		String senseId;

		// if word was disambiguated
		if (word.getLexicalChainLink() != null) {
			senseId = word.getLexicalChainLink().getSenseId();
		} else {
			// get the first sense for the word
			senseId = OntologySupport.getFirstSense(word);
		}
		return OntologySupport.getDictionary(word).getAverageDepthBySynset(senseId);
	}

	public static int getPathCountToHypernymTreeRoot(Word word, Lang lang) {
		String senseId;

		// if word was disambiguated
		if (word.getLexicalChainLink() != null) {
			senseId = word.getLexicalChainLink().getSenseId();
		} else {
			// get the first sense for the word
			senseId = OntologySupport.getFirstSense(word);
		}
		WordnetData dictionary = OntologySupport.getDictionary(word);
		ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
		if (dictionary.hyperRelations.containsKey(senseId)) {
			dictionary.getMultipleHyperChain(senseId, targetChains);
			return targetChains.size();
		}
		return 0;
	}

	public static int getDifferenceBetweenLemmaAndStem(Word word) {
		return Math.abs(word.getLemma().length() - word.getStem().length());
	}

	public static double getDifferenceBetweenWordAndStem(Word word) {
		return Math.abs(word.getText().length() - word.getStem().length());
	}

	public static double getAverageComplexity(AbstractDocument d, BiFunction<Word, Lang, ? extends Number> f) {
		double distanceSum = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			distanceSum += f.apply(e.getKey(), d.getLanguage()).doubleValue() * e.getValue();
			totalWords += e.getValue();
		}
		return (totalWords > 0 ? distanceSum / totalWords : 0);
	}

	@Override
    public double compute(AbstractDocument d) {
        return getAverageComplexity(d, f);
    }
}
