package com.readerbench.readerbenchcore.data;

import edu.stanford.nlp.util.Triple;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gabriel Cristian on 6/14/2017.
 */
public class CVStructure {

    AbstractDocument document;
    private final int MIN_CHAIN_NR = 2;
    private final int MAX_CHAIN_NR = 5;
    private final float CHAIN_PROPORTION = 2;
    public boolean[] linesLeft;

    private ArrayList<Triple<Float, Float, String>> yCoords = new ArrayList<>();

    public CVStructure () {
        yCoords.add(new Triple<>(0f, 0f, " "));
    }

    public CVStructure (AbstractDocument document) {
        this.document = document;
    }

    public float getLastYCoord() {
        return  yCoords.get(yCoords.size() - 1).first;
    }

    public void addYCoord(Triple<Float, Float, String> y) {
        yCoords.add(y);
    }

    public ArrayList<Triple<Float, Float, String>> getYCoords() {
        return yCoords;
    }

    public float getCoord (Triple<Float, Float, String> info) {
        return info.first;
    }

    public float getFontSize (Triple<Float, Float, String> info) {
        return info.second;
    }

    public String getFontName (Triple<Float, Float, String> info) {
        return info.third;
    }


    public int getByFontSize() {

        int firstAlready = -1;
        int paragraphsNoSize = 0;

        //trying to get 2-4 length periodical occurrences that may indicate a paragraph
        for (int p = MIN_CHAIN_NR; p < MAX_CHAIN_NR; p++) {

            int linesLeftNo = linesLeft.length;
            for (boolean line : linesLeft) {
                if(line == true)
                    linesLeftNo--;
            }
            if((float)(linesLeft.length / linesLeftNo) > CHAIN_PROPORTION) {
                break;
            }

            ArrayList<Pair<Triple<Float, Float, String>,Integer>> chain = new ArrayList<>();
            for (int i = 0; i < yCoords.size() - p; i++ ) {

                if(chain.size() != p) {
                    chain.add(new Pair<>(yCoords.get(i),i));
                }
                else {


                    int stop = 0;
                    int sndIterator = i;
                    int sameLines = 0;
                    while (stop != 1 && sndIterator < yCoords.size()) {

                        for(int j = 0; j < p && sndIterator + j < yCoords.size(); j++) {

                            while(chain.get((p + j -1) % p).getKey().second.equals(yCoords.get(sndIterator + j).second)
                                    && chain.get((p + j -1) % p).getKey().third.equals(yCoords.get(sndIterator + j).third)
                                    && sndIterator + j + 1< yCoords.size()) {
                                sameLines++;
                                sndIterator++;
                            }

                            if(!chain.get(j).getKey().second.equals(yCoords.get(sndIterator+j).second) ||
                                    !chain.get(j).getKey().third.equals(yCoords.get(sndIterator+j).third)) {

                                stop = 1;
                                break;

                            }
                        }
                        if(stop != 1) {
//                            System.out.println("am mai gasit un paragraf cu final in " + sndIterator);
                            if(sndIterator + p > yCoords.size()) {
                                break;
                            }
                            sndIterator += p;
                        }
                    }
                    if(i + sameLines != sndIterator) {
                        paragraphsNoSize += (sndIterator - sameLines - i) / p + 1;

                        if(sndIterator > yCoords.size()) {
                            i = sndIterator - p;
                        }
                        else {
                            i = sndIterator;
                        }


                        while(  sndIterator < yCoords.size() &&
                                chain.get(chain.size() - 1).getKey().second.equals(yCoords.get(sndIterator).second)
                                && chain.get(chain.size() -1 ).getKey().third.equals(yCoords.get(sndIterator).third)
                                ) {

                            sndIterator++;

                        }

                        i = sndIterator - 1;

                        for(int it = chain.get(0).getValue(); it < sndIterator; it++) {
                            linesLeft[it] = true;
                        }
                        chain.clear();
                    }
                    else {
                        for(int j = 0; j < p -1 ; j++) {
                            chain.set(j,chain.get(j+1));
                        }
                        while (chain.get(p-1).getKey().second.equals(yCoords.get(i).second)
                                && chain.get(p-1).getKey().third.equals(yCoords.get(i).third)) {
                            if(i + 1 < yCoords.size()) {
                                i++;
                            }
                            else {
                                break;
                            }
                        }
                        chain.set(p-1,new Pair<>(yCoords.get(i), i));

                    }
                }
            }
        }

        return paragraphsNoSize;
    }

    public int getBySpacing () {

        int paragraphsNo = 0;
        Map<Float,Integer> diffOccurrence = new HashMap<>();
        float error = (float)0.5;
        boolean isAlready = false;

        Float[] diffs = new Float[yCoords.size() - 1];

        for (int i = 0 ; i < yCoords.size() - 1; i++) {
            diffs[i] = yCoords.get(i + 1).first - yCoords.get(i).first;
        }

        for( int i = 0; i < diffs.length; i++) {
            if(diffs[i] > 0) {
                isAlready = false;
                for (Map.Entry<Float, Integer> entry : diffOccurrence.entrySet()) {
                    if (entry.getKey() - error < diffs[i]
                            && entry.getKey() + error > diffs[i]) {

                        entry.setValue(entry.getValue() + 1);
                        isAlready = true;
                        break;
                    }
                }
                if (isAlready == false) {
                    diffOccurrence.put(diffs[i], 1);
                }
            }
        }
        Float maxdiff = (float)(0);
        int maxVal = 0;
        for(Map.Entry<Float, Integer> entry : diffOccurrence.entrySet()) {
            if(entry.getValue() > maxVal) {
                maxVal = entry.getValue();
                maxdiff = entry.getKey();
            }
        }
        maxdiff += 1;

        for (int i = 1 ; i < yCoords.size() - 1; i++) {
            float diff = yCoords.get(i + 1).first - yCoords.get(i).first;
            if (diff > maxdiff) {

                if(i + 2 < yCoords.size()) {
                    if(linesLeft[i + 1] == false && linesLeft[i + 2] == true) {
                        i++;
                        continue;
                    }
                }
                if(i - 1 > 0) {
                    if(linesLeft[i - 1] == true && linesLeft[i] == false && linesLeft[i + 1] == true) {
                        continue;
                    }
                }
                if(linesLeft[i] == true && linesLeft[i + 1] == true) {
                    continue;
                }
//                System.out.println("Se schimba paragraful pe linia " + (i+1));
                paragraphsNo++;
            }

        }

        return paragraphsNo;
    }

    public int getParagraphs () {
        linesLeft = new boolean[yCoords.size()];
        for (boolean bool: linesLeft) {
            bool = false;
        }
        int paragraphs= getByFontSize();
        paragraphs += getBySpacing();
//        System.out.println("NUMARUL TOTAL DE PARAGRAFE ESTE  " + paragraphs);
        return paragraphs;
    }


    public int getSentences (String parsedText) {
        int sentencesNo = 0;
        String[] lines = parsedText.split("[\\r\\n]+");
        for(int i = 0; i < lines.length; i++) {
                sentencesNo++;
                for(int j = 0; j < lines[i].length() - 1; j++) {
                    if(lines[i].charAt(j) == '.' &&
                            lines[i].charAt(j + 1) == ' ') {
                        sentencesNo++;
                    }
                }
        }
        return  sentencesNo;
    }

}
