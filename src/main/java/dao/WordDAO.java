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
package dao;

import data.pojo.Language;
import java.util.List;
import javax.persistence.TypedQuery;
import data.pojo.Word;
import data.Lang;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Stefan
 */
public class WordDAO extends AbstractDAO<Word> {

    private static WordDAO instance = null;

    private Map<Lang, Map<String, Word>> cache = null;

    public WordDAO() {

    }

    public static WordDAO getInstance() {
        if (instance == null) {
            instance = new WordDAO();
        }
        return instance;
    }
    
    public Word findByLabel(String label) {
        return findByLabel(label, Lang.eng);
    }

    public Word findByLabel(String label, Lang lang) {
        if (cache != null && cache.containsKey(lang)) {
            return cache.get(lang).get(label);
        }
        return findByLabel(label, Language.fromLang(lang));
    }
    
    public Word findByLabel(String label, Language lang) {
        return dao.executeQuery(em -> {
            TypedQuery<Word> query = em.createNamedQuery("Word.findByLabel", Word.class);
            query.setParameter("lang", lang);
            query.setParameter("label", label);
            return query.getSingleResult();
        });
    }
    
    public List<Word> findAllByLabel(String label, Language lang) {
        return dao.executeQuery(em -> {
            TypedQuery<Word> query = em.createNamedQuery("Word.findByLabel", Word.class);
            query.setParameter("lang", lang);
            query.setParameter("label", label);
            return query.getResultList();
        });
    }

    public List<Word> findByLang(Language lang) {
        return dao.executeQuery(em -> {
            TypedQuery<Word> query = em.createNamedQuery("Word.findByLang", Word.class);
            query.setParameter("lang", lang);
            return query.getResultList();
        });
    }
    
    public List<Word> findByLang(Lang lang) {
        return findByLang(Language.fromLang(lang));
    }
    
    public List<Word> findByPrefix(String label, Language lang) {
        return dao.executeQuery(em -> {
            TypedQuery<Word> query = em.createNamedQuery("Word.findByPrefix", Word.class);
            query.setParameter("lang", lang);
            query.setParameter("label", label + "%");
            return query.getResultList();
        });
    }
    
    public List<Word> findByPrefix(String label, Lang lang) {
        return findByPrefix(label, Language.fromLang(lang));
    }
    
    public List<Word> findByPrefix(String label) {
        return findByPrefix(label, Language.fromLang(Lang.eng));
    }

    public void load(Lang lang) {
        if (cache == null) {
            cache = new HashMap<>();
        }
        if (cache.containsKey(lang)) {
            return;
        }
        List<String> words = findByLang(lang).stream()
                .map(Word::getLabel).collect(Collectors.toList());
        Map<String, String> set = new HashMap<>();
        for (String w : words) {
            if (!set.containsKey(w)) {
                set.put(w, w);
            }
            else {
                System.out.println(w + " " + set.get(w));
            }
        }
        Map<String, Word> map = findByLang(lang).stream().collect(
                Collectors.toConcurrentMap(Word::getLabel, Function.identity()));
        cache.put(lang, map);
    }
    
    public void loadAll() {
        if (cache != null) {
            return;
        }
        for (Lang lang : Lang.values()) {
            load(lang);
        }
        
    }

}
