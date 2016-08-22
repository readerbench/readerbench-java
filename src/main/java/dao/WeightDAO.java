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

import javax.persistence.TypedQuery;

import data.pojo.Weight;

/**
 *
 * @author Gabriel Gutu
 */
public class WeightDAO extends AbstractDAO<Weight>{
	
	private static WeightDAO instance = null;
	
	public WeightDAO() {
		
	}
	
	public static WeightDAO getInstance() {
		if (instance == null) {
			instance = new WeightDAO();
		}
		return instance;
	}
	
	public Weight findByPair(Integer primaryValenceId, Integer rageValenceId) {
		return dao.executeQuery(em -> {
			TypedQuery<Weight> query = em.createNamedQuery("Weight.findByPair", Weight.class);
			query.setParameter("primaryValence", primaryValenceId);
			query.setParameter("rageValence", rageValenceId);
			return query.getSingleResult();
		});
	}
	
}
