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
    public static final String LSA_CORPORA = "Le_Monde";
    public static final String LDA_CORPORA = "Le_Monde";
    public static final String WOR2VEC_CORPORA = "";
    public static final String LANG_FR = "French";
    public static final Boolean POS_TAGGING = true;
    public static final Boolean DIALOGISM = true;
    public static final Boolean USE_BIGRAMS = false;
    public static final Double THRESHOLD = 0.3;
    
    public static final int FILESIZE_MAX        = 25 * 1024 * 1024; // 25MB
    public static final int FILESIZE_WARN       = 5 * 1024 * 1024; // 5MB
    public static final int FILESIZE_COMPRESS   = 1 * 1024 * 1024; // 1MB
    
    public static final int PAGES_MIN = 1;
    public static final int PAGES_MAX = 2;
    
    public static final Double FAN_DELTA = 1.0;
    public static final Double FAN_DELTA_VERY = 2.0;
    public static final String KEYWORDS = "prospection, prospect, développement, clients, fidélisation, chiffre d’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";
    public static final String IGNORE = "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre";
    // the following keywords should lead to the deletion of the entire row when met
    public static final String IGNORE_LINES = "rue";
        
    public static final String CSV_DELIM = ",";
    public static final String CSV_NEW_LINE_DELIM = "\n";
    public static final String CV_PATH_SAMPLE = "resources/in/cv_pos_neg/sample/";
    public static final String CV_PATH = "resources/in/cv_pos_neg/";
    public static final String STATS_FILE = "global_stats.csv";
    
    // discriminants constats
    // global
    public static final Double DISC_GLOBAL_MIN_FONT_SIZE_NEG = .006;
    public static final Double DISC_GLOBAL_MIN_FONT_SIZE_POS = .004;
    public static final Double DISC_GLOBAL_CONSTANT_NEG = -2.886;
    public static final Double DISC_GLOBAL_CONSTANT_POS = -1.959;
    // visual
    public static final Double DISC_VISUAL_WU_PALMER_ABVMEANSTDEV_NEG = .009;
    public static final Double DISC_VISUAL_WU_PALMER_ABVMEANSTDEV_POS = .012;
    public static final Double DISC_VISUAL_MIN_FONT_SIZE_NEG = .005;
    public static final Double DISC_VISUAL_MIN_FONT_SIZE_POS = .003;
    public static final Double DISC_VISUAL_PERCEPT_LIWC_NEG = .000;
    public static final Double DISC_VISUAL_PERCEPT_LIWC_POS = .006;
    public static final Double DISC_VISUAL_CONSTANT_NEG = -4.705;
    public static final Double DISC_VISUAL_CONSTANT_POS = -5.482;
    // content
    public static final Double DISC_CONTENT_AVG_DEPSSEN_NEG = .028;
    public static final Double DISC_CONTENT_AVG_DEPSSEN_POS = .046;
    public static final Double DISC_CONTENT_CONSTANT_NEG = -1.212;
    public static final Double DISC_CONTENT_CONSTANT_POS = -2.114;
}
