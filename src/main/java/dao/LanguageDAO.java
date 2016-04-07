package dao;

import data.pojo.Language;

/**
 *
 * @author Stefan
 */
public class LanguageDAO extends AbstractDAO<Language> {

    private static LanguageDAO instance = null;

    private LanguageDAO() {

    }

    public static LanguageDAO getInstance() {
        if (instance == null) {
            instance = new LanguageDAO();
        }
        return instance;
    }

}
