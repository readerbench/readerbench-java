package data.dao;

import data.pojo.CategoryPhrase;

/**
 *
 * @author Stefan
 */
public class CategoryPhraseDAO extends AbstractDAO<CategoryPhrase>{
    private static CategoryPhraseDAO instance = null;

    private CategoryPhraseDAO() {

    }

    public static CategoryPhraseDAO getInstance() {
        if (instance == null) {
            instance = new CategoryPhraseDAO();
        }
        return instance;
    }
    
}
