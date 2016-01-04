package dao;

import data.pojo.Category;

/**
 *
 * @author Stefan
 */
public class CategoryDAO extends AbstractDAO<Category>{
    private static CategoryDAO instance = null;

    private CategoryDAO() {

    }

    public static CategoryDAO getInstance() {
        if (instance == null) {
            instance = new CategoryDAO();
        }
        return instance;
    }

}
