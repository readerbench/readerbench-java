package services.sentiment;

import DAO.db.DAOService;
import DAO.db.WordDAO;
import pojo.Word;

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
