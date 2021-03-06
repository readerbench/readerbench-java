/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.data.cscl;

/**
 *
 * @author gabigutu
 */
public class CSCLConstants {
    
    // semantic
    public static Boolean USE_POSTAGGING = true;
    public static Boolean DIALOGISM = false;
    
    // analysis
    public static Integer DISTANCE_ANALYSIS = 0;
    public static Integer TIME_ANALYSIS = 1;
    
    // general
    public static String CSCL_CORPUS = "resources/in/corpus_v2_w2v";
    public static String LSA_PATH = "resources/config/EN/LSA/TASA_CSCL";
    public static String LDA_PATH = "resources/config/EN/LDA/TASA_CSCL";
    public static String WORD2VEC_PATH = "resources/config/EN/word2vec/TASA_CSCL";
    public static String CSV_DELIM = ",";
    
}
