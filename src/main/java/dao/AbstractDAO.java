package dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

/**
 *
 * @author Stefan
 */
public abstract class AbstractDAO<T> {

    protected DAOService dao = DAOService.getInstance();
    protected Class<T> type;

    public AbstractDAO() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class) pt.getActualTypeArguments()[0];
    }

    public List<T> findAll() {
        return dao.executeQuery(em -> {
            List<T> result = new ArrayList<>();
            TypedQuery<T> query = em.createNamedQuery(type.getSimpleName() + ".findAll", type);
            try {
                result = query.getResultList();
            } catch (Exception ex) {

            }
            return result;
        });
    }

    public T findById(int id) {
        return dao.executeQuery(em -> {
            return em.find(type, id);
        });
    }

    public boolean save(T object) {
        Boolean result = dao.executeQuery(em -> {
            em.persist(object);
            return true;
        });
        return (result != null);
    }

}
