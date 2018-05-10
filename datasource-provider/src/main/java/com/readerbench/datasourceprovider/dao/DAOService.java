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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;
import java.util.function.Function;

/**
 *
 * @author Stefan
 */
public class DAOService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DAOService.class);

    private static DAOService instance = null;

    private final EntityManagerFactory emf;
    private EntityManager em;

    private static final Object lock = new Object();

    private DAOService() {
        Properties p = getProperties("db.properties");
        makeTmpProperties(p);
        emf = Persistence.createEntityManagerFactory("ReaderBench", p);
        em = emf.createEntityManager();
    }

    /**
     * read data from .properties files
     *
     * @param resourceName
     * @return
     */
    public Properties getProperties(String resourceName) {
        Properties properties = new Properties();
        InputStream input;
        try {

            input = this.getClass().getClassLoader().getResourceAsStream(resourceName);
            if (input == null) {
                throw new RuntimeException("Configuration file missing: " + resourceName);
            }
            properties.load(input);

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return properties;
    }

    private void makeTmpProperties(Properties p) {
        try {
            // Create tmp folder
            Path filePath = Files.createTempFile("temp", "");
            if (p.containsKey("javax.persistence.jdbc.url")) {
                String value = p.getProperty("javax.persistence.jdbc.url", null);

                if (value != null) {
                    String[] parts = value.split(":");
                    String pathToDbFileString = parts[parts.length - 1];
                    Path pathToDbFile = Paths.get(pathToDbFileString);
                    Long timestamp = System.currentTimeMillis();
                    CopyOption[] options = new CopyOption[]{
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    };
                    Files.copy(pathToDbFile, filePath, options);

                    p.setProperty("javax.persistence.jdbc.url", "jdbc:sqlite:" + filePath.toString());
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
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
                //em.getTransaction().begin();
                T result = f.apply(em);
                //em.getTransaction().commit();
                return result;
            } catch (NoResultException ex) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return null;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
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
