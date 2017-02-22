package runtime.pairwise;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.commons.VectorAlgebra;

public class Ranking {

    private static final int NO_MAX_ITERATIONS = 1000000;
    int noDocs = 600;
    private final int[][] compareUnderstanding = new int[noDocs + 1][noDocs + 1];
    private final int[][] compareSpeed = new int[noDocs + 1][noDocs + 1];
    private final int[][] compareFamiliarity = new int[noDocs + 1][noDocs + 1];

    public Ranking(String path) {
        parse(path);
    }

    public void performDFS(Set<Integer> s, int i, int[][] compare) {
        s.add(i);
        System.out.print(i + " ");
        for (int j = 1; j < compare[i].length; j++) {
            if (compare[i][j] != 0) {
                if (s.contains(j)) {
                    System.out.print("(" + i + ">>" + j + ") ");
                } else {
                    performDFS(s, j, compare);
                }
            }
        }
    }

    public void performDFS(int[][] compare) {
        System.out.println("Depth first search");
        Set<Integer> marked = new TreeSet<>();
        for (int i = 1; i < compare.length; i++) {
            if (!marked.contains(i)) {
                performDFS(marked, i, compare);
                System.out.println();
            }
        }
    }

    public void performTopologicSort(int[][] compare) {
        System.out.println("Topological sort");
        int rank = 0;
        Set<Integer> ranked = new TreeSet<>();
        int tolerance = 0;
        while (true) {

            Set<Integer> s = new TreeSet<>();
            for (int i = 1; i < compare.length; i++) {
                int noIncoming = 0;
                // determine number of incoming edges for node i
                for (int j = 1; j < compare[i].length; j++) {
                    noIncoming += compare[j][i];
                }
                if (noIncoming <= tolerance && !ranked.contains(i)) {
                    s.add(i);
                    ranked.add(i);
                }
            }
            if (!s.isEmpty()) {
                System.out.println("Rank " + rank + "  and  tolerance " + tolerance + ":");
                System.out.println(s);
                rank++;
                tolerance = 0;

                for (int i : s) {
                    for (int j = 1; j < compare[i].length; j++) {
                        compare[i][j] = 0;
                        compare[j][i] = 0;
                    }
                }
            } else {
                // determine if all elements have been ranked
                boolean containsUnranked = false;
                for (int i = 1; i < compare.length; i++) {
                    if (!ranked.contains(i)) {
                        containsUnranked = true;
                        break;
                    }
                }
                if (containsUnranked) {
                    tolerance++;
                } else {
                    break;
                }
            }
        }
    }

    public void printConditionally(String criteriaName, double[] v, double threshold1, double threshold2) {
        System.out.print(criteriaName + ": ");
        for (int i = 1; i < v.length; i++) {
            if (v[i] >= threshold1 && v[i] < threshold2) {
                System.out.print("(" + i + ">>" + Formatting.formatNumber(v[i]) + ") ");
            }
        }
        System.out.println();
    }

    private class CompareDocs implements Comparable<CompareDocs> {

        private final int ID;
        private final double likelihood;

        public CompareDocs(int ID, double likelihood) {
            this.ID = ID;
            this.likelihood = likelihood;
        }

        public int getID() {
            return ID;
        }

        public double getLikelihood() {
            return likelihood;
        }

        @Override
        public int compareTo(CompareDocs o) {
            return (int) Math.signum(o.getLikelihood() - this.getLikelihood());
        }
    }

    public void performMM(int[][] compare, double epsilon) {
        double[] gamma = new double[noDocs + 1];
        double[] newGamma = null;
        for (int i = 1; i < compare.length; i++) {
            gamma[i] = Math.random();
        }
        gamma = VectorAlgebra.normalize(gamma);

        int noIterations = 0;
        while (true) {
            newGamma = new double[noDocs + 1];

            for (int i = 1; i < compare.length; i++) {
                double Wi = 0;
                for (int j = 1; j < compare.length; j++) {
                    Wi += compare[j][i];
                }
                double sum = 0;
                for (int j = 1; j < compare.length; j++) {
                    if ((gamma[i] + gamma[j]) != 0) {
                        sum += (compare[i][j] + compare[j][i]) / (gamma[i] + gamma[j]);
                    }
                }
                if (sum != 0) {
                    newGamma[i] = Wi / sum;
                }
            }

            newGamma = VectorAlgebra.normalize(newGamma);
            noIterations++;
            // check convergence
            if (VectorAlgebra.norm(VectorAlgebra.difference(newGamma, gamma), 2) < epsilon
                    || noIterations == NO_MAX_ITERATIONS) {
                break;
            }
            gamma = newGamma;
        }
        System.out.println("Bradley-Terry Model\nReached convergence in " + noIterations
                + " iterations for L2 norm smaller than " + epsilon);

        List<CompareDocs> results = new ArrayList<>();
        for (int i = 1; i < compare.length; i++) {
            results.add(new CompareDocs(i, newGamma[i]));
        }

        Collections.sort(results);

        for (CompareDocs c : results) {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(10);
            System.out.println(c.getID() + "\t" + df.format(c.getLikelihood()));
        }
    }

    public void performTopologicSort() {
        System.out.println("Ranked by more easy to understand:");
//        performDFS(compareUnderstanding);
//        System.out.println("\n----------");
        performMM(compareUnderstanding, 0.00000001);
//        System.out.println("\n----------");
//        performTopologicSort(compareUnderstanding);

        System.out.println("\n\nRanked by more quick to read:");
//        performDFS(compareSpeed);
//        System.out.println("\n----------");
        performMM(compareSpeed, 0.00000001);
//        System.out.println("\n----------");
//        performTopologicSort(compareSpeed);

        System.out.println("\n\nRanked by more familiar:");
//        performDFS(compareFamiliarity);
//        System.out.println("\n----------");
        performMM(compareFamiliarity, 0.00000001);
//        System.out.println("\n----------");
//        performTopologicSort(compareFamiliarity);
    }

    private void parse(String path) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
            String line = in.readLine();
            while ((line = in.readLine()) != null) {
                if (line.length() > 0) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    String rater = st.nextToken().trim();
                    int id1 = Integer.valueOf(st.nextToken());
                    int id2 = Integer.valueOf(st.nextToken());
                    int easierToRead = Integer.valueOf(st.nextToken());
                    if (easierToRead == 1) {
                        compareUnderstanding[id1][id2]++;
                    } else if (easierToRead == 2) {
                        compareUnderstanding[id2][id1]++;
                    }

                    int quickerToRead = Integer.valueOf(st.nextToken());
                    if (quickerToRead == 1) {
                        compareSpeed[id1][id2]++;
                    } else if (quickerToRead == 2) {
                        compareSpeed[id2][id1]++;
                    }

                    int moreFamiliar = Integer.valueOf(st.nextToken());
                    if (moreFamiliar == 1) {
                        compareFamiliarity[id1][id2]++;
                    } else if (moreFamiliar == 2) {
                        compareFamiliarity[id2][id1]++;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        Ranking r = new Ranking("resources/in/pairwise/cleaned ALRC data v2.csv");
        r.performTopologicSort();
    }
}
