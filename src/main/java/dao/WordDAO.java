/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import data.pojo.Language;
import java.util.List;
import javax.persistence.TypedQuery;
import data.pojo.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;
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
