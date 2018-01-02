/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readerbench.dao;

import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
