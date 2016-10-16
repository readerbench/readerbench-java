/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package data.cscl;

import java.util.Date;

import data.Block;
import data.Sentence;
import java.util.EnumMap;

public class Utterance extends Block {

    private static final long serialVersionUID = -8055024257921316162L;

    // chat specific inputs
    private Participant participant;
    private Date time;
    // collaboration assessment in terms of Knowledge Building
    private double KB;
    private double socialKB;
    private double personalKB;

    public Utterance(Block b, Participant p, Date time) {
        super(b.getContainer(), b.getIndex(), b.getText(), b.getSemanticModels(), b.getLanguage());
        // inherit all attributes
        super.setSentences(b.getSentences());
        super.setRefBlock(b.getRefBlock());
        super.setFollowedByVerbalization(b.isFollowedByVerbalization());
        super.setCorefs(b.getCorefs());
        super.setStanfordSentences(b.getStanfordSentences());
        super.setWordOccurences(b.getWordOccurences());
        super.setModelVectors(new EnumMap<>(b.getModelVectors()));
        super.setProcessedText(b.getProcessedText());
        this.participant = p;
        this.time = time;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getKB() {
        return KB;
    }

    public void setKB(double kB) {
        KB = kB;
    }

    public double getSocialKB() {
        return socialKB;
    }

    public void setSocialKB(double socialKB) {
        this.socialKB = socialKB;
    }

    public double getPersonalKB() {
        return personalKB;
    }

    public void setPersonalKB(double personalKB) {
        this.personalKB = personalKB;
    }

    public boolean isEligible(Date startDate, Date endDate) {
        return ((startDate == null) || (time.after(startDate)))
                && ((endDate == null) || (time.before(endDate)));
    }

    @Override
    public String toString() {
        String s = "";
        if (participant != null) {
            s += participant.getName();
        }
        if (time != null) {
            s += "(" + time + ")";
        }
        if (!s.equals("")) {
            s += ":\n";
        }
        s += "{\n";
        s = getSentences().stream().map((sentence) -> "\t" + sentence.toString() + "\n").reduce(s, String::concat);
        s += "}\n[" + getOverallScore() + "]\n";
        return s;
    }

}
