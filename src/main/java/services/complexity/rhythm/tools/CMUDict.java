/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class CMUDict {
    private static final Logger LOGGER = Logger.getLogger("");
    private static CMUDict instance = null;
    private String path = "resources/config/EN/word lists/cmudict_en.txt";
    private Map<String, List<String>> dict;
	
    private CMUDict() {
        LOGGER.log(Level.INFO, "Loading file {0} ...", path);
        dict = new TreeMap<>();
        try {
            FileInputStream inputFile = new FileInputStream(path);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals(""))
                	continue;
                    String[] paStrings = line.split("\\s+");
                    if (paStrings.length < 2)
                	continue;
                    String key = paStrings[0];
                    List<String> value = new ArrayList<>();
                    for (int i = 1; i < paStrings.length; ++i) {
    			value.add(paStrings[i]);
                    }
                    dict.put(key, value);
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
	}
    }
	
    public Map<String, List<String>> getDict() {
        return dict;
    }

    public static CMUDict getInstance() {
        if (instance == null) {
            instance = new CMUDict();
        }
        return instance;
    }
	
    public static void main(String[] args) {
        CMUDict en_CMUDictionary = CMUDict.getInstance();
        System.out.println(en_CMUDictionary.getDict().keySet());
    }
}
