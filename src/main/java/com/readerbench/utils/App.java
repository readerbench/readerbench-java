package com.readerbench.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dorinela on 28.11.2017.
 */
public class App {

    /**
     * read data from .properties files
     * @param resourceName
     * @return
     */
    public static Properties getProperties (String resourceName) {

        Properties properties = new Properties();
        InputStream input;
        try {

            input = new App().getClass().getClassLoader().getResourceAsStream(resourceName);
            if (input == null) {
                throw new RuntimeException("Configuration file missing: " + resourceName);
            }
            properties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return properties;
    }


}