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
package com.readerbench.coreservices.cna.extendedcna;

import com.readerbench.datasourceprovider.commons.Formatting;

import java.util.Objects;

public class AuthorPairDistanceContainer implements Comparable<AuthorPairDistanceContainer> {

    private SingleAuthorContainer firstAuthor;
    private SingleAuthorContainer secondAuthor;
    private double similarity;

    public AuthorPairDistanceContainer(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor,
            double similarity) {
        super();
        this.firstAuthor = firstAuthor;
        this.secondAuthor = secondAuthor;
        this.similarity = similarity;
    }

    public SingleAuthorContainer getFirstAuthor() {
        return firstAuthor;
    }

    public SingleAuthorContainer getSecondAuthor() {
        return secondAuthor;
    }

    public double getSimilarity() {
        return this.similarity;
    }

    @Override
    public int compareTo(AuthorPairDistanceContainer o) {
        return (new Double(o.getSimilarity())).compareTo(this.getSimilarity());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null) {
            return false;
        }
        AuthorPairDistanceContainer o = (AuthorPairDistanceContainer) obj;
        return (this.getFirstAuthor().isSameAuthor(o.getFirstAuthor().getAuthor()) && this.getSecondAuthor().isSameAuthor(o.getSecondAuthor().getAuthor()))
                || (this.getFirstAuthor().isSameAuthor(o.getSecondAuthor().getAuthor()) && this.getSecondAuthor().isSameAuthor(o.getFirstAuthor().getAuthor()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.firstAuthor);
        hash = 19 * hash + Objects.hashCode(this.secondAuthor);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.similarity) ^ (Double.doubleToLongBits(this.similarity) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return Formatting.formatNumber(this.getSimilarity()) + ":\n\t- "
                + this.getFirstAuthor().getAuthor().getAuthorName() + "\n\t- "
                + this.getSecondAuthor().getAuthor().getAuthorName() + "\n";
    }
}
