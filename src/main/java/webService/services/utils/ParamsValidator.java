/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.utils;

import java.util.Set;

/**
 * Class covering validation of input data for web services.
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ParamsValidator {

    /**
     * Returns a list of required key parameters that are not available in the
     * second set.
     *
     * @param requiredParams a set of required key parameters
     * @param params a set of provided key parameters
     * @return a set of required key parameters that are missing
     */
    public static Set<String> checkRequiredParams(Set<String> requiredParams, Set<String> params) {
        requiredParams.removeAll(params);
        if (requiredParams.size() >= 1) {
            return requiredParams;
        }
        return null;
    }

    /**
     * Returns a string describing the required key parameters that are missing
     * in a human readable format.
     *
     * @param requiredParamsMissing a set of required key parameters that are
     * missing
     * @return a string describing the missing parameters in a human readable
     * format
     */
    public static String errorParamsMissing(Set<String> requiredParamsMissing) {
        StringBuilder sb = new StringBuilder();
        sb.append("Missing required parameters: ");
        sb.append(String.join(", ", requiredParamsMissing));
        return sb.toString();
    }

}
