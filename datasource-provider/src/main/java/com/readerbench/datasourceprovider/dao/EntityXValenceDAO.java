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
package com.readerbench.datasourceprovider.dao;

import com.readerbench.datasourceprovider.pojo.EntityXValence;
import com.readerbench.datasourceprovider.pojo.SentimentEntity;
import com.readerbench.datasourceprovider.pojo.SentimentValence;
import com.readerbench.datasourceprovider.pojo.Word;

import javax.persistence.TypedQuery;
import java.util.List;

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
    
    public List<EntityXValence> findBySentimentEntity(SentimentEntity se) {
		return dao.executeQuery(em -> {
			TypedQuery<EntityXValence> query = em.createNamedQuery("EntityXValence.findBySentimentEntity", EntityXValence.class);
			query.setParameter("se", se);
			return query.getResultList();
		});
	}
}
