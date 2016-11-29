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
    
    public static List<CVFeedback> validatePages(int pages) {
        List<CVFeedback> feedback = new ArrayList<>();
        if (pages < PAGES_MIN) {
            feedback.add(new CVFeedback(false, ResourceBundle.getBundle("utils.localization.cv_errors").getString("min_pages")));
        }
        else if (pages > PAGES_MAX) {
            feedback.add(new CVFeedback(false, ResourceBundle.getBundle("utils.localization.cv_errors").getString("max_pages")));
        }
        return feedback;
    }
    
    public static List<CVFeedback> validateFileSize(long fileSize) {
        List<CVFeedback> feedback = new ArrayList<>();
        if (fileSize > FILESIZE_MAX) {
            feedback.add(new CVFeedback(true, ResourceBundle.getBundle("utils.localization.cv_errors").getString("filesize_max")));
        }
        else if (fileSize > FILESIZE_WARN) {
            feedback.add(new CVFeedback(false, ResourceBundle.getBundle("utils.localization.cv_errors").getString("filesize_warn")));
        }
        else if (fileSize > FILESIZE_COMPRESS) {
            feedback.add(new CVFeedback(false, ResourceBundle.getBundle("utils.localization.cv_errors").getString("filesize_compress")));
        }
        return feedback;
    }
    
}
