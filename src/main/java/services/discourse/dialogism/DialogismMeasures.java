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
package services.discourse.dialogism;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import data.cscl.Participant;
import services.commons.VectorAlgebra;
import services.discourse.CSCL.Collaboration;
import data.cscl.Conversation;
import data.discourse.SemanticChain;
import java.util.logging.Logger;

public class DialogismMeasures {

    static Logger logger = Logger.getLogger("");

    public static double[][] getSentenceCorrelationMatrix(List<SemanticChain> voices) {
        double[][] correlations = new double[voices.size()][voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
                        .get(i).getSentenceDistribution(), voices.get(j)
                        .getSentenceDistribution());
            }
        }
        return correlations;
    }

    public static double[][] getBlockCorrelationMatrix(List<SemanticChain> voices) {
        double[][] correlations = new double[voices.size()][voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
                        .get(i).getBlockDistribution(), voices.get(j)
                        .getBlockDistribution());
            }
        }
        return correlations;
    }

    public static double[][] getMovingAverageCorrelationMatrix(List<SemanticChain> voices) {
        double[][] correlations = new double[voices.size()][voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
                        .get(i).getBlockMovingAverage(), voices.get(j)
                        .getBlockMovingAverage());
            }
        }
        return correlations;
    }

    public static double[][] getSentenceMutualInformationMatrix(List<SemanticChain> voices) {
        double[][] correlations = new double[voices.size()][voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                correlations[i][j] = VectorAlgebra.mutualInformation(voices
                        .get(j).getSentenceDistribution(), voices.get(j)
                        .getSentenceDistribution());
            }
        }
        return correlations;
    }

    public static double[][] getBlockMutualInformationMatrix(List<SemanticChain> voices) {
        double[][] correlations = new double[voices.size()][voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                correlations[i][j] = VectorAlgebra.mutualInformation(voices
                        .get(i).getBlockMovingAverage(), voices.get(j)
                        .getBlockMovingAverage());
            }
        }
        return correlations;
    }

    public static double[] getAverageBlockMutualInformationEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getBlockMovingAverage().length];

        int no = 0;
        for (int i = 1; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                double[] mi = VectorAlgebra.discreteMutualInformation(voices
                        .get(i).getBlockMovingAverage(), voices.get(j)
                        .getBlockMovingAverage());
                for (int k = 0; k < evolution.length; k++) {
                    evolution[k] += mi[k];
                }
                no++;
            }
        }

        if (no > 0) {
            for (int k = 0; k < evolution.length; k++) {
                evolution[k] /= no;
            }
            return evolution;
        }
        return null;
    }

    // sentence level
    public static double[] getAverageSentenceMutualInformationEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

        int no = 0;
        for (int i = 1; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                double[] mi = VectorAlgebra.discreteMutualInformation(voices
                        .get(i).getSentenceDistribution(), voices.get(j)
                        .getSentenceDistribution());
                for (int k = 0; k < evolution.length; k++) {
                    evolution[k] += mi[k];
                }
                no++;
            }
        }

        if (no > 0) {
            for (int k = 0; k < evolution.length; k++) {
                evolution[k] /= no;
            }
            return evolution;
        }
        return null;
    }

    public static double[] getCoOccurrenceBlockEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getBlockDistribution().length];

        for (int k = 0; k < evolution.length; k++) {
            for (int i = 0; i < voices.size(); i++) {
                evolution[k] += voices.get(i).getBlockDistribution()[k] > 0 ? 1
                        : 0;
            }
        }

        return evolution;
    }

    // sentence level
    public static double[] getCoOccurrenceSentenceEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

        for (int k = 0; k < evolution.length; k++) {
            for (int i = 0; i < voices.size(); i++) {
                evolution[k] += voices.get(i).getSentenceDistribution()[k] > 0 ? 1
                        : 0;
            }
        }

        return evolution;
    }

    public static double[] getCumulativeBlockMuvingAverageEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getBlockMovingAverage().length];

        for (int k = 0; k < evolution.length; k++) {
            for (int i = 0; i < voices.size(); i++) {
                evolution[k] += voices.get(i).getBlockMovingAverage()[k];
            }
        }

        return evolution;
    }

    // sentence level
    public static double[] getCumulativeSentenceEvolution(List<SemanticChain> voices) {
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

        for (int k = 0; k < evolution.length; k++) {
            for (int i = 0; i < voices.size(); i++) {
                evolution[k] += voices.get(i).getSentenceDistribution()[k];
            }
        }

        return evolution;
    }

    public static double[] getCollaborationEvolution(Conversation c) {
        if (c.getVoices() == null || c.getVoices().isEmpty()) {
            return null;
        }
        double[] evolution = new double[c.getVoices().get(0).getBlockMovingAverage().length];
        Iterator<Participant> it = c.getParticipants().iterator();
        List<Participant> lsPart = new ArrayList<>();
        while (it.hasNext()) {
            Participant part = it.next();
            lsPart.add(part);
        }

        c.setNoConvergentPoints(0);
        c.setNoDivergentPoints(0);
        double[][] recurrencePlot = new double[c.getBlocks().size()][c.getBlocks().size()];
        // take all voices
        for (int i = 0; i < c.getVoices().size(); i++) {
            // for different participants build collaboration based on inter-twined voices
            for (int p1 = 0; p1 < lsPart.size() - 1; p1++) {
                for (int p2 = p1 + 1; p2 < lsPart.size(); p2++) {
                    double[] distribution1 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), lsPart.get(p1));
                    double[] distribution2 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), lsPart.get(p2));

                    double[][] mi = VectorAlgebra.recurrencePlot(distribution1, distribution2);
                    for (int j = 0; j < evolution.length; j++) {
                        evolution[j] += mi[j][j];
                    }

                    //build a cumulated recurrence plot
                    for (int row = 0; row < c.getBlocks().size(); row++) {
                        for (int col = 0; col < c.getBlocks().size(); col++) {
                            if (recurrencePlot[row][col] == 0)
                                recurrencePlot[row][col] += mi[row][col];
                        }
                    }
                }
            }
        }

        //determine the rqa indexes
        int noRecurrencePoints = 0;
        int noRecurrencePointsOnDiagonal = 0;
        int[] lengthDiagonalLines = new int[c.getBlocks().size()];
        int index = 0;
        int lengthDiagonal = 0;
        for (int row = 0; row < c.getBlocks().size(); row++) {
            for (int col = 0; col < c.getBlocks().size(); col++) {
                if (recurrencePlot[row][col] != 0) {
                    noRecurrencePoints++;
                    if (col == row) {
                        noRecurrencePointsOnDiagonal++;
                        //start or part of a diagonal line
                        lengthDiagonal++;
                    }
                } else {
                    //reset the length  of diagonal line because met a nul point
                    if (col == row) {
                        if (lengthDiagonal >= 2) {
                            lengthDiagonalLines[index++] = lengthDiagonal;
                        }
                        lengthDiagonal = 0;
                    }
                }
            }
        }

        int maxLine = -1;
        int sumLines = 0;
        double averageLine = 0;
        int noDiagonalLines = 0;
        for (int i = 0; i < lengthDiagonalLines.length; i++) {
            //find the max line
            if (lengthDiagonalLines[i] > maxLine) {
                maxLine = lengthDiagonalLines[i];
            }
            if (lengthDiagonalLines[i] > 0) {
                sumLines += lengthDiagonalLines[i];
                noDiagonalLines++;
            }
        }
        if (noDiagonalLines != 0 )
            averageLine = sumLines * 1.0 / noDiagonalLines;
        else averageLine = 0;



        for (int j = 0; j < evolution.length; j++) {
            if (evolution[j] > 0) {
                c.setNoConvergentPoints(c.getNoConvergentPoints() + 1);
            }
            else if (evolution[j] < 0) {
                c.setNoDivergentPoints(c.getNoDivergentPoints() + 1);
            }
        }

        c.setRecurrenceRate(noRecurrencePoints * 1.0/ (c.getBlocks().size() * c.getBlocks().size()));
        if (noRecurrencePoints != 0)
            c.setDeterminism(noRecurrencePointsOnDiagonal * 1.0 / noRecurrencePoints);
        else c.setDeterminism(0);
        c.setConvergenceRate(c.getNoConvergentPoints() * 1.0 / c.getBlocks().size());
        c.setDivergenceRate(c.getNoDivergentPoints() * 1.0 / c.getBlocks().size());
        c.setConvergenceOrDivergenceRate((c.getNoConvergentPoints() + c.getNoDivergentPoints()) * 1.0 / c.getBlocks().size());
        c.setMaxLine(maxLine);
        c.setAverageLine(averageLine);

        System.out.println("------Recurrence rate: " + c.getRecurrenceRate());
        System.out.println("------Determinism: " + c.getDeterminism());
        System.out.println("------Convergent or divergent points/ Total number of utterances: " + c.getConvergenceOrDivergenceRate());
        System.out.println("------Convergent points/Total number of utterances: " + c.getConvergenceRate());
        System.out.println("------Divergent points/Total number of utterances: " + c.getDivergenceRate());
        System.out.println("------Max line: "+ maxLine);
        System.out.println("------Average line: " + averageLine);
        System.out.println("----Number of convergent points: " + c.getNoConvergentPoints());
        System.out.println("----Number of divergent points: " + c.getNoDivergentPoints());


        c.setIntenseCollabZonesVoice(Collaboration.getCollaborationZones(evolution));
        c.setIntenseConvergentZonesVoice(Collaboration.getConvergentZones(evolution));
        c.setIntenseDivergentZonesVoice(Collaboration.getDivergentZones(evolution));

        return evolution;
    }
}
