package services.semanticModels.WordNet;

import dao.DAOService;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import data.Word;
import data.cscl.Utterance;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import vu.wntools.wnsimilarity.main.WordSim;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

public class OntologySupport {

    static Logger logger = Logger.getLogger(OntologySupport.class);
    private static int id = 0;

    public static final double SYNONYM_WEIGHT = 1.0;
    public static final double HYPERNYM_WEIGHT = 1.0;
    private static final EnumMap<SimilarityType, Double> THRESHOLDS = new EnumMap<>(SimilarityType.class);

    static {
        THRESHOLDS.put(SimilarityType.LEACOCK_CHODOROW, 1.);
        THRESHOLDS.put(SimilarityType.WU_PALMER, 1.);
        THRESHOLDS.put(SimilarityType.PATH_SIM, 1.);
    }
    private static final int MAX_NO_SYNONYMS = 2;
    private static final int MAX_NO_HYPERNYMS = 1;

    private static final Map<Lang, WordnetPOSData> dictionaries = new EnumMap<>(Lang.class);
    private static final Map<Lang, String> wordnetFiles = new EnumMap<>(Lang.class);

    static {
        wordnetFiles.put(Lang.ro, "resources/config/WN/wn-ron-lmf.xml");
        wordnetFiles.put(Lang.eng, "resources/config/WN/wn-eng-lmf.xml");
        wordnetFiles.put(Lang.fr, "resources/config/WN/wn-fra-lmf.xml");
        wordnetFiles.put(Lang.nl, "resources/config/WN/wn-nld-lmf.xml");
        wordnetFiles.put(Lang.it, "resources/config/WN/wn-ita-lmf.xml");
        wordnetFiles.put(Lang.es, "resources/config/WN/wn-spa-lmf.xml");
        wordnetFiles.put(Lang.la, "resources/config/WN/wn-la-lmf.xml");
        for (Map.Entry<Lang, String> e : wordnetFiles.entrySet()) {
            dictionaries.put(e.getKey(), new WordnetPOSData(e.getValue()));
        }
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
        if (w1 == null || w2 == null || w1.getPOS() == null || w2.getPOS() == null || !w1.getPOS().equals(w2.getPOS())) {
            return 0;
        }
        double sim = dictionaries.get(w1.getLanguage()).semanticSimilarity(w1, w2, type);
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
        return dictionaries.get(lang).getDictionary();
    }

    public static WordnetData getDictionary(Lang lang, POS pos) {
        return dictionaries.get(lang).getByPOS(pos);
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
        Set<String> synonyms1 = dictionaries.get(language).getSynonyms(w1.getLemma(), getPOS(w1.getPOS()));
        Set<String> synonyms2 = dictionaries.get(language).getSynonyms(w2.getLemma(), getPOS(w2.getPOS()));

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

    public static boolean areHypernym(String s1, String s2, Lang language) {
        return getAllHypernyms(s2, language).contains(s1);

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
        return dictionaries.get(word.getLanguage()).getSynonyms(word.getLemma(), getPOS(word.getPOS()));
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
        int no = 0;
        for (String s : synonyms) {
            if (!StopWords.isStopWord(s, word.getLanguage()) && Dictionary.isDictionaryWord(s, word.getLanguage())) {
                results.put(new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
                        word.getLanguage()), SYNONYM_WEIGHT);
                no++;
            }
            if (no >= MAX_NO_SYNONYMS) {
                break;
            }
        }
        no = 0;
        Set<String> hypernyms = getHypernyms(word);
        for (String s : hypernyms) {
            Word newWord = new Word(s, s, Stemmer.stemWord(s.toLowerCase(), word.getLanguage()), word.getPOS(), null,
                    word.getLanguage());
            if (results.containsKey(newWord) && !StopWords.isStopWord(s, word.getLanguage())
                    && Dictionary.isDictionaryWord(s, word.getLanguage())) {
                results.put(newWord, HYPERNYM_WEIGHT);
                no++;
            }
            if (no >= MAX_NO_HYPERNYMS) {
                break;
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
        return new HashSet<>(wnd.entryToSynsets.getOrDefault(word.getLemma(), new ArrayList<>()));
    }

    public static String getFirstSense(Word word) {
        return getDictionary(word).getFirstSynsetString(word.getLemma());
    }

    public static void correctFiles() {
        final Pattern find = Pattern.compile("relType='hype'");
        wordnetFiles.values().parallelStream()
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
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
    }

    public static void main(String[] args) {
        System.out.println(dictionaries.get(Lang.eng).semanticSimilarity("man", "woman", POS.n, SimilarityType.LEACOCK_CHODOROW));
        System.out.println(dictionaries.get(Lang.eng).semanticSimilarity("man", "woman", POS.n, SimilarityType.WU_PALMER));
        System.out.println(dictionaries.get(Lang.eng).semanticSimilarity("man", "woman", POS.n, SimilarityType.PATH_SIM));
        System.out.println(exists("final", "JJ", Lang.fr));
        
        /*Word w1 = Word.getWordFromConcept("horse", Lang.eng);
        Word w2 = Word.getWordFromConcept("dog", Lang.eng);
        w1.setPOS("noun");
        w2.setPOS("noun");
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.LEACOCK_CHODOROW));
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.WU_PALMER));
        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SimilarityType.PATH_SIM));*/
        
        AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(
				"What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance). Human machine interface for ABC computer applications."
						+ " A survey of user opinion of computer system response time."
						+ " The EPS user interface management system. "
						+ "System and human system engineering testing of EPS. "
						+ "Relation of user perceived response time to error measurement.");
        AbstractDocument d = new Document(null, docTmp, null, null, Lang.eng, true, false);
        
        AbstractDocumentTemplate docTmp1 = AbstractDocumentTemplate.getDocumentModel(
				"RAGE, Realising an Applied Gaming Eco-system, aims to develop, transform and enrich advanced technologies from the leisure games industry into self-contained gaming assets that support game studios at developing applied games easier, faster and more cost-effectively. These assets will be available along with a large volume of high-quality knowledge resources through a self-sustainable Ecosystem, which is a social space that connects research, gaming industries, intermediaries, education providers, policy makers and end-users.");
        AbstractDocument d1 = new Document(null, docTmp1, null, null, Lang.eng, false, false);
        
        SemanticCohesion sc = new SemanticCohesion(d, d1);
        System.out.println(sc.getOntologySim().get(SimilarityType.LEACOCK_CHODOROW));
        System.out.println(sc.getOntologySim().get(SimilarityType.WU_PALMER));
        System.out.println(sc.getOntologySim().get(SimilarityType.PATH_SIM));
//correctFiles();
    }
}
