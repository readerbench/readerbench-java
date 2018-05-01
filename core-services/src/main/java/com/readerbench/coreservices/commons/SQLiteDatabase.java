package com.readerbench.coreservices.commons;

import com.readerbench.coreservices.sentimentanalysis.data.SentimentValence;
import com.readerbench.coreservices.sentimentanalysis.data.SentimentWeights;
import com.readerbench.datasourceprovider.dao.WordDAO;
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
        LOGGER.info("Valence map has {} sentiments after initialization.", SentimentValence.getValenceMap().size());
    }
}
