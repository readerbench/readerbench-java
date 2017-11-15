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
public class LessonThemes {

    private boolean theory;
    private boolean practice;
    private boolean guidelines;

    public LessonThemes() {
        theory = false;
        practice = false;
        guidelines = false;
    }

    public boolean isTheory() {
        return theory;
    }

    public boolean isPractice() {
        return practice;
    }

    public boolean isGuidelines() {
        return guidelines;
    }

    public void setTheory(boolean theory) {
        this.theory = theory;
    }

    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    public void setGuidelines(boolean guidelines) {
        this.guidelines = guidelines;
    }
    
    public static Integer themeToConstant(String theme) {
        switch(theme) {
            case "science":
                return Constants.THEME_SCIENCE;
            case "guidelines":
                return Constants.THEME_GUIDELINES;
            case "practice":
                return Constants.THEME_PRACTICE;
            default: return 0;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[THEMES: ");
        if (theory) sb.append("Theory, ");
        if (practice) sb.append("Practice, ");
        if (guidelines) sb.append("Guidelines, ");
        sb.append("] ");
        return sb.toString();
    }
    
}
