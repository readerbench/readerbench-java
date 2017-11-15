/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.enea;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

/**
 *
 * This class is aimed at parsing and interpreting the contents of a CSV file
 * that contains details and descriptions of the lessons of the ENeA MOOC
 * platform.
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class LessonsReader {

    private static final Logger LOGGER = Logger.getLogger("");

    // path to the CSV file that contains lessons' description and details
    public static final String CSV_FILE_PATH = "resources/in/enea/lesson_descriptions/enea_lessons.csv";
    public static final char CSV_DELIMITER = ';';

    // column names in the CSV file
    public static final String COL_KEYWORDS_LEVEL1 = "key_lvl1";
    public static final String COL_KEYWORDS_LEVEL2 = "key_lvl2";
    public static final String COL_MODULE = "mod";
    public static final String COL_UNIT = "unit";
    public static final String COL_LESSON = "lesson";
    public static final String COL_THEMES_SCIENCE = "themes_sci";
    public static final String COL_THEMES_GUIDE = "themes_guide";
    public static final String COL_THEMES_PRACTICE = "themes_practice";
    public static final String COL_EXPERTISE_MEDICINE_PAEDIATRICIAN = "exp_med_paedi";
    public static final String COL_EXPERTISE_MEDICINE_GYNOCOLOGIST = "exp_med_gyn";
    public static final String COL_EXPERTISE_MEDICINE_GP = "exp_med_gp";
    public static final String COL_EXPERTISE_MEDICINE_OTHER = "exp_med_other";
    public static final String COL_EXPERTISE_NURSING = "exp_nurse";
    public static final String COL_EXPERTISE_NUTRITION = "exp_nutrition";
    public static final String COL_PRE_MODULE = "pre_mod";
    public static final String COL_PRE_UNIT = "pre_unit";
    public static final String COL_PRE_LESSSON = "pre_les";
    public static final String COL_POST_MODULE = "post_mod";
    public static final String COL_POST_UNIT = "post_unit";
    public static final String COL_POST_LESSON = "post_les";
    public static final String COL_TIME = "time";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_URL = "url";

    private final Map<LessonDescriptives, Lesson> lessons;

    /**
     * Load the CSV file and stores data into internal objects
     */
    public LessonsReader() {
        lessons = new HashMap<>();
    }

    public void parse() {
        try {

            File initialFile = new File(CSV_FILE_PATH);
            InputStream targetStream = FileUtils.openInputStream(initialFile);
            Reader reader = new InputStreamReader(targetStream);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().withDelimiter(CSV_DELIMITER).parse(reader);
            for (final CSVRecord record : records) {

                final Integer mod = Integer.parseInt(record.get(COL_MODULE));
                final Integer unit = Integer.parseInt(record.get(COL_UNIT));
                final Integer les = Integer.parseInt(record.get(COL_LESSON));

                final String title = record.get(COL_TITLE);
                final String description = record.get(COL_DESCRIPTION);

                Lesson lesson = new Lesson(title, description, mod, unit, les);

                final String key_lvl1 = record.get(COL_KEYWORDS_LEVEL1);
                final String key_lvl2 = record.get(COL_KEYWORDS_LEVEL2);
                LessonKeywords lessonKeywords = new LessonKeywords();
                lessonKeywords.setLevel1(key_lvl1);
                lessonKeywords.setLevel2(key_lvl2);
                lesson.setLessonKeywords(lessonKeywords);

                final Boolean themes_sci = parseBoolean(record.get(COL_THEMES_SCIENCE));
                final Boolean themes_guide = parseBoolean(record.get(COL_THEMES_GUIDE));
                final Boolean themes_practice = parseBoolean(record.get(COL_THEMES_PRACTICE));
                LessonThemes lessonThemes = new LessonThemes();
                lessonThemes.setTheory(themes_sci);
                lessonThemes.setGuidelines(themes_guide);
                lessonThemes.setPractice(themes_practice);
                lesson.setLessonThemes(lessonThemes);

                final Boolean exp_med_paedi = parseBoolean(record.get(COL_EXPERTISE_MEDICINE_PAEDIATRICIAN));
                final Boolean exp_med_gyn = parseBoolean(record.get(COL_EXPERTISE_MEDICINE_GYNOCOLOGIST));
                final Boolean exp_med_gp = parseBoolean(record.get(COL_EXPERTISE_MEDICINE_GP));
                final Boolean exp_med_other = parseBoolean(record.get(COL_EXPERTISE_MEDICINE_OTHER));
                final Boolean exp_nurse = parseBoolean(record.get(COL_EXPERTISE_NURSING));
                final Boolean exp_nutrition = parseBoolean(record.get(COL_EXPERTISE_NUTRITION));
                LessonExpertise lesssonExpertise = new LessonExpertise();
                lesssonExpertise.setMedicinePaeditrician(exp_med_paedi);
                lesssonExpertise.setMedicineGynocologist(exp_med_gyn);
                lesssonExpertise.setMedicineGp(exp_med_gp);
                lesssonExpertise.setMedicineOther(exp_med_other);
                lesssonExpertise.setNursing(exp_nurse);
                lesssonExpertise.setNutrition(exp_nutrition);
                lesson.setLesssonExpertise(lesssonExpertise);

                final Integer preMod;
                if (!"".equals(record.get(COL_PRE_MODULE))) preMod = Integer.parseInt(record.get(COL_PRE_MODULE));
                else preMod = 0;
                
                final Integer preUnit;
                if (!"".equals(record.get(COL_PRE_UNIT))) preUnit = Integer.parseInt(record.get(COL_PRE_UNIT));
                else preUnit = 0;
                
                final Integer preLes;
                if (!"".equals(record.get(COL_PRE_LESSSON))) preLes = Integer.parseInt(record.get(COL_PRE_LESSSON));
                else preLes = 0;
                
                LessonDescriptives preLessonDescipritves = new LessonDescriptives(preMod, preUnit, preLes);
                lesson.setPrerequisites(preLessonDescipritves);

                final Integer postMod;
                if (!"".equals(record.get(COL_POST_MODULE))) postMod = Integer.parseInt(record.get(COL_POST_MODULE));
                else postMod = 0;
                
                final Integer postUnit;
                if (!"".equals(record.get(COL_POST_UNIT))) postUnit = Integer.parseInt(record.get(COL_POST_UNIT));
                else postUnit = 0;
                
                final Integer postLes;
                if (!"".equals(record.get(COL_POST_LESSON))) postLes = Integer.parseInt(record.get(COL_POST_LESSON));
                else postLes = 0;
                
                LessonDescriptives postLessonDescipritves = new LessonDescriptives(postMod, postUnit, postLes);
                lesson.setPostrequisites(postLessonDescipritves);

                final Integer time = Integer.parseInt(record.get(COL_TIME));
                lesson.setTime(time);

                final String url = record.get(COL_URL);
                lesson.setUri(url);

                lessons.put(lesson.getLessonDescriptives(), lesson);

            }           
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
        }
    }

    public Map<LessonDescriptives, Lesson> getLessons() {
        return lessons;
    }
    
    private Boolean parseBoolean(String string) {
        return "x".equals(string);
    }
    
    public void printLessons() {
        LOGGER.info("Printing lessons...");
        LOGGER.info(this.toString());
    }

    @Override
    public String toString() {
        LOGGER.log(Level.INFO, "There are {0} lessons.", lessons.size());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry pair : lessons.entrySet()) {
            LessonDescriptives lessonDescipritves = (LessonDescriptives) pair.getKey();
            Lesson lesson = (Lesson) pair.getValue();
            sb.append(lesson).append("\n");
        }
        return sb.toString();
    }

}
