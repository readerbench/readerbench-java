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
package com.readerbench.data.lexicalChains;

import com.readerbench.data.Word;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @authors Ioana Serban, Mihai Dascalu
 */
public class LexicalChain implements Serializable {

    private static final long serialVersionUID = -4724528858130546429L;

    private final Set<LexicalChainLink> links = new HashSet<>();
    
    public boolean addLink(LexicalChainLink link) {
        link.setLexicalChain(this);
        return links.add(link);
    }

    public boolean containsWord(Word word) {
        for (LexicalChainLink link : links) {
            if (link.getWord() == word) {
                return true;
            }
        }
        return false;
    }

    public LexicalChainLink getLink(Word word) {
        for (LexicalChainLink link : links) {
            if (link.getWord() == word) {
                return link;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        Map<String, Integer> count = new HashMap<>();
        for (LexicalChainLink link : links) {
            String word = link.getWord().getLemma();
            if (!count.containsKey(word)) {
                count.put(word, 1);
            } else {
                count.put(word, count.get(word) + 1);
            }
        }
        List<String> entries = count.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(e -> e.getKey() + "(" + e.getValue() + ")")
                .collect(Collectors.toList());
        return "(" + StringUtils.join(entries, ", ") + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((links == null) ? 0 : links.hashCode());
        return result;
    }

    public Set<LexicalChainLink> getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LexicalChain other = (LexicalChain) obj;
        if (links == null) {
            if (other.links != null) {
                return false;
            }
        } else if (!links.equals(other.links)) {
            return false;
        }
        return true;
    }
}
