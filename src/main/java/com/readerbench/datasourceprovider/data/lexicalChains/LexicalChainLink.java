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
package com.readerbench.datasourceprovider.data.lexicalChains;

import com.readerbench.datasourceprovider.data.Word;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @authors Ioana Serban, Mihai Dascalu
 */
public class LexicalChainLink implements Serializable {

    private static final long serialVersionUID = 63732297667987014L;

    private final Word word;
    private final String senseId;
    private LexicalChain lexicalChain;
    private final HashMap<LexicalChainLink, Double> connections;
    private double value = 0;

    public LexicalChainLink(Word word, String senseId) {
        this.word = word;
        this.senseId = senseId;
        this.connections = new HashMap<>();
    }

    public void addConnection(LexicalChainLink link, double weight) {
        connections.put(link, weight);
        value += weight;
    }

    public void removeConnection(LexicalChainLink link) {
        double weight = connections.remove(link);
        value -= weight;
    }

    public boolean hasSameWord(LexicalChainLink link) {
        return word.equals(link.getWord());
        // return word.equals(link.getWord()) &&
        // senseId.equals(link.getSenseId());
    }

    public Word getWord() {
        return word;
    }

    public String getSenseId() {
        return senseId;
    }

    public HashMap<LexicalChainLink, Double> getConnections() {
        return connections;
    }

    public double getValue() {
        return value;
    }

    public LexicalChain getLexicalChain() {
        return lexicalChain;
    }

    public void setLexicalChain(LexicalChain lexicalChain) {
        this.lexicalChain = lexicalChain;
    }

    @Override
    public String toString() {
        String s = "";
        s += getWord().getText() + "[" + getSenseId() + "]: ";
        for (Map.Entry<LexicalChainLink, Double> e : connections.entrySet()) {
            s += "(" + e.getKey().getSenseId() + "["
                    + e.getKey().getWord().getText() + "], " + e.getValue()
                    + ") ";
        }
        return s;
    }
}
