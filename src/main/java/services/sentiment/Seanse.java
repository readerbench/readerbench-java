/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.sentiment;

import com.csvreader.CsvReader;
import dao.DAOService;
import dao.EntityXValenceDAO;
import dao.ValenceDAO;
import dao.WordDAO;
import data.pojo.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.openide.util.Exceptions;
import data.pojo.SentimentValence;
import data.pojo.Word;
import data.Lang;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import services.nlp.listOfWords.ListOfWords;

/**
 *
 * @author Stefan
 */
public class Seanse {

    private static final String BASE_DIR = "resources/config/WordLists/EN/";
    private static final String AFFECTIVE_LIST = "affective_list.txt";
    private static final String INQUIRER_BASIC = "inquirerbasic.txt";
    private static final String NEGATIVE_WORDS = "negative_words.txt";
    private static final String POSITIVE_WORDS = "positive_words.txt";
    private static final String AFFECTIVE_NORMS = "affective_norms.txt";
    private static final String SENTICNET = "senticnet_data.txt";

    private ListOfWords low;

    public void readList(String listFile, String key) {
        try {
            SentimentValence valence = new SentimentValence(
                    data.sentiment.SentimentValence.get(key).getId());
            Scanner s = new Scanner(new File(BASE_DIR + listFile));
            while (s.hasNext()) {
                String word = s.next();
                saveWordValence(word, valence, 1.);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void readTabel(String fileName, String[] valenceNames) {
        try {
            SentimentValence[] valences = new SentimentValence[valenceNames.length];
            for (int i = 0; i < valences.length; i++) {
                SentimentValence valence = new SentimentValence(
                        data.sentiment.SentimentValence.get(valenceNames[i]).getId());
                valences[i] = valence;
            }
            Scanner s = new Scanner(new File(BASE_DIR + fileName));
            s.nextLine();
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] split = line.split("\t");
                String word = split[0].trim();
                for (int i = 0; i < valences.length; i++) {
                    saveWordValence(word, valences[i], Double.parseDouble(split[i + 1]));
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void loadWords() {
        low = new ListOfWords("resources/config/Dictionary/dict_fr.txt");
    }

    public void readANEW(String fileName, Lang lang) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        //Read header
        in.readLine();
        in.readLine();
        in.readLine();
        String line;
        while ((line = in.readLine()) != null) {
            String[] cols = line.split(",");
            if (cols.length == 0) continue;
            addANEW(cols[0], lang,
                    ValenceDAO.getInstance().findById(1),
                    Float.parseFloat(cols[4]),
                    1.f);
            addANEW(cols[0], lang,
                    ValenceDAO.getInstance().findById(2),
                    Float.parseFloat(cols[6]),
                    1.f);
//            addANEW(cols[0], lang,
//                    ValenceDAO.getInstance().findById(3),
//                    Float.parseFloat(cols[8]),
//                    Float.parseFloat(cols[9]));
        }
    }

    public void addANEW(String word, Lang lang, SentimentValence valence, float mean, float sd) {
        if (sd > 1.75) {
            System.out.println(word + " - " + valence + " (sd=" + sd + ")");
            return;
        }
        saveWordValence(word, lang, valence, mean);

    }

    public void saveWordValence(String wordLabel, SentimentValence valence, double value) {
        saveWordValence(wordLabel, Lang.eng, valence, value);
    }

    public void saveWordValence(String wordLabel, Lang lang, SentimentValence valence, double value) {
        List<Word> words;
        if (wordLabel.endsWith("*")) {
            words = WordDAO.getInstance().findByPrefix(wordLabel.substring(0, wordLabel.length() - 1), lang);
        } else {
            words = WordDAO.getInstance().findAllByLabel(wordLabel, Language.fromLang(lang));
            if (words.isEmpty()) {
                Word word = new Word();
                word.setFkLanguage(Language.fromLang(lang));
                word.setLabel(wordLabel);
                WordDAO.getInstance().save(word);
                words.add(word);
            }
        }
        for (Word word : words) {
            EntityXValenceDAO.getInstance().saveWordValence(word, valence, value);
        }
    }

    public void readListsFile(String listFile) {
        try {

            Scanner s = new Scanner(new File(BASE_DIR + listFile));
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Scanner lineScanner = new Scanner(line);
                String sentiment = lineScanner.next();
                SentimentValence valence = new SentimentValence(
                        data.sentiment.SentimentValence.get(sentiment).getId());
                while (lineScanner.hasNext()) {
                    String w = lineScanner.next();
                    saveWordValence(w, valence, 1.);
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void readAffectiveList() {
        readListsFile(AFFECTIVE_LIST);
    }

    public void readInquirerBasic() {
        readListsFile(INQUIRER_BASIC);
    }

    public void readPositiveWords() {
        readList(POSITIVE_WORDS, "Positive_HULIU");
    }

    public void readNegativeWords() {
        readList(NEGATIVE_WORDS, "Negative_HULIU");
    }

    public void readANEW() {
        readTabel(AFFECTIVE_NORMS, new String[]{"Valence_ANEW", "Arousal_ANEW", "Dominance_ANEW"});
    }

    public void readSenticnet() {
        readTabel(SENTICNET, new String[]{"pleasantness_SENTICNET", "attention_SENTICNET", "sensitivity_SENTICNET", "aptitude_SENTICNET", "polarity_SENTICNET"});
    }

    public void saveWordsToDB(Lang lang) {
        WordDAO wd = WordDAO.getInstance();
        low.getWords().stream()
                .map((word) -> {
                    Word w = new Word();
                    w.setLabel(word);
                    w.setFkLanguage(Language.fromLang(lang));
                    return w;
                }).forEach((w) -> {
            wd.save(w);
        });
    }

    public void createValences() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(BASE_DIR + "Processed Lists/all.txt"));
            String line;
            while ((line = in.readLine()) != null) {
                SentimentValence valence = new SentimentValence();
                valence.setLabel(line);
                valence.setIndexLabel(line);
                valence.setRage(false);
                ValenceDAO.getInstance().save(valence);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Map<Integer, String> getLIWCValences() {
        BufferedReader in = null;
        Map<Integer, String> result = new HashMap<>();
        try {
            in = new BufferedReader(new FileReader(BASE_DIR + "LIWC2007dictionary poster.csv"));
            String[] ids = in.readLine().split(",");
            String[] valences = in.readLine().split(",");
            for (int i = 0; i < ids.length; i++) {
                result.put(Integer.parseInt(ids[i]), valences[i] + "_LIWC");
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    public void addLIWC(String fileName, Lang lang) throws FileNotFoundException {
        Map<Integer, String> liwc = getLIWCValences();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            int[] ids = Arrays.stream(in.readLine().split(","))
                    .mapToInt(Integer::parseInt).toArray();
            in.readLine();
            String line;
            while ((line = in.readLine()) != null) {
                String[] words = line.split(",");
                for (int i = 0; i < words.length; i++) {
                    saveWordValence(words[i], lang,
                            new SentimentValence(data.sentiment.SentimentValence.get(liwc.get(ids[i])).getId()),
                            1.);
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        System.out.println("Done adding LIWC");
    }

    public void addLIWCFromDic(String fileName, Lang lang) throws FileNotFoundException {
        Map<Integer, String> liwc = getLIWCValences();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            in.readLine();
            String line;
            while (!(line = in.readLine()).startsWith("%")) {

            }
            while ((line = in.readLine()) != null) {
                String[] split = line.split("\t");
                String word = split[0];
                for (int i = 1; i < split.length; i++) {
                    int id = Integer.parseInt(split[i]);
                    saveWordValence(word, lang,
                            new SentimentValence(data.sentiment.SentimentValence.get(liwc.get(id)).getId()),
                            1.);
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        System.out.println("Done adding LIWC");
    }

    public static void main(String[] args) throws IOException {
        Seanse seanse = new Seanse();
        //seanse.loadWords();
        //seanse.saveWordsToDB(Lang.fr);
        //seanse.createValences();
        //seanse.readAffectiveList();
        //seanse.readInquirerBasic();
        //seanse.readPositiveWords();
        //seanse.readNegativeWords();
        //seanse.readANEW();
        //seanse.readSenticnet();
        seanse.readANEW("resources/config/WordLists/FR/13428_2013_431_MOESM2_ESM.csv", Lang.fr);
        //seanse.readANEW();
//        seanse.getLIWCValences().values().stream().forEach(s -> {
//            SentimentValence valence = new SentimentValence();
//            valence.setLabel(s);
//            valence.setIndexLabel(s);
//            valence.setRage(false);
//            ValenceDAO.getInstance().save(valence);
//        });
        //seanse.addLIWCFromDic("resources/config/WordLists/FR/FrenchLIWCDictionary.dic", Lang.fr);
        System.out.println("Closing..");
        DAOService.getInstance().close();
        System.out.println("Finished");
        //seanse.readAffectiveList();
        //Path link = Files.readSymbolicLink(Paths.get(BASE_DIR + AFFECTIVE_LIST));
        //System.out.println(link.toFile());
        //System.out.println(new File(AFFECTIVE_LIST).toPath().toRealPath());
    }

}
