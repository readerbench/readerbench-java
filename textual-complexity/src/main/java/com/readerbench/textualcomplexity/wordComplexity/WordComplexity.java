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
package com.readerbench.textualcomplexity.wordComplexity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.rhythm.Syllable;
import com.readerbench.coreservices.semanticmodels.wordnet.OntologySupport;

import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

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
