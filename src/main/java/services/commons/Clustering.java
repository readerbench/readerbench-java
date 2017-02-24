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
package services.commons;

import data.AbstractDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Clustering {

    public static final Logger LOGGER = Logger.getLogger("");
    public static final int MAXIMUM_NUMBER_OF_ITERATIONS = 1000;
    
    private List<AbstractDocument> clustroids;
    private List<List<AbstractDocument>> clusters;

    public List<AbstractDocument> getClustroids() {
        return clustroids;
    }

    public List<List<AbstractDocument>> getClusters() {
        return clusters;
    }
    
    public void performKMeansClustering(List<AbstractDocument> docs, int K) {

        clustroids = new ArrayList<>();

        // sets initialization to random or "smart" node selection for maximum
        // dispersion
        // choose a random initial node
        int randomId = (int) (Math.random() * docs.size());
        AbstractDocument randomDoc = docs.get(randomId);

        LOGGER.info("Initializing k clustroids with best possible dispersion.");
        // compute kNN++
        for (int i = 0; i < K; i++) {
            double minDist = Double.MAX_VALUE;
            AbstractDocument chosenDoc = null; //
            // select word with highest distance
            for (AbstractDocument d : docs) {
                if (!clustroids.contains(d)) {
                    double distance = 0;
                    distance += compareDocs(d, randomDoc);
                    for (int j = 0; j < i; j++) {
                        distance += compareDocs(d, clustroids.get(j));
                    }
                    if (distance < minDist) {
                        minDist = distance;
                        chosenDoc = d;
                    }
                }
            }
            clustroids.add(chosenDoc);
        }

        // cohesion and separation evolution
        double compactness = 0, isolation = 0;
        clusters = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            clusters.add(new ArrayList<>());
        }

        int noIterations = 0;
        // begin assigning process
        while (noIterations < MAXIMUM_NUMBER_OF_ITERATIONS) {
            LOGGER.log(Level.INFO, "Starting iteration no {0}", noIterations);

            boolean changesMade = false;

            // clean clusters
            for (int i = 0; i < K; i++) {
                clusters.set(i, new ArrayList<>());
                clusters.get(i).add(clustroids.get(i));
            }

            double sumDist = 0;
            // assign nodes to clusters
            for (AbstractDocument d : docs) {
                if (!clustroids.contains(d)) {
                    double maxDist = Double.MIN_VALUE;
                    int clusterId = -1;
                    for (int i = 0; i < clustroids.size(); i++) {
                        double dist = compareDocs(d, clustroids.get(i));
                        if (dist > maxDist) {
                            if (dist > maxDist) {
                                maxDist = dist;
                                clusterId = i;
                            }
                        }
                    }

                    sumDist += maxDist;

                    // add current word to corresponding cluster;
                    if (clusterId != -1) {
                        clusters.get(clusterId).add(d);
                    }
                }
            }

            // assign intra-cluster cohesion (compactness)
            compactness = ((double) docs.size()) / sumDist;

            // determine inter-cluster separation (isolation)
            sumDist = 0;
            for (int i = 0; i < clustroids.size() - 1; i++) {
                for (int j = i + 1; j < clustroids.size(); j++) {
                    sumDist += compareDocs(clustroids.get(i), clustroids.get(j));
                }
            }
            isolation = sumDist / ((double) docs.size());

            // recompute clusteroids
            for (int i = 0; i < K; i++) {
                if (clusters.get(i).isEmpty()) {
                    LOGGER.info("Empty cluster!");
                }
                AbstractDocument localCluster = null;
                // determine new centroid
                double maxSim = Double.MIN_VALUE;
                for (AbstractDocument d1 : clusters.get(i)) {
                    double sim = 0;
                    for (AbstractDocument d2 : clusters.get(i)) {
                        sim += compareDocs(d1, d2);
                    }
                    if (sim > maxSim) {
                        localCluster = d1;
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

        LOGGER.log(Level.INFO, "{0} clusters after {1} iterations with {2} compactness and {3} isolation", new Object[]{K, noIterations, Formatting.formatNumber(compactness), Formatting.formatNumber(isolation)});
        for (int i = 0; i < clustroids.size(); i++) {
            System.out.print(">>" + (i + 1) + ": ");
            for (AbstractDocument d : clusters.get(i)) {
                if (clustroids.contains(d)) {
                    LOGGER.log(Level.INFO, "({0}); ", d.getTitleText());
                } else {
                    LOGGER.log(Level.INFO, "{0}; ", d.getTitleText());
                }
            }
        }
        
    }

    public void performAglomerativeClustering(List<AbstractDocument> docs) {

        List<List<Integer>> groups = new ArrayList<>();

        // initialize groups
        for (int i = 0; i < docs.size(); i++) {
            List<Integer> group = new ArrayList<>();
            group.add(i);
            groups.add(group);
        }

        int noInterations = 0;

        while (noInterations < docs.size() - 1) {
            // determine max similarity
            double maxSim = Double.MIN_VALUE;
            int group1 = -1, group2 = -1;
            for (int i = 0; i < groups.size() - 1; i++) {
                for (int j = i + 1; j < groups.size(); j++) {
                    double dist = compareGroups(docs, groups.get(i), groups.get(j));
                    if (dist > maxSim) {
                        maxSim = dist;
                        group1 = i;
                        group2 = j;
                    }
                }
            }

            // merge groups 1 & 2 that have the minimum distance
            groups.get(group1).addAll(groups.get(group2));

            groups.remove(group2);

            noInterations++;
            // display groups
            System.out.println("\n" + noInterations
                    + " iteration (max similarity = " + maxSim + "):");
            for (int i = 0; i < groups.size(); i++) {
                System.out.print(">>" + (i + 1) + ": ");
                for (int j : groups.get(i)) {
                    System.out.print(docs.get(j).getTitleText() + "; ");
                }
                System.out.println();
            }

        }
    }

    public abstract double compareDocs(AbstractDocument d1, AbstractDocument d2);

    public double compareGroups(List<AbstractDocument> docs,
            List<Integer> group1, List<Integer> group2) {
        // determine group average
        double dist = 0;
        for (int i : group1) {
            for (int j : group2) {
                dist += compareDocs(docs.get(i), docs.get(j));
            }
        }
        if (!group1.isEmpty() && !group2.isEmpty()) {
            return dist / (group1.size() * group2.size());
        }
        return 0;
    }
}