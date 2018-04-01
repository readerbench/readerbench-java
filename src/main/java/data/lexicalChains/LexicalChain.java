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
package data.lexicalChains;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import data.Word;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @authors Ioana Serban, Mihai Dascalu
 */
public class LexicalChain implements Serializable {
	private static final long serialVersionUID = -4724528858130546429L;

	private final Set<LexicalChainLink> links = new HashSet<>();
	private final HashMap<LexicalChainLink, HashMap<LexicalChainLink, Double>> distanceMap = new HashMap<>();

	public boolean addLink(LexicalChainLink link) {
		link.setLexicalChain(this);
		return links.add(link);
	}

	public boolean containsWord(Word word) {
		for (LexicalChainLink link : links) {
			if (link.getWord() == word)
				return true;
		}
		return false;
	}

	public LexicalChainLink getLink(Word word) {
		for (LexicalChainLink link : links) {
			if (link.getWord() == word)
				return link;
		}
		return null;
	}

	/**
	 * Applies a Floyd-Warshall algorithm to the lexical chain to determine
	 * distances between all words in the chain.
	 */
	public void computeDistances() {
		// initialize distanceMap structure
		for (LexicalChainLink link : links) {
			distanceMap.put(link, new HashMap<LexicalChainLink, Double>());
		}
		for (Iterator<LexicalChainLink> it = links.iterator(); it.hasNext();) {
			LexicalChainLink link = it.next();
			for (Map.Entry<LexicalChainLink, Double> e : link.getConnections()
					.entrySet()) {
				// set distances between links that are directly connected
				// the distance of an edge in the graph is the inverse of the
				// weight in the disambiguation graph
				setDistance(link, e.getKey(), (1.0 / e.getValue()));
			}
		}

		// apply the Floyd-Warshall algorithm
		for (Iterator<LexicalChainLink> it1 = links.iterator(); it1.hasNext();) {
			LexicalChainLink link1 = it1.next();
			for (Iterator<LexicalChainLink> it2 = links.iterator(); it2
					.hasNext();) {
				LexicalChainLink link2 = it2.next();
				for (Iterator<LexicalChainLink> it3 = links.iterator(); it3
						.hasNext();) {
					LexicalChainLink link3 = it3.next();
					double d1 = getDistance(link2, link3);
					double d2 = getDistance(link2, link1)
							+ getDistance(link1, link3);
					if (d2 < d1) {
						setDistance(link2, link3, d2);
					}
				}
			}
		}
	}

	public double getDistance(LexicalChainLink link1, LexicalChainLink link2) {
		HashMap<LexicalChainLink, Double> mapLink1 = distanceMap.get(link1);
		Double distance = mapLink1.get(link2);
		if (distance == null) {
			return Double.MAX_VALUE;
		}
		return distance;
	}

	public void setDistance(LexicalChainLink link1, LexicalChainLink link2,
			Double distance) {
		distanceMap.get(link1).put(link2, distance);
	}

    @Override
	public String toString() {
        Map<String, Integer> count = new HashMap<>();
        for (LexicalChainLink link : links) {
            String word = link.getWord().getLemma();
            if (!count.containsKey(word)) count.put(word, 1);
            else count.put(word, count.get(word) + 1);
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
		result = prime * result
				+ ((distanceMap == null) ? 0 : distanceMap.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		return result;
	}

	public Set<LexicalChainLink> getLinks() {
		return links;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LexicalChain other = (LexicalChain) obj;
		if (distanceMap == null) {
			if (other.distanceMap != null)
				return false;
		} else if (!distanceMap.equals(other.distanceMap))
			return false;
		if (links == null) {
			if (other.links != null)
				return false;
		} else if (!links.equals(other.links))
			return false;
		return true;
	}
}
