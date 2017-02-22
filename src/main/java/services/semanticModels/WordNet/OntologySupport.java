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
package services.semanticModels.WordNet;

import services.semanticModels.SimilarityType;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import data.Word;
import data.discourse.SemanticCohesion;
import data.document.Document;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.POS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import vu.wntools.wordnet.WordnetData;

public class OntologySupport {

    static Logger logger = Logger.getLogger("");

    public static final double SYNONYM_WEIGHT = 1.0;
    public static final double HYPERNYM_WEIGHT = 1.0;
    private static final EnumMap<SimilarityType, Double> THRESHOLDS = new EnumMap<>(SimilarityType.class);

    static {
        THRESHOLDS.put(SimilarityType.LEACOCK_CHODOROW, 1.);
        THRESHOLDS.put(SimilarityType.WU_PALMER, 1.);
        THRESHOLDS.put(SimilarityType.PATH_SIM, 1.);
    }

    private static final Map<Lang, WordnetPOSData> DICTIONARIES = new EnumMap<>(Lang.class);
    private static final Map<Lang, String> WORDNET_FILEs = new EnumMap<>(Lang.class);

    static {
        WORDNET_FILEs.put(Lang.ro, "resources/config/RO/WN/wn-ron-lmf.xml");
        WORDNET_FILEs.put(Lang.en, "resources/config/EN/WN/wn-eng-lmf.xml");
        WORDNET_FILEs.put(Lang.fr, "resources/config/FR/WN/wn-fra-lmf.xml");
        WORDNET_FILEs.put(Lang.nl, "resources/config/NL/WN/wn-nld-lmf.xml");
        WORDNET_FILEs.put(Lang.it, "resources/config/IT/WN/wn-ita-lmf.xml");
        WORDNET_FILEs.put(Lang.es, "resources/config/ES/WN/wn-spa-lmf.xml");
        WORDNET_FILEs.put(Lang.la, "resources/config/LA/WN/wn-la-lmf.xml");
        WORDNET_FILEs.entrySet().stream().forEach((e) -> {
            DICTIONARIES.put(e.getKey(), new WordnetPOSData(e.getValue()));
        });
    }

    public static POS getPOS(String posTag) {
        if (posTag == null) {
            return null;
        }
        if (posTag.startsWith("NN")) {
            return POS.n;
        }
        if (posTag.startsWith("VB")) {
            return POS.v;
        }
        if (posTag.startsWith("RB")) {
            return POS.r;
        }
        if (posTag.startsWith("JJ")) {
            return POS.a;
        }
        return null;
    }

    public static double semanticSimilarity(Word w1, Word w2, SimilarityType type) {
        if (!w1.getLanguage().equals(w2.getLanguage())) {
            return 0;
        }
        if (w1.getPOS() == null || w2.getPOS() == null || !w1.getPOS().equals(w2.getPOS())) {
            return 0;
        }
        double sim = DICTIONARIES.get(w1.getLanguage()).semanticSimilarity(w1, w2, type);
        if (sim > THRESHOLDS.get(type)) {
            sim = THRESHOLDS.get(type);
        }
        return sim;
    }

    public static <T> boolean haveCommonElements(List<T> set1, List<T> set2) {
        return haveCommonElements(set1, new HashSet<T>(set2));
    }

    public static <T> boolean haveCommonElements(Collection<T> set1, Set<T> set2) {
        for (T s1 : set1) {
            if (set2.contains(s1)) {
                return true;
            }
        }
        return false;
    }

    public static WordnetData getDictionary(Lang lang) {
        return DICTIONARIES.get(lang).getDictionary();
    }

    public static WordnetData getDictionary(Lang lang, POS pos) {
        return DICTIONARIES.get(lang).getByPOS(pos);
    }

    public static WordnetData getDictionary(Word word) {
        if (word.getPOS() == null) {
            return getDictionary(word.getLanguage());
        }
        return getDictionary(word.getLanguage(), getPOS(word.getPOS()));
    }

    public static boolean areSynonyms(String s1, String s2, Lang language) {
        Set<String> synonyms1 = new HashSet<>(getDictionary(language).getSynonyms(s1));
        Set<String> synonyms2 = new HashSet<>(getDictionary(language).getSynonyms(s2));

        return haveCommonElements(synonyms1, synonyms2);
        // return synonyms1.contains(s2) || synonyms2.contains(s1);
    }

    public static boolean areSynonyms(Word w1, Word w2, Lang language) {
        if (!w1.getPOS().equals(w2.getPOS())) {
            return false;
        }
        if (getPOS(w1.getPOS()) == null || getPOS(w2.getPOS()) == null) {
            return false;
        }
        Set<String> synonyms1 = DICTIONARIES.get(language).getSynonyms(w1.getLemma(), getPOS(w1.getPOS()));
        Set<String> synonyms2 = DICTIONARIES.get(language).getSynonyms(w2.getLemma(), getPOS(w2.getPOS()));

        return synonyms1.contains(w2.getLemma()) || synonyms2.contains(w1.getLemma());
    }

    public static Set<String> getAllHypernyms(String sense, Lang lang) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(sense);
        visited.add(sense);
        WordnetData wnd = getDictionary(lang);
        while (!queue.isEmpty()) {
            String synset = queue.poll();
            for (String next : wnd.hyperRelations.getOrDefault(synset, new ArrayList<>())) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.offer(next);
                }
            }
        }
        return visited;
    }

    public static boolean areDirectHypernyms(String s1, String s2, Lang language) {
        return getDictionary(language).hyperRelations.getOrDefault(s2, new ArrayList<>()).stream()
                .anyMatch(s -> s.equals(s1));
    }

    public static boolean areHypernym(String s1, String s2, Lang language) {
        return getAllHypernyms(s2, language).contains(s1);

    }

    public static boolean areDirectHyponyms(String s1, String s2, Lang language) {
        return areDirectHypernyms(s2, s1, language);
    }

    public static boolean areHyponym(String s1, String s2, Lang language) {
        return getAllHypernyms(s1, language).contains(s2);

    }

    public static boolean areSiblings(String s1, String s2, Lang language) {
        WordnetData wnd = getDictionary(language);
        ArrayList<String> parents1 = wnd.getHyperRelations().get(s1);
        ArrayList<String> parents2 = wnd.getHyperRelations().get(s2);
        if (parents1 == null || parents2 == null) {
            return false;
        }
        return haveCommonElements(parents1, parents2);
    }

    private static Set<String> getSiblingSet(String sense, Lang language) {
        final WordnetData wnd = getDictionary(language);
        Set<String> siblings = wnd.hyperRelations.getOrDefault(sense, new ArrayList<>()).stream()
                .flatMap(parent -> wnd.childRelations.getOrDefault(parent, new ArrayList<>()).stream())
                .collect(Collectors.toSet());
        siblings.remove(sense);
        return siblings;
    }

    public static boolean exists(String word, String pos, Lang lang) {
        WordnetData wnd = getDictionary(lang, getPOS(pos));
        return wnd.entryToSynsets.containsKey(word);
    }

    public static Set<String> getSynonyms(Word word) {
        if (word.getPOS() == null) {
            return null;
        }
        return DICTIONARIES.get(word.getLanguage()).getSynonyms(word.getLemma(), getPOS(word.getPOS()));
    }

    public static Set<String> getHypernyms(Word word) {
        if (word.getPOS() == null) {
            return null;
        }
        final WordnetData wnd = getDictionary(word.getLanguage(), getPOS(word.getPOS()));
        return wnd.lemmaToSynsets.getOrDefault(word.getLemma(), new ArrayList<>()).stream()
                .flatMap(synset -> wnd.hyperRelations.getOrDefault(synset, new ArrayList<>()).stream())
                .flatMap(synset -> wnd.synsetToEntries.getOrDefault(synset, new ArrayList<>()).stream())
                .collect(Collectors.toSet());
    }

    public static Set<String> getOtherRelatedWords(Word word) {
        if (word.getPOS() == null) {
            return null;
        }
        final WordnetData wnd = getDictionary(word.getLanguage(), getPOS(word.getPOS()));
        return wnd.lemmaToSynsets.getOrDefault(word.getLemma(), new ArrayList<>()).stream()
                .flatMap(synset -> wnd.otherRelations.getOrDefault(synset, new ArrayList<>()).stream())
                .flatMap(synset -> wnd.synsetToEntries.getOrDefault(synset, new ArrayList<>()).stream())
                .collect(Collectors.toSet());
    }

    public static Set<String> getHypernymSenses(String sense, Lang lang) {
        final WordnetData wnd = getDictionary(lang);
        return wnd.hyperRelations.getOrDefault(sense, new ArrayList<>()).stream()
                .collect(Collectors.toSet());
    }

    public static TreeMap<Word, Double> getSimilarConcepts(Word word) {
        if (word.getPOS() == null) {
            return null;
        }
        TreeMap<Word, Double> results = new TreeMap<>();
        Set<String> synonyms = getSynonyms(word);
        for (String s : synonyms) {
            if (!StopWords.isStopWord(s, word.getLanguage()) && Dictionary.isDictionaryWord(s, word.getLanguage())) {
                results.put(new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
                        word.getLanguage()), SYNONYM_WEIGHT);
            }
        }
        Set<String> hypernyms = getHypernyms(word);
        for (String s : hypernyms) {
            Word newWord = new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
                    word.getLanguage());
            if (results.containsKey(newWord) && !StopWords.isStopWord(s, word.getLanguage())
                    && Dictionary.isDictionaryWord(s, word.getLanguage())) {
                results.put(newWord, HYPERNYM_WEIGHT);
            }
        }
        return results;
    }

    public static Set<String> getWordSenses(Word word) {
        WordnetData wnd;
        if (word.getPOS() == null) {
            wnd = getDictionary(word.getLanguage());
        } else {
            wnd = getDictionary(word.getLanguage(), getPOS(word.getPOS()));
        }
        if (wnd == null) {
            System.out.println("hopa");
        }
        return new HashSet<>(wnd.entryToSynsets.getOrDefault(word.getLemma(), new ArrayList<>()));
    }

    public static String getFirstSense(Word word) {
        if (!getDictionary(word).entryToSynsets.containsKey(word.getLemma())) {
            return "";
        }
        ArrayList<String> synsets = getDictionary(word).entryToSynsets.get(word.getLemma());
        if (synsets.isEmpty()) {
            return "";
        }
        return synsets.get(0);
    }

    public static Set<String> getRootSenses(Lang lang) {
        return new HashSet<>(getDictionary(lang).getTopNodes());
    }

    public static void correctFiles() {
        final Pattern find = Pattern.compile("relType='hype'");
        WORDNET_FILEs.values().parallelStream()
                .forEach(fileName -> {
                    BufferedReader in = null;
                    BufferedWriter out = null;
                    try {
                        in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + ".tmp"), "UTF-8"));
                        String line;
                        while ((line = in.readLine()) != null) {
                            line = find.matcher(line).replaceAll("relType='has_hypernym'");
                            out.write(line + "\n");
                        }
                        out.close();
                        in.close();
                        new File(fileName).delete();
                        new File(fileName + ".tmp").renameTo(new File(fileName));
                    }
                    catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        }
                        catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
    }

    public static Set<Lang> getAvailableLanguages() {
        return WORDNET_FILEs.keySet();
    }

    public static TreeMap<Word, Double> getExtendedSymilarConcepts(Word word) {

        TreeMap<Word, Double> results = getSimilarConcepts(word);

        System.out.println("first set: " + results);

        //gets hyponyms
        Set<String> related = getOtherRelatedWords(word);
        for (String s : related) {
            Word newWord = new Word(s, s, Stemmer.stemWord(s.toLowerCase(),
                    word.getLanguage()), word.getPOS(), null,
                    word.getLanguage());
            if (results.containsKey(newWord)
                    && !StopWords.isStopWord(s, word.getLanguage())
                    && Dictionary.isDictionaryWord(s, word.getLanguage())) {
                results.put(newWord, HYPERNYM_WEIGHT);
            }
        }

        System.out.println("Found " + results.size() + " related");
        return results;
    }

    public static void main(String[] args) {
        Word w1 = Word.getWordFromConcept("country_NN", Lang.en);
        Word w2 = Word.getWordFromConcept("frog_NN", Lang.en);
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.WU_PALMER));
        System.out.println(OntologySupport.getWordSenses(w1));
        System.out.println(OntologySupport.getSynonyms(w1));
        System.out.println(OntologySupport.getSynonyms(w2));
        System.out.println("w1 similar -> " +  OntologySupport.getSimilarConcepts(w1));
        System.out.println("w1 extended -> " +  OntologySupport.getExtendedSymilarConcepts(w1));
        
        /*Word w1 = Word.getWordFromConcept("horse", Lang.eng);
        Word w2 = Word.getWordFromConcept("dog", Lang.eng);
        w1.setPOS("noun");
        w2.setPOS("noun");
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.LEACOCK_CHODOROW));
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.WU_PALMER));
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.PATH_SIM));*/
//        AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(
//                "What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance). Human machine interface for ABC computer applications."
//                + " A survey of user opinion of computer system response time."
//                + " The EPS user interface management system. "
//                + "System and human system engineering testing of EPS. "
//                + "Relation of user perceived response time to error measurement.");
//        AbstractDocument d = new Document(null, docTmp, new ArrayList<>(), Lang.en, true);
//
//        AbstractDocumentTemplate docTmp1 = AbstractDocumentTemplate.getDocumentModel(
//                "RAGE, Realising an Applied Gaming Eco-system, aims to develop, transform and enrich advanced technologies from the leisure games industry into self-contained gaming assets that support game studios at developing applied games easier, faster and more cost-effectively. These assets will be available along with a large volume of high-quality knowledge resources through a self-sustainable Ecosystem, which is a social space that connects research, gaming industries, intermediaries, education providers, policy makers and end-users.");
//        AbstractDocument d1 = new Document(null, docTmp1, new ArrayList<>(), Lang.en, false);
//
//        SemanticCohesion sc = new SemanticCohesion(d, d1);
//        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.LEACOCK_CHODOROW));
//        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.WU_PALMER));
//        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.PATH_SIM));
        // correctFiles();
    }
}
