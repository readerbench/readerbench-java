/*
 * Copyright 2018 ReaderBench.
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
package com.readerbench.datasourceprovider.data.cscl;

import com.readerbench.coreservices.commons.VectorAlgebra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Exceptions;

public class ClusterCommunity {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterCommunity.class);

    public static final int MAXIMUM_NUMBER_OF_ITERATIONS = 1000;

    public static void performKMeansClusteringForCSCL(List<Participant> initialParticipants,
            int K, String pathToFile) {

        List<ParticipantNormalized> participants = CommunityUtils.normalizeParticipantsData(initialParticipants);

        List<ParticipantNormalized> clustroids = new LinkedList<>();

        // sets initialization to random or "smart" node selection for maximum
        // dispersion
        // choose a random initial node
        int randomId = (int) (Math.random() * participants.size());
        ParticipantNormalized randomParticipant = participants.get(randomId);

        LOGGER.info("Initializing k clustroids with best possible dispersion...");
        // compute kNN++
        for (int i = 0; i < K; i++) {
            double minDist = Double.MAX_VALUE;
            ParticipantNormalized chosenParticipant = null; //
            // select word with highest distance
            for (ParticipantNormalized p : participants) {
                if (!clustroids.contains(p)) {
                    double distance = 0;
                    distance += compareParticipants(p, randomParticipant);
                    for (int j = 0; j < i; j++) {
                        distance += compareParticipants(p, clustroids.get(j));
                    }
                    if (distance < minDist) {
                        minDist = distance;
                        chosenParticipant = p;
                    }
                }
            }
            clustroids.add(chosenParticipant);
        }

        // cohesion and separation evolution
        double compactness = 0, isolation = 0;
        List<List<ParticipantNormalized>> clusters = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            clusters.add(new LinkedList<>());
        }

        int noIterations = 0;
        // begin assigning process
        while (noIterations < MAXIMUM_NUMBER_OF_ITERATIONS) {
            LOGGER.info("Starting iteration no " + noIterations);

            boolean changesMade = false;

            // clean clusters
            for (int i = 0; i < K; i++) {
                clusters.set(i, new LinkedList<>());
                clusters.get(i).add(clustroids.get(i));
            }

            double sumDist = 0;
            // assign nodes to clusters
            for (ParticipantNormalized p : participants) {
                if (!clustroids.contains(p)) {
                    double maxDist = Double.MIN_VALUE;
                    int clusterId = -1;
                    for (int i = 0; i < clustroids.size(); i++) {
                        double dist = compareParticipants(p, clustroids.get(i));
                        if (dist > maxDist) {
                            if (dist > maxDist) {
                                maxDist = dist;
                                clusterId = i;
                            }
                        }
                    }

                    sumDist += maxDist;

                    // add current participant to corresponding cluster;
                    if (clusterId != -1) {
                        clusters.get(clusterId).add(p);
                    }
                }
            }

            // assign intra-cluster cohesion (compactness)
            compactness = ((double) participants.size()) / sumDist;

            // determine inter-cluster separation (isolation)
            sumDist = 0;
            for (int i = 0; i < clustroids.size() - 1; i++) {
                for (int j = i + 1; j < clustroids.size(); j++) {
                    sumDist += compareParticipants(clustroids.get(i), clustroids.get(j));
                }
            }
            isolation = sumDist / ((double) participants.size());

            // recompute clusteroids
            for (int i = 0; i < K; i++) {
                ParticipantNormalized localCluster = null;
                // determine new centroid
                double maxSim = Double.MIN_VALUE;
                for (ParticipantNormalized p1 : clusters.get(i)) {
                    double sim = 0;
                    for (ParticipantNormalized p2 : clusters.get(i)) {
                        sim += compareParticipants(p1, p2);
                    }
                    if (sim > maxSim) {
                        localCluster = p1;
                        maxSim = sim;
                    }
                }
                if (localCluster != null
                        && !localCluster.equals(clustroids.get(i))) {
                    changesMade = true;
                    clustroids.set(i, localCluster);
                }
            }

            // if there are no further changes we have reached convergence
            if (!changesMade) {
                break;
            }

            noIterations++;
        }

        File output = new File(pathToFile);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                    32768);

            out.write(initialParticipants.toString() + "\n\n");

            for (int i = 0; i < clustroids.size(); i++) {
                out.write(">>" + (i + 1) + ": ");
                for (ParticipantNormalized p : clusters.get(i)) {
                    if (clustroids.contains(p)) {
                        out.write("(" + p.toString() + "); \n");
                    } else {
                        out.write("(" + p.toString() + "); \n");
                    }
                }
                out.write("\n");
            }
            out.close();
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void performAglomerativeClusteringForCSCL(List<Participant> initialParticipants, String pathToFile) {

        List<ParticipantNormalized> participants = CommunityUtils.normalizeParticipantsData(initialParticipants);

        List<List<Integer>> groups = new ArrayList<>();

        // initialize groups
        for (int i = 0; i < participants.size(); i++) {
            List<Integer> group = new ArrayList<>();
            group.add(i);
            groups.add(group);
        }

        int noInterations = 0;

        while (noInterations < participants.size() - 1) {
            // determine max similarity
            double minSum = Double.MAX_VALUE;
            int group1 = -1, group2 = -1;
            for (int i = 0; i < groups.size() - 1; i++) {
                for (int j = i + 1; j < groups.size(); j++) {
                    double dist = compareGroupsOfParticipants(participants, groups.get(i),
                            groups.get(j));
                    if (dist < minSum) {
                        minSum = dist;
                        group1 = i;
                        group2 = j;
                    }
                }
            }

            // merge groups 1 & 2 that have the minimum distance
            try {
                groups.get(group1).addAll(groups.get(group2));
                groups.remove(group2);
            } catch (Exception e) {
                LOGGER.error("Clustering error: " + e.getMessage());
            }

            noInterations++;

            // display groups only if 3
            if (groups.size() == 3) {
                File output = new File(pathToFile);
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768);

                    double max = Double.MIN_VALUE;
                    double min = Double.MAX_VALUE;

                    int central = -1;
                    int active = -1;
                    int peripheral = -1;

                    for (int i = 0; i < groups.size(); i++) {
                        out.write(">>" + (i + 1) + "\n");
                        double[] indegree = getIndegreeVector(getParticipantsFromGroup(groups.get(i), participants));
                        double[] outdegree = getOutdegreeVector(getParticipantsFromGroup(groups.get(i), participants));

                        out.write(">>" + (i + 1) + ": \n");
                        out.write("Size, Mean indegree, Stddev indegree, Mean outdegree, Stddv outdegree\n");
                        out.write(groups.get(i).size() + "," + VectorAlgebra.mean(indegree) + "," + VectorAlgebra.stdev(indegree)
                                + "," + VectorAlgebra.mean(outdegree) + "," + VectorAlgebra.stdev(outdegree) + "\n");

                        double meanGroup = (VectorAlgebra.mean(indegree) + VectorAlgebra.mean(outdegree)) / 2.0;
                        if (meanGroup > max) {
                            max = meanGroup;
                            central = i;
                        }

                        if (meanGroup < min) {
                            min = meanGroup;
                            peripheral = i;
                        }
                        out.write("Name, Indegree, Outdegree");

                        for (CSCLIndices csclIndices : CSCLIndices.values()) {
                            if (!csclIndices.equals(CSCLIndices.INDEGREE) || !(csclIndices.equals(CSCLIndices.OUTDEGREE))) {
                                out.write("," + csclIndices.toString());
                            }
                        }
                        out.write("\n");

                        for (int j : groups.get(i)) {
                            out.write(participants.get(j).toString());
                            //add other indices
                            for (int ind = 0; ind < CSCLIndices.values().length; ind++) {
                                out.write("," + initialParticipants.get(j).getIndices().get(CSCLIndices.values()[ind]));
                            }
                            out.write("\n");
                        }
                        out.write("\n");
                    }

                    for (int i = 0; i < groups.size(); i++) {
                        if (i != central && i != peripheral) {
                            active = i;
                        }
                    }
                    assessParticipantsToGroup(initialParticipants, groups, central, active, peripheral);

                    out.close();
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static double compareParticipants(ParticipantNormalized p1, ParticipantNormalized p2) {
        if (p1 == null || p2 == null) {
            return -1;
        }
        return VectorAlgebra.euclidianDistance(
                p1.getVector(),
                p2.getVector());
    }

    public static double compareGroupsOfParticipants(List<ParticipantNormalized> participants,
            List<Integer> group1, List<Integer> group2) {

        double[] meanGroup1 = computeClusterCenter(getParticipantsFromGroup(group1, participants));
        double[] meanGroup2 = computeClusterCenter(getParticipantsFromGroup(group2, participants));

        double coef = (double) (group1.size() * group2.size()) / (double) (group1.size() + group2.size());
        return coef * VectorAlgebra.euclidianDistance(meanGroup1, meanGroup2);
    }

    public static double[] computeClusterCenter(List<ParticipantNormalized> participants) {
        double[] indegree = new double[participants.size()];
        double[] outdegree = new double[participants.size()];

        for (int i = 0; i < participants.size(); i++) {
            indegree[i] = participants.get(i).getIndegree();
            outdegree[i] = participants.get(i).getOutdegree();
        }
        return new double[]{VectorAlgebra.mean(indegree),
            VectorAlgebra.mean(outdegree)};
    }

    public static List<ParticipantNormalized> getParticipantsFromGroup(List<Integer> group,
            List<ParticipantNormalized> allParticipants) {
        List<ParticipantNormalized> participantFromGroup = new ArrayList<>();
        for (Integer i : group) {
            participantFromGroup.add(allParticipants.get(i));
        }
        return participantFromGroup;
    }

    public static double[] getIndegreeVector(List<ParticipantNormalized> participants) {
        double[] indegree = new double[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            indegree[i] = participants.get(i).getIndegree();
        }

        return indegree;
    }

    public static double[] getOutdegreeVector(List<ParticipantNormalized> participants) {
        double[] outdegree = new double[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            outdegree[i] = participants.get(i).getOutdegree();
        }

        return outdegree;
    }

    private static void assessParticipantsToGroup(List<Participant> participants, List<List<Integer>> groups,
            int central, int active, int peripheral) {
        for (int i = 0; i < groups.size(); i++) {
            ParticipantGroup group = null;
            if (i == central) {
                group = ParticipantGroup.CENTRAL;
            } else if (i == active) {
                group = ParticipantGroup.ACTIVE;
            } else if (i == peripheral) {
                group = ParticipantGroup.PERIPHERAL;
            }
            for (int j : groups.get(i)) {
                participants.get(j).setParticipantGroup(group);
            }
        }
    }
}
