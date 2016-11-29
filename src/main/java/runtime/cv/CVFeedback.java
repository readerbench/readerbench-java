/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cv;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class CVFeedback {
    
    private Boolean fatal;
    private String feedback;

    public CVFeedback(Boolean fatal, String feedback) {
        this.fatal = fatal;
        this.feedback = feedback;
    }

    public Boolean getFatal() {
        return fatal;
    }
    
    public void setFatal(Boolean fatal) {
        this.fatal = fatal;
    }

    public String getFeedback() {
        return feedback;
    }    

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }    
    
}
