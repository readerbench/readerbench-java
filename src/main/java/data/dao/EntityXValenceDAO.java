package data.dao;

import data.pojo.EntityXValence;
import data.pojo.SentimentEntity;
import data.pojo.SentimentValence;
import data.pojo.Word;

/**
 *
 * @author Stefan
 */
public class EntityXValenceDAO extends AbstractDAO<EntityXValence> {

    private static EntityXValenceDAO instance = null;

    private EntityXValenceDAO() {

    }

    public static EntityXValenceDAO getInstance() {
        if (instance == null) {
            instance = new EntityXValenceDAO();
        }
        return instance;
    }

    public boolean saveWordValence(Word word, SentimentValence valence, double value) {
        Boolean success = dao.executeQuery(em -> {
            SentimentEntity se = word.getFkSentimentEntity();
            if (se == null) {
                se = new SentimentEntity();
                em.persist(se);
                word.setFkSentimentEntity(se);
                em.persist(word);
            }
            EntityXValence exv = new EntityXValence();
            exv.setFkSentimentEntity(se);
            exv.setFkSentimentValence(valence);
            exv.setValue(value);
            em.persist(exv);
            return true;
        });
        return (success != null);
    }
}