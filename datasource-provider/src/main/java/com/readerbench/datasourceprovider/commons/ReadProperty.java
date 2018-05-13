/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.datasourceprovider.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ReadProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadProperty.class);

    /**
     * read data from .properties files
     *
     * @param resourceName
     * @return
     */
    public static Properties getProperties(String resourceName) {
        Properties properties = new Properties();
        InputStream input;
        try {
            input = ReadProperty.class.getClassLoader().getResourceAsStream(resourceName);
            if (input == null) {
                throw new RuntimeException("Configuration file missing: " + resourceName);
            }
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return properties;
    }
}
