/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.enea;

/**
 *
 * This is the main class for the ENeA service.
 * 
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class EneaCore {
    
    public static void main(String args[]) {
        
        LessonsReader lessonsReader = new LessonsReader();
        lessonsReader.parse();
        lessonsReader.printLessons();
        
    }
    
}
