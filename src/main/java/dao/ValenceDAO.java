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
package dao;

import java.util.List;
import javax.persistence.TypedQuery;
import data.pojo.SentimentValence;

/**
 *
 * @author Gabriel Gutu
 */
public class ValenceDAO extends AbstractDAO<SentimentValence>{
	
	private static ValenceDAO instance = null;
	
	private ValenceDAO() {
		
	}
	
	public static ValenceDAO getInstance() {
		if (instance == null) {
			instance = new ValenceDAO();
		}
		return instance;
	}
	
	public List<SentimentValence> findByRage(boolean rage) {
		return dao.executeQuery(em -> {
			TypedQuery<SentimentValence> query = em.createNamedQuery("SentimentValence.findByRage", SentimentValence.class);
			query.setParameter("rage", rage);
			return query.getResultList();
		});
	}
	
}
