/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cscl;

import data.cscl.Conversation;
import data.cscl.Utterance;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class SimilarityHelper {
    
    /**
     *
     * @param c
     * @param min
     * @param max
     * @return
     */
    public static boolean isInBlock(Conversation c, int min, int max) {
        boolean isInBlock = false;
        if (min != -1) {
            isInBlock = true;
            if (min == max) {
                isInBlock = true;
            }
            if (!((Utterance) c.getBlocks().get(min)).getParticipant().getName()
                    .equals(((Utterance) c.getBlocks().get(max)).getParticipant().getName())) {
                isInBlock = false;
            } else {
                for (int j = min + 1; j <= max - 1; j++) {
                    Utterance secondUtt = (Utterance) c.getBlocks().get(j);
                    if (secondUtt != null) {
                        if (secondUtt.getParticipant() == null || !secondUtt.getParticipant()
                                .getName().equals(((Utterance) c.getBlocks().get(min)).getParticipant().getName())) {
                            isInBlock = false;
                            break;
                        }
                    }
                }
            }
        }
        return isInBlock;
    }

    
}
