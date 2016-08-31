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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

/**
 *
 * @author Stefan
 */
public class DAOService {

    private static DAOService instance = null;

    private final EntityManagerFactory emf;
    private EntityManager em;

    private static final Object lock = new Object();

    private DAOService() {
        Properties p = null;
        try {
            p = new Properties();
            p.load(new FileInputStream("db.properties"));
        } catch (IOException ex) {
            Logger.getLogger(DAOService.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Database properties not set!");
        }
        emf = Persistence.createEntityManagerFactory("ReaderBench", p);
        em = emf.createEntityManager();
    }

    public static DAOService getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new DAOService();
            }
            return instance;
        }
    }

    public <T> T executeQuery(Function<EntityManager, T> f) {
        synchronized (this) {
            try {
                em.getTransaction().begin();
                T result = f.apply(em);
                em.getTransaction().commit();
                return result;
            } catch (NoResultException ex) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return null;
            }
        }
    }

    public void close() {
        if (em != null) {
            em.close();
        }
        emf.close();
    }

}
