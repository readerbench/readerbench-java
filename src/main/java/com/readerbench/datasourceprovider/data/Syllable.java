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
package com.readerbench.datasourceprovider.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Valentin Cioaca
 */
public class Syllable implements Serializable, Comparable<Syllable> {

    private final String text;
    private final List<String> symbols;
    private final boolean primaryStressed;
    private final boolean secondaryStressed;

    public Syllable(String syll) {
        text = syll;
        symbols = new ArrayList<>(Arrays.asList(syll.trim().split("\\s+")));
        primaryStressed = syll.contains("1");
        secondaryStressed = syll.contains("2");
    }

    public String getText() {
        return text;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public boolean isPrimaryStressed() {
        return primaryStressed;
    }

    public boolean isSecondaryStressed() {
        return secondaryStressed;
    }

    @Override
    public String toString() {
        return symbols.stream().map(Object::toString).collect(Collectors.joining(" ")).trim();
    }

    @Override
    public int compareTo(Syllable s) {
        return text.compareTo(s.getText());
    }
}
