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
public class LessonKeywords {
    
    private String level1;
    private String level2;

    public LessonKeywords() {
        level1 = "";
        level2 = "";
    }

    public String getLevel1() {
        return level1;
    }

    public String getLevel2() {
        return level2;
    }

    public void setLevel1(String level1) {
        this.level1 = level1;
    }

    public void setLevel2(String level2) {
        this.level2 = level2;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[KEYWORDS: ");
        sb.append(level1).append(", ").append(level2);
        sb.append("]");
        return sb.toString();
    }
    
}
