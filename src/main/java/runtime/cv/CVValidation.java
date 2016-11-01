/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cv;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static runtime.cv.CVConstants.*;

/**
 *
 * @author gabigutu
 */
public class CVValidation {
    
    public static List<String> validatePages(int pages) {
        List<String> feedback = new ArrayList<>();
        if (pages < PAGES_MIN) {
            feedback.add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("min_pages"));   
        }
        else if (pages > PAGES_MAX) {
            feedback.add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("max_pages"));   
        }
        return feedback;
    }
    
}
