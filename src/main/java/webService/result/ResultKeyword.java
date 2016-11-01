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
package webService.result;

import java.util.Comparator;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultKeyword implements Comparable<ResultKeyword> {

    private String name;
    private int noOccurences;
    private double relevance;

    public ResultKeyword(String name, int noOccurences, double relevance) {
        this.name = name;
        this.noOccurences = noOccurences;
        this.relevance = relevance;
    }

    public String getName() {
        return name;
    }

    public int getNoOccurences() {
        return noOccurences;
    }

    public double getRelevance() {
        return relevance;
    }

    @Override
    public int compareTo(ResultKeyword o) {
        // Reverse order
        return (int) Math.signum(this.getRelevance() - o.getRelevance());
    }

    public static Comparator<ResultKeyword> ResultKeywordRelevanceComparator = new Comparator<ResultKeyword>() {

        public int compare(ResultKeyword o1, ResultKeyword o2) {
            // descending order
            return o2.compareTo(o1);
        }

    };

}
