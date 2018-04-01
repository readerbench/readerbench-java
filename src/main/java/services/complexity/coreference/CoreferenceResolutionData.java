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
package services.complexity.coreference;

/**
	 * class used for computing all the data needed by these
	 * features. We prefer this usage because we don't want to perform all the
	 * operations every time we need a feature
	 * 
	 * @author Mihai Alexandru Ortelecan
	 * 
	 *
 */
public class CoreferenceResolutionData {

    /*
		 * A big span is considered to be a span with size >= PROPORTION *
		 * docLenght
     */
    public static final float PROPORTION = 0.3f;

    /* no... per document */
    private int noChains;
    private int noCoreferences;
    private int noEntities;
    private int noWords;
    /* the spans of every chain summed */
    private int totalSizeOfSpan;

    /* big span means that the span is >= PROPORTION * docLength */
    private int noChainsWithBigSpan;

    public int getNoChains() {
        return noChains;
    }

    public void setNoChains(int noChains) {
        this.noChains = noChains;
    }

    public int getNoCoreferences() {
        return noCoreferences;
    }

    public void setNoCoreferences(int noCoreferences) {
        this.noCoreferences = noCoreferences;
    }

    public int getNoEntities() {
        return noEntities;
    }

    public void setNoEntities(int noEntities) {
        this.noEntities = noEntities;
    }

    public int getNoWords() {
        return noWords;
    }

    public void setNoWords(int noWords) {
        this.noWords = noWords;
    }

    public int getTotalSizeOfSpan() {
        return totalSizeOfSpan;
    }

    public void setTotalSizeOfSpan(int totalSizeOfSpan) {
        this.totalSizeOfSpan = totalSizeOfSpan;
    }

    public int getNoChainsWithBigSpan() {
        return noChainsWithBigSpan;
    }

    public void setNoChainsWithBigSpan(int noChainsWithBigSpan) {
        this.noChainsWithBigSpan = noChainsWithBigSpan;
    }

}
