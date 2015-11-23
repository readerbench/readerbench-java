package services.sentiment;

import data.dao.DAOService;
import data.dao.WordDAO;
import data.pojo.Word;

/**
 *
 * @author Stefan
 */
public class Test {
	
	
    public static void main(String[] args) {
        DAOService ad = DAOService.getInstance();
        Word word = new WordDAO().findById(2);
        System.out.println(word);
        ad.close();
    }
}
