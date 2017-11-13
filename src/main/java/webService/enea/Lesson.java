/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.enea;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class Lesson {
    
    private final String title;
    private final String description;
    
    private final LessonDescriptives lessonDescriptives;
    private LessonKeywords lessonKeywords;
    private LessonThemes lessonThemes;
    private LessonExpertise lesssonExpertise;
    
    private LessonDescriptives prerequisites;
    private LessonDescriptives postrequisites;
    private Integer time;
    private String uri;
    
    private Double similarityScore;
    
    public Lesson(String title, String description, Integer module, Integer unit, Integer lesson) {
        this.title = title;
        this.description = description;
        lessonDescriptives = new LessonDescriptives(module, unit, lesson);
        similarityScore = 0.0;
    }

    public void setLessonKeywords(LessonKeywords lessonKeywords) {
        this.lessonKeywords = lessonKeywords;
    }
    
    public void setLessonThemes(LessonThemes lessonThemes) {
        this.lessonThemes = lessonThemes;
    }

    public void setLesssonExpertise(LessonExpertise lesssonExpertise) {
        this.lesssonExpertise = lesssonExpertise;
    }

    public void setPrerequisites(LessonDescriptives prerequisites) {
        this.prerequisites = prerequisites;
    }

    public void setPostrequisites(LessonDescriptives postrequisites) {
        this.postrequisites = postrequisites;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
    
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LessonDescriptives getLessonDescriptives() {
        return lessonDescriptives;
    }

    public LessonKeywords getLessonKeywords() {
        return lessonKeywords;
    }

    public LessonThemes getLessonThemes() {
        return lessonThemes;
    }

    public LessonExpertise getLesssonExpertise() {
        return lesssonExpertise;
    }

    public LessonDescriptives getPrerequisites() {
        return prerequisites;
    }

    public LessonDescriptives getPostrequisites() {
        return postrequisites;
    }

    public Integer getTime() {
        return time;
    }    

    public String getUri() {
        return uri;
    }    

    public Double getSimilarityScore() {
        return similarityScore;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lessonDescriptives);
        sb.append(title).append(' ');
        if (!"".equals(uri)) sb.append('(').append(uri).append(')').append(' ');
        if (time != 0) sb.append(time).append(' ');
        sb.append('(').append(description).append(')');
        sb.append(' ');
        sb.append('[').append(lessonKeywords).append(']');
        sb.append(' ');
        sb.append(lessonThemes);
        sb.append(lesssonExpertise);
        if (prerequisites != null) sb.append("PRE: ").append(prerequisites).append("; ");
        if (postrequisites != null) sb.append("POST: ").append(postrequisites).append("; ");
        return sb.toString();
    }
    
}
