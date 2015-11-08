/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.sentiment;

import DAO.db.DAOService;
import DAO.db.EntityXValenceDAO;
import DAO.db.ValenceDAO;
import DAO.db.WordDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.openide.util.Exceptions;
import pojo.SentimentValence;
import pojo.Word;
import services.nlp.listOfWords.ListOfWords;

/**
 *
 * @author Stefan
 */
public class Seanse {

    private static final String BASE_DIR = "resources/RAGE docs/WP2/Sem res/";
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
                    DAO.sentiment.SentimentValence.get(key).getId());
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
                        DAO.sentiment.SentimentValence.get(valenceNames[i]).getId());
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
        low = new ListOfWords("resources/config/Dictionary/dict_en.txt");
    }

    public void saveWordValence(String wordLabel, SentimentValence valence, double value) {
        List<Word> words;
        if (wordLabel.endsWith("*")) {
            words = WordDAO.getInstance().findByPrefix(wordLabel.substring(0, wordLabel.length() - 1));
        } else {
            Word word = WordDAO.getInstance().findByLabel(wordLabel);
            words = new ArrayList<>(1);
            if (word != null) {
                words.add(word);
            }
        }
        for (Word word : words) {
            EntityXValenceDAO.getInstance().saveWordValence(word, valence, 1.);
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
                        DAO.sentiment.SentimentValence.get(sentiment).getId());
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

    public void saveWordsToDB() {
        WordDAO wd = WordDAO.getInstance();
        low.getWords().stream().map((word) -> {
            Word w = new Word();
            w.setLabel(word);
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

    public static void main(String[] args) throws IOException {
        Seanse seanse = new Seanse();
        //seanse.loadWords();
        //seanse.saveWordsToDB();
        //seanse.createValences();
        seanse.readAffectiveList();
        seanse.readInquirerBasic();
        seanse.readPositiveWords();
        seanse.readNegativeWords();
        seanse.readANEW();
        seanse.readSenticnet();
        DAOService.getInstance().close();
        //seanse.readAffectiveList();
        //Path link = Files.readSymbolicLink(Paths.get(BASE_DIR + AFFECTIVE_LIST));
        //System.out.println(link.toFile());
        //System.out.println(new File(AFFECTIVE_LIST).toPath().toRealPath());
    }

}
