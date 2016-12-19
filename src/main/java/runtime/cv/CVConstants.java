/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cv;

/**
 *
 * @author gabigutu
 */
public class CVConstants {
    
    /* Training data */
    public static final String LSA_PATH_FR = "resources/config/FR/LSA/Le_Monde";
    public static final String LDA_PATH_FR = "resources/config/FR/LDA/Le_Monde";
    public static final String LANG_FR = "French";
    public static final String POS_TAGGING = "true";
    public static final String DIALOGISM = "true";
    public static final String THRESHOLD = "0.3";
    
    public static final int NO_CONCEPTS = 10;
    
    public static final int FILESIZE_MAX        = 25 * 1024 * 1024; // 25MB
    public static final int FILESIZE_WARN       = 5 * 1024 * 1024; // 5MB
    public static final int FILESIZE_COMPRESS   = 1 * 1024 * 1024; // 1MB
    
    public static final int PAGES_MIN = 1;
    public static final int PAGES_MAX = 2;
    
    public static final double FAN_DELTA = 1;
    public static final String KEYWORDS = "prospection, prospect, développement, clients, fidélisation, chiffre d’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";
    public static final String IGNORE = "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre";
    // the following keywords should lead to the deletion of the entire row when met
    public static final String IGNORE_LINES = "rue";
    
    public static final String CSV_DELIM = ",";
    public static final String CV_PATH_SAMPLE = "resources/in/cv/cv_sample/";
    public static final String CV_PATH = "resources/in/cv_new/cv_analyse/";
    public static final String STATS_FILE = "global_stats.csv";
    
}
