/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.sentiment;

import DAO.db.DAOService;
import DAO.db.ValenceDAO;
import DAO.db.WordDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
	private ListOfWords low;
	
	
    public void readList(String listFile, String key) {
        
    }
	
	public void loadWords() {
		low = new ListOfWords("resources/config/Dictionary/dict_en.txt");
	}
    
    public void readListsFile(String listFile) {
        try {
            
            Scanner s = new Scanner(new File(BASE_DIR + listFile));
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Scanner lineScanner = new Scanner(line);
                String sentiment = lineScanner.next();
                while (lineScanner.hasNext()) {
                    String word = lineScanner.next();
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
	
	public void saveWordsToDB() {
		WordDAO wd = new WordDAO();
		low.getWords().stream().map((word) -> {
			Word w = new Word();
			w.setLabel(word);
			return w;
		}).forEach((w) -> {
			wd.save(w);
		});
	}
	
	public void createValences() {
		File folder = new File(BASE_DIR + "Processed Lists/");
		for (File f : folder.listFiles()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
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
	
	}
	
	public static void main(String[] args) throws IOException {
        Seanse seanse = new Seanse();
		//seanse.saveWordsToDB();
		seanse.createValences();
		DAOService.getInstance().close();
        //seanse.readAffectiveList();
        //Path link = Files.readSymbolicLink(Paths.get(BASE_DIR + AFFECTIVE_LIST));
        //System.out.println(link.toFile());
        //System.out.println(new File(AFFECTIVE_LIST).toPath().toRealPath());
    }
    
}
