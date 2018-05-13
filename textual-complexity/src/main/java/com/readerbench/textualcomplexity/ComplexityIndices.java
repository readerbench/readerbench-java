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
package com.readerbench.textualcomplexity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class used to define all factors to be used within the complexity evaluation
 * model
 *
 * @author Mihai Dascalu
 */
public class ComplexityIndices {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplexityIndices.class);

    public static final int IDENTITY = -1;

    public static void computeComplexityFactors(AbstractDocument d) {
        d.setComplexityIndices(
                Arrays.stream(ComplexityIndexType.values()).parallel()
                        .filter(t -> t.getFactory() != null)
                        .map(cat -> cat.getFactory())
                        .flatMap(f -> f.build(d.getLanguage()).stream())
                        .collect(Collectors.toMap(Function.identity(), f -> f.compute(d))));
    }

    public static List<ComplexityIndex> getIndices(Lang lang) {
        return Arrays.stream(ComplexityIndexType.values())
                .filter(cat -> cat.getFactory() != null)
                .map(cat -> cat.getFactory())
                .flatMap(f -> f.build(lang).stream())
                .collect(Collectors.toList());
    }

    public static double[] getComplexityIndicesArray(AbstractDocument d) {
        return ComplexityIndices.getIndices(d.getLanguage()).stream()
                .mapToDouble(index -> d.getComplexityIndices().get(index))
                .toArray();
    }

    public static void main(String[] args) {
        List<ComplexityIndex> factors = getIndices(Lang.en);
        factors.stream().forEachOrdered(f -> {
            System.out.println(f.getCategoryName() + "\t" + f.getAcronym() + "\t"
                    + f.getDescription());
        });

        System.out.println("TOTAL:" + factors.size() + " factors");
    }
}
