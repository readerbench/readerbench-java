package services.complexity;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import data.AbstractDocument;

public class Clustering {
	static Logger logger = Logger.getLogger(Clustering.class);
	public static final int MAXIMUM_NUMBER_OF_ITERATIONS = 1000;

	public static void performKMeansClustering(List<AbstractDocument> docs,
			int K) {

		List<AbstractDocument> clustroids = new LinkedList<AbstractDocument>();

		// sets initialization to random or "smart" node selection for maximum
		// dispersion

		// choose a random initial node
		int randomId = (int) (Math.random() * docs.size());
		AbstractDocument randomDoc = docs.get(randomId);

		logger.info("Initializing k clustroids with best possible dispersion.");
		// compute kNN++
		for (int i = 0; i < K; i++) {
			double minDist = Double.MAX_VALUE;
			AbstractDocument chosenDoc = null; //
			// select word with highest distance
			for (AbstractDocument d : docs) {
				if (!clustroids.contains(d)) {
					double distance = 0;
					distance += compareDocs(d, randomDoc);
					for (int j = 0; j < i; j++)
						distance += compareDocs(d, clustroids.get(j));
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
		List<List<AbstractDocument>> clusters = new Vector<List<AbstractDocument>>();
		for (int i = 0; i < K; i++)
			clusters.add(new LinkedList<AbstractDocument>());

		int noIterations = 0;
		// begin assigning process
		while (noIterations < MAXIMUM_NUMBER_OF_ITERATIONS) {
			logger.info("Starting iteration no " + noIterations);

			boolean changesMade = false;

			// clean clusters
			for (int i = 0; i < K; i++) {
				clusters.set(i, new LinkedList<AbstractDocument>());
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
			for (int i = 0; i < clustroids.size() - 1; i++)
				for (int j = i + 1; j < clustroids.size(); j++)
					sumDist += compareDocs(clustroids.get(i), clustroids.get(j));
			isolation = sumDist / ((double) docs.size());

			// recompute clusteroids
			for (int i = 0; i < K; i++) {
				if (clusters.get(i).size() == 0)
					System.out.println("Empty cluster!");
				AbstractDocument localCluster = null;
				// determine new centroid
				double maxDist = Double.MIN_VALUE;
				for (AbstractDocument d1 : clusters.get(i)) {
					double dist = 0;
					for (AbstractDocument d2 : clusters.get(i))
						dist += compareDocs(d1, d2);
					if (dist > maxDist) {
						localCluster = d1;
						dist = maxDist;
					}
				}
				if (localCluster != null
						&& !localCluster.equals(clustroids.get(i))) {
					changesMade = true;
					clustroids.set(i, localCluster);
				}
			}

			// if there are no further changes we have reached convergence
			if (!changesMade)
				break;

			noIterations++;
		}

		System.out.println(K + " clusters after " + noIterations
				+ " iterations with " + Formatting.formatNumber(compactness)
				+ " compactness and " + Formatting.formatNumber(isolation)
				+ " isolation");
		for (int i = 0; i < clustroids.size(); i++) {
			System.out.print(">>" + (i + 1) + ": ");
			for (AbstractDocument d : clusters.get(i))
				if (clustroids.contains(d))
					System.out.print("(" + d.getTitleText() + "); ");
				else
					System.out.print(d.getTitleText() + "; ");
			System.out.println();
		}
	}

	public static void performAglomerativeClustering(List<AbstractDocument> docs) {

		List<List<Integer>> groups = new LinkedList<List<Integer>>();

		// initialize groups
		for (int i = 0; i < docs.size(); i++) {
			List<Integer> group = new LinkedList<Integer>();
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
					double dist = compareGroups(docs, groups.get(i),
							groups.get(j));
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

	public static double compareDocs(AbstractDocument d1, AbstractDocument d2) {
		if (d1 == null || d2 == null || d1.getComplexityIndices() == null
				|| d2.getComplexityIndices() == null)
			return -1;
		return VectorAlgebra.cosineSimilarity(d1.getComplexityIndices(),
				d2.getComplexityIndices());
	}

	public static double compareGroups(List<AbstractDocument> docs,
			List<Integer> group1, List<Integer> group2) {
		// determine group average
		double dist = 0;
		for (int i : group1) {
			for (int j : group2) {
				dist += compareDocs(docs.get(i), docs.get(j));
			}
		}
		if (group1.size() != 0 && group2.size() != 0)
			return dist / (group1.size() * group2.size());
		return 0;
	}
}
