/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cscl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class TimeHelper {
    
    /**
     * Computes the difference between two dates
     *
     * @param date1 First datetime
     * @param date2 Second datetime
     * @param timeUnit Time unit
     * @return The difference in time units between dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        // if the difference between the date is negative, add one day
        if (diffInMillies < 0) {
            diffInMillies += 1000 * 60 * 60 * 24;
        }
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
}
