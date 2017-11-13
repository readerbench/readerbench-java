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
public class LessonDescriptives {
    
    private final Integer module;
    private final Integer unit;
    private final Integer lesson;
    
    private final char DELIMITER = '.';

    public LessonDescriptives(Integer module, Integer unit, Integer lesson) {
        this.module = module;
        this.unit = unit;
        this.lesson = lesson;
    }

    public Integer getModule() {
        return module;
    }

    public Integer getUnit() {
        return unit;
    }

    public Integer getLesson() {
        return lesson;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(module).append(DELIMITER);
        sb.append(unit).append(DELIMITER);
        sb.append(lesson).append(" ");
        return sb.toString();
    }
}
