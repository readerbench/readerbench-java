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
package data.sentiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dao.ValenceDAO;

/**
 * Holds a sentiment valence
 * 
 * @author Gabriel Gutu
 *
 */
public class SentimentValence {
	
	private static final Map<String, SentimentValence> valenceMap;
    private static final List<SentimentValence> valences = new ArrayList<>();
    public static final Map<SentimentValence, Double> maxValues = new HashMap<>();
    public static final Map<SentimentValence, Double> minValues = new HashMap<>();
    
	static {
		Map<String, SentimentValence> valenceMaplocal = new HashMap<>();
		List<data.pojo.SentimentValence> valenceEntities = ValenceDAO.getInstance().findAll();
		for (data.pojo.SentimentValence v : valenceEntities) {
			SentimentValence sv = new SentimentValence(v.getId(), v.getLabel(), v.getIndexLabel(), v.getRage());
            valences.add(sv);
			valenceMaplocal.put(v.getIndexLabel(), sv);
		}
		valenceMap = Collections.unmodifiableMap(valenceMaplocal);
        maxValues.put(valenceMap.get("scared_RAGE"), 7.7);
        maxValues.put(valenceMap.get("angry_RAGE"), 8.8);
        maxValues.put(valenceMap.get("sad_RAGE"), 16.9);
        maxValues.put(valenceMap.get("happy_RAGE"), 20.8);
        maxValues.put(valenceMap.get("excited_RAGE"), 8.7);
        maxValues.put(valenceMap.get("tender_RAGE"), 10.8);
        
        minValues.put(valenceMap.get("scared_RAGE"), -3.2);
        minValues.put(valenceMap.get("angry_RAGE"), -7.1);
        minValues.put(valenceMap.get("sad_RAGE"), -7.7);
        minValues.put(valenceMap.get("happy_RAGE"), -13.6);
        minValues.put(valenceMap.get("excited_RAGE"), -3.3);
        minValues.put(valenceMap.get("tender_RAGE"), -3.6);
        
	}
	
	private Integer id;
	private String name;
	private String indexLabel;
	private boolean rage;
	
	/**
	 * Creates a sentiment valence
	 * 
	 * @param id
	 * 			id of the sentiment valence
	 * @param name
	 * 			name of the sentiment valence
	 * @param rage
	 * 			shows if it is a RAGE valence
	 * 			or a primary one
	 */
	public SentimentValence(Integer id, String name, String index, boolean rage) {
		this.id = id;
		this.name = name;
		this.indexLabel = index;
		this.rage = rage;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getIndexLabel() {
		return indexLabel;
	}
	
	public void setIndexLabel(String index) {
		this.indexLabel = index;
	}
	
	public boolean getRage() {
		return rage;
	}
	
	public void setRage(boolean rage) {
		this.rage = rage;
	}
	
	public static Map<String, SentimentValence> getValenceMap() {
		return valenceMap;
	}
	
	public static SentimentValence get(String index) {
		return valenceMap.get(index);
	}
    
    public static List<SentimentValence> getAllValences() {
        return valences;
    }
 
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.indexLabel);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SentimentValence other = (SentimentValence) obj;
        if (!Objects.equals(this.indexLabel, other.indexLabel)) {
            return false;
        }
        return true;
    }
    
    
	
}	