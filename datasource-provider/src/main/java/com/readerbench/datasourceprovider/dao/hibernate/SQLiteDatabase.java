package com.readerbench.datasourceprovider.dao.hibernate;

import com.readerbench.datasourceprovider.dao.WordDAO;
import com.readerbench.datasourceprovider.data.sentiment.SentimentWeights;
import org.slf4j.LoggerFactory;

/**
 * Created by dorinela on 10.12.2017.
 */
public class SQLiteDatabase {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SQLiteDatabase.class);

    public static void initializeDB() {
        LOGGER.info("Starting database initialization ...");
        LOGGER.info("Initialize words...");
        WordDAO.getInstance().loadAll();
        LOGGER.info("Words initialization finished");

        SentimentWeights.initialize();
        LOGGER.info("Valence map has {} sentiments after initialization.", com.readerbench.datasourceprovider.data.sentiment.SentimentValence.getValenceMap().size());
    }
}
