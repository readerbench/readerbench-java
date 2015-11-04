package services.sentiment;

import DAO.AbstractDAO;
import pojo.Word;

/**
 *
 * @author Stefan
 */
public class Test {
    public static void main(String[] args) {
        AbstractDAO ad = AbstractDAO.getInstance();
        Word word = ad.findById(Word.class, 2);
        System.out.println(word);
        ad.close();
    }
}
