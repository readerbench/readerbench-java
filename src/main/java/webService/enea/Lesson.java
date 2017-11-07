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
    
    private LessonDescriptives pre;
    private LessonDescriptives post;
    private Integer time;
    private String url;
    
    public Lesson(String title, String description, Integer module, Integer unit, Integer lesson) {
        this.title = title;
        this.description = description;
        lessonDescriptives = new LessonDescriptives(module, unit, lesson);
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

    public void setPre(LessonDescriptives pre) {
        this.pre = pre;
    }

    public void setPost(LessonDescriptives post) {
        this.post = post;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public LessonDescriptives getPre() {
        return pre;
    }

    public LessonDescriptives getPost() {
        return post;
    }

    public Integer getTime() {
        return time;
    }    

    public String getUrl() {
        return url;
    }    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lessonDescriptives);
        sb.append(title).append(' ');
        if (!"".equals(url)) sb.append('(').append(url).append(')').append(' ');
        if (time != 0) sb.append(time).append(' ');
        sb.append('(').append(description).append(')');
        sb.append(' ');
        sb.append('[').append(lessonKeywords).append(']');
        sb.append(' ');
        sb.append(lessonThemes);
        sb.append(lesssonExpertise);
        if (pre != null) sb.append("PRE: ").append(pre).append("; ");
        if (post != null) sb.append("POST: ").append(post).append("; ");
        return sb.toString();
    }
    
}
