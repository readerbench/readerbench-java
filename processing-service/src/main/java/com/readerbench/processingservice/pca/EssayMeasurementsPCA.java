package com.readerbench.processingservice.pca;

import com.readerbench.coreservices.commons.VectorAlgebra;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

import java.io.*;
import java.util.*;

/**
 * Created by Robert Botarleanu on 05-Apr-17.
 */
public class EssayMeasurementsPCA {

    // Values are considered equal to 0 under this threshhold
    private static final double EPSILON = 1e-6;

    private static final double MINIMUM_COMMUNALITY = 0.4;
    private static final double MINIMUM_LOAD = 0.4;
    private static final double CORRELATION_THRESHOLD = 0.9;
    private static final double OUTLIER_PERCENTAGE = 0.1;
    private static final double SKEWNESS_LIMIT = 2;
    private static final double KURTOSIS_LIMIT = 3.5;
    private static final int COMPONENT_LIMIT = 5;
    private static final double VARIANCE_THRESHOLD = 0.03;

    private static final int DESCRIPTIVE_COLUMNS_COUNT = 2;

    public static class MetricValue implements Comparable<MetricValue> {
        boolean isDescriptive;
        String desc; // either the category or the file name
        Double value;

        MetricValue() {
            super();
        }

        MetricValue(String desc) {
            this.desc = desc;
            isDescriptive = true;
        }

        MetricValue(Double value) {
            this.value = value;
            isDescriptive = false;
        }

        @Override
        public String toString() {
            return isDescriptive ? desc : value.toString();
        }

        @Override
        public int compareTo(MetricValue o) {
            if (isDescriptive && o.isDescriptive)
                return desc.compareTo(o.desc);
            if (!isDescriptive && !o.isDescriptive)
                return value.compareTo(o.value);
            if (!isDescriptive && o.isDescriptive)
                return 1;
            return -1;
        }
    }

    /*
     * Parses a CSV file into a hashmap.
     */
    public static Map<String, ArrayList<MetricValue>> parseCSV(String path) {
        Map<String, ArrayList<MetricValue>> document = new LinkedHashMap<>();
        Set<String> badMetrics = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            String[] header = line.split(",");
            for (String metric : header) {
                document.put(metric, new ArrayList<>());
            }
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 1) continue;
                String[] values = line.split(",", -1);
                // The first 2 are the category and the filename
                document.get(header[0]).add(new MetricValue(values[0]));
                document.get(header[1]).add(new MetricValue(values[1]));
                // The rest are actual values
                for (int i = 2; i < values.length; ++i) {
                    if (values[i].equals("NaN")) {
                        badMetrics.add(header[i]);
                    }
                    try {
                        Double v = Double.parseDouble(values[i]);
                        document.get(header[i]).add(new MetricValue(v));
                    } catch (NumberFormatException ex) {
                        document.get(header[i]).add(new MetricValue(0.));
                    }
                }
            }
            badMetrics.forEach(metric -> document.remove(metric));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    /*
     * Removes all metrics that do not have at least 20% non-null values.
     */
    public Map<String, ArrayList<MetricValue>> removeLocalMetrics(Map<String, ArrayList<MetricValue>> doc) {
        List<String> headers = new ArrayList<String>(doc.keySet());
        List<String> metricsToPrune = new ArrayList<>();

        // Search for metrics to prune
        int N = doc.get(headers.get(0)).size();
        for (int i = 2; i < headers.size(); ++i) {
            // Count how many close-to-zero values this metric has for the files
            int zeros = 0;
            int negones = 0;
            for (MetricValue mv : doc.get(headers.get(i))) {
                if (!mv.isDescriptive && mv.value == 0) {
                    ++zeros;
                }
                if (!mv.isDescriptive && mv.value == -1) {
                    ++negones;
                }
            }
            // If the number is over 20% then this metric will be deleted
            if (zeros >= 0.2 * N || negones >= 0.2 * N) {
                metricsToPrune.add(headers.get(i));
            }
        }

        // Delete the selected metrics
        System.out.println("Metrics before first stage pruning: " + doc.keySet().size());
        metricsToPrune.forEach(metric -> doc.remove(metric));
        System.out.println("Metrics after first stage pruning: " + doc.keySet().size());

        return doc;
    }

    private double[] getDoubleArray(ArrayList<MetricValue> l) {
        double[] a = new double[l.size()];
        for (int i = 0; i < l.size(); ++i) {
            a[i] = l.get(i).value;
        }

        return a;
    }

    private List<Integer> selectMetricsForRemoval(SortedSet<Map.Entry<Integer, ArrayList<Integer>>> sortedG,
                                                  Map<Integer, ArrayList<Integer>> G) {
        Map<Integer, Boolean> eliminated = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<Integer>> e : sortedG) {
            Integer u = e.getKey();
            // Check if this index has any redundancies
            if (e.getValue().size() == 0) {
                continue;
            }
            // Check if removing this index will cause others to lose all redundancies
            boolean isSafe = true;
            for (Integer v : e.getValue()) {
                if (eliminated.containsKey(v) && G.get(v).size() == 1 && G.get(v).contains(u)) {
                    isSafe = false;
                    break;
                }
            }

            if (isSafe) {
                eliminated.put(u, true);
                e.getValue().forEach(v -> G.get(v).remove(u));
            }
        }

        return new ArrayList<Integer>(eliminated.keySet());
    }

    private Map<String, ArrayList<MetricValue>> removeOutliers(Map<String, ArrayList<MetricValue>> doc,
                                                               String measurementsPath) {
        List<String> headers = new ArrayList<String>(doc.keySet());
        System.out.println("Removing documents that are outliers for at least " + OUTLIER_PERCENTAGE * 100 + "% of metrics.");
        System.out.println("Initial document count: " + doc.get(headers.get(0)).size());

        // Compute means and standard deviations for the values held in the doc map
        double[] means = new double[headers.size() - DESCRIPTIVE_COLUMNS_COUNT];
        double[] sds = new double[headers.size() - DESCRIPTIVE_COLUMNS_COUNT];

        for (int i = DESCRIPTIVE_COLUMNS_COUNT; i < headers.size(); ++i) {
            List<MetricValue> values = doc.get(headers.get(i));
            double[] numericalValues = new double[values.size()];
            for (int j = 0; j < values.size(); ++j)
                numericalValues[j] = values.get(j).value;
            means[i - DESCRIPTIVE_COLUMNS_COUNT] = VectorAlgebra.mean(numericalValues);
            sds[i - DESCRIPTIVE_COLUMNS_COUNT] = VectorAlgebra.stdev(numericalValues);
        }


        List<Integer> documentsToRemove = new ArrayList<>();
        int documentCount = doc.get(headers.get(0)).size();

        // Find documents that are outliers for too many metrics
        for (int i = 0; i < documentCount; ++i) {
            int outlierCount = 0;
            for (int j = 2; j < headers.size(); ++j) {
                double value = doc.get(headers.get(j)).get(i).value;
                int k = j - 2;
                // Check if document i is an outlier for metric j
                if ((value < means[k] - 2 * sds[k]) || (value > means[k] + 2 * sds[k])) {
                    ++outlierCount;
                }
            }
            if (outlierCount >= OUTLIER_PERCENTAGE * headers.size()) {
                documentsToRemove.add(i);
            }
        }

        // Sort the indices in descending order to prevent any conflict during removal
        Collections.sort(documentsToRemove, Collections.reverseOrder());

        // Remove selected documents
        for (int documentIndex : documentsToRemove) {
            for (String header : headers) {
                doc.get(header).remove(documentIndex);
            }
        }

        System.out.println("Documents left after pruning: " + doc.get(headers.get(0)).size());
        return doc;
    }

    private Map<String, ArrayList<MetricValue>> pruneSkewnessKurtosis(Map<String, ArrayList<MetricValue>> doc,
                                                                      String measurementsPath) {
        System.out.println("Removing metrics by skewness and kurtosis.");
        String fullPath = new File(measurementsPath).getAbsolutePath().replace("\\", "\\\\");
        // Construct the R matrix for the corelation computations
        Map<Integer, ArrayList<Integer>> G = new TreeMap<>();
        List<String> headers = new ArrayList<String>(doc.keySet());
        int N = headers.size() - 2; // ignore the category and filename
        List<String> metricsToRemove = new ArrayList<>();

        double[] skewness = new double[headers.size() - DESCRIPTIVE_COLUMNS_COUNT];
        double[] kurtosis = new double[headers.size() - DESCRIPTIVE_COLUMNS_COUNT];

        Skewness apacheSkewness = new Skewness();
        Kurtosis apacheKurtosis = new Kurtosis();


        for (int i = DESCRIPTIVE_COLUMNS_COUNT; i < headers.size(); ++i) {
            List<MetricValue> values = doc.get(headers.get(i));
            double[] numericalValues = new double[values.size()];
            for (int j = 0; j < values.size(); ++j)
                numericalValues[j] = values.get(j).value;
            skewness[i - DESCRIPTIVE_COLUMNS_COUNT] = apacheSkewness.evaluate(numericalValues, 0, numericalValues.length);
            // The Kurtosis implementation Apache Math provides uses the Fisher definition, which implies subtracting 3
            // in order to have the kurtosis value of a normal distribution at 0. We add this value back to preserve
            // compatibility with the previous, R based, implementation.
            kurtosis[i - DESCRIPTIVE_COLUMNS_COUNT] = apacheKurtosis.evaluate(numericalValues, 0, numericalValues.length) + 3;
        }

        for (int i = 0; i < skewness.length; ++i) {
            if (Math.abs(skewness[i]) > SKEWNESS_LIMIT || Math.abs(kurtosis[i]) > KURTOSIS_LIMIT) {
                metricsToRemove.add(headers.get(i + 2));
            }
        }
        metricsToRemove.forEach(metric -> doc.remove(metric));
        System.out.println("Metrics left after skewness/kurtosis pruning: " + doc.size());
        return doc;
    }

    private Map<String, ArrayList<MetricValue>> removeCorrelatedMetrics(Map<String, ArrayList<MetricValue>> doc,
                                                                        String measurementsPath) {
        // Construct the R matrix for the corelation computations
        Map<Integer, ArrayList<Integer>> G = new TreeMap<>();
        List<String> headers = new ArrayList<String>(doc.keySet());
        int N = headers.size() - 2; // number of columns, ignore the category and filename
        int M = doc.get(headers.get(0)).size(); // number of rows

        double[][] docValues = new double[M][N];
        for (int i = 0; i < N; ++i) {
            List<MetricValue> columnValues = doc.get(headers.get(i + DESCRIPTIVE_COLUMNS_COUNT));
            for (int j = 0; j < M; ++j)
                docValues[j][i] = columnValues.get(j).value;
        }

        PearsonsCorrelation apachePearsonCorr = new PearsonsCorrelation();
        double[][] correlationMatrix = apachePearsonCorr.computeCorrelationMatrix(docValues).getData();

        System.out.println("Pruning correlated metrics.");
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                if (i == j) continue;
                if (correlationMatrix[i][j] > CORRELATION_THRESHOLD) {
                    if (!G.containsKey(i + 2)) {
                        G.put(i + 2, new ArrayList<>());
                    }
                    G.get(i + 2).add(j + 2);
                }
            }
        }
        // Sort by the number of redundancies
        SortedSet<Map.Entry<Integer, ArrayList<Integer>>> sortedG = new TreeSet<>(
                (o1, o2) -> {
                    int ans = new Integer(o2.getValue().size()).compareTo(new Integer(o1.getValue().size()));
                    return ans == 0 ? 1 : ans;
                }
        );
        sortedG.addAll(G.entrySet());

        List<Integer> toRemove = selectMetricsForRemoval(sortedG, G);
        toRemove.forEach(index -> doc.remove(headers.get(index)));
        System.out.println("Metrics after third stage pruning: " + doc.entrySet().size());

        return doc;
    }


    private List<String> selectOverloadedMetrics(List<String> headers, double[][] loadings) {
        List<String> metricsToRemove = new ArrayList<>();

        for (int i = 0; i < loadings.length; ++i) {
            int count = 0;
            for (int j = 0; j < loadings[i].length; ++j) {
                if (Math.abs(loadings[i][j]) >= MINIMUM_LOAD) {
                    ++count;
                }
            }
            if (count > 1) {
                metricsToRemove.add(headers.get(i + 2));
            }
        }

        return metricsToRemove;
    }

    private Map<String, ArrayList<MetricValue>> PCAPruning(Map<String, ArrayList<MetricValue>> doc,
                                                           String measurementsPath,
                                                           String loadingsPath,
                                                           String zScorePath,
                                                           String path) {
        System.out.println("Beginning PCA pruning...");
        String fullMeasurementsPath = new File(measurementsPath).getAbsolutePath().replace("\\", "\\\\");
        String fullLoadingsPath = new File(loadingsPath).getAbsolutePath().replace("\\", "\\\\");
        int componentCount = 20;
        List<String> metricsToRemove = new ArrayList<>();

        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(doc);
        pca.pca(true);
        pca.writeMeansToFile(zScorePath + "_means.csv");
        pca.writeStdevsToFile(zScorePath + "_sds.csv");

        double[] variances = pca.getPcVariances();
        double[][] eigenVectors = pca.getPcRotation();

        // Select components that explain a variance of at least 1%
        componentCount = 0;
        double cumulatedVar = 0;
        for (int i = 0; i < variances.length && variances[i] > VARIANCE_THRESHOLD && componentCount < COMPONENT_LIMIT;
             ++i, ++componentCount) {
            cumulatedVar += variances[i];
            System.out.println("Variance " + i + " is " + variances[i]);
        }
        System.out.println("Component count: " + componentCount);
        System.out.println("Cumulated variance: " + cumulatedVar);

        // Get the first componentCount rotated loadings
        pca.computeComponentLoadings(componentCount);

        // Write the computed loadings
        pca.writeLoadingsToFile(fullLoadingsPath);

        // Run the loading algorithm
        loadingsRefinement(loadingsPath, path, eigenVectors);

        System.out.println("Metrics left after PCA pruning: " + doc.size());

        return doc;
    }

    public static void writeToCSV(Map<String, ArrayList<MetricValue>> doc, String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            List<String> headers = new ArrayList<String>(doc.keySet());
            bw.write(StringUtils.join(headers, ","));
            bw.write("\n");
            for (int i = 0; i < doc.get(headers.get(0)).size(); ++i) {
                // Print category and filename first
                bw.write(doc.get(headers.get(0)).get(i).toString() + ",");
                bw.write(doc.get(headers.get(1)).get(i).toString() + ",");
                // Construct a list of the other values and add it
                List<String> v = new ArrayList<>();
                for (int k = 2; k < headers.size(); ++k) {
                    v.add(doc.get(headers.get(k)).get(i).toString());
                }
                bw.write(StringUtils.join(v, ","));
                if (i != doc.get(headers.get(0)).size() - 1) {
                    bw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadingsRefinement(String loadingsPath, String path, double[][] eigenVectors) {
        Map<String, ArrayList<MetricValue>> doc = parseCSV(loadingsPath);
        List<String> headers = new ArrayList<>(doc.keySet());

        int metricCount = doc.get(headers.get(1)).size();
        for (int j = 0; j < metricCount; ++j) {
            double bestEigenValue = -Float.MIN_VALUE;
            int previousBest = -1;
            for (int i = 1; i < headers.size(); ++i) {
                double v = Double.parseDouble(doc.get(headers.get(i)).get(j).toString());
                double eigenVectorValue = Math.abs(eigenVectors[j][i - 1]);
                if (Math.abs(v) < MINIMUM_LOAD) {
                    doc.get(headers.get(i)).set(j, new MetricValue(0.));
                    continue;
                }

                if (previousBest == -1) {
                    bestEigenValue = eigenVectorValue;
                    previousBest = i;
                } else if (eigenVectorValue > bestEigenValue) {
                    doc.get(headers.get(previousBest)).set(j, new MetricValue(0.));
                    bestEigenValue = eigenVectorValue;
                    previousBest = i;
                } else {
                    doc.get(headers.get(i)).set(j, new MetricValue(0.));
                }
            }
        }

        List<String> componentsToRemove = new ArrayList<>();
        for (int i = 1; i < headers.size(); ++i) {
            List<MetricValue> l = doc.get(headers.get(i));
            boolean allZero = true;
            for (int j = 0; j < l.size(); ++j) {
                double v = Double.parseDouble(l.get(j).toString());
                if (Math.abs(v) >= MINIMUM_LOAD) {
                    allZero = false;
                    break;
                }
            }
            if (allZero) {
                componentsToRemove.add(headers.get(i));
            }
        }
        componentsToRemove.forEach(component -> doc.remove(component));

        List<Integer> metricsToRemove = new ArrayList<>();
        for (int j = 0; j < doc.get(headers.get(0)).size(); ++j) {
            boolean allZeros = true;
            for (int i = 1; i < headers.size(); ++i) {
                if (Math.abs(Double.parseDouble(doc.get(headers.get(i)).get(j).toString())) >= MINIMUM_LOAD) {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros) {
                metricsToRemove.add(j);
            }
        }

        Collections.sort(metricsToRemove, Collections.reverseOrder());
        metricsToRemove.forEach(metric -> {
            for (int i = 0; i < headers.size(); ++i) {
                doc.get(headers.get(i)).remove((int) metric);
            }
        });

        writeToCSV(doc, path + "/pca.csv");
        formatLoadings(doc, loadingsPath);
    }

    private void formatLoadings(Map<String, ArrayList<MetricValue>> doc, String path) {
        List<String> headers = new ArrayList<>(doc.keySet());
        Map<String, ArrayList<MetricValue>> newDoc = new LinkedHashMap<>();

        // Create a map of metric indexes, by component
        Map<String, Integer> indexes = new HashMap<>();
        List<Integer> startPositions = new ArrayList<>();
        int indexesAlreadyAdded = 0;
        for (int c = 1; c < headers.size(); ++c) {
            int crt = 0;
            for (int i = 0; i < doc.get(headers.get(c)).size(); ++i) {
                if (Double.parseDouble(doc.get(headers.get(c)).get(i).toString()) == 0) {
                    continue;
                }

                String metric = doc.get(headers.get(0)).get(i).toString();
                indexes.put(metric, indexesAlreadyAdded + crt++);
            }

            startPositions.add(indexesAlreadyAdded);
            indexesAlreadyAdded += crt;
        }

        // Add the metrics in their relative order
        newDoc.put(headers.get(0), new ArrayList<>(doc.get(headers.get(0))));
        for (String m : indexes.keySet()) {
            int index = indexes.get(m);
            newDoc.get(headers.get(0)).set(index, new MetricValue(m));
        }

        class Pair implements Comparable {

            String metric;
            Double value;

            Pair(String metric, Double value) {
                super();
                this.metric = metric;
                this.value = value;
            }

            @Override
            public int compareTo(Object o) {
                return this.value.compareTo(((Pair) o).value);
            }

            @Override
            public String toString() {
                return metric + ": " + value;
            }
        }

        // Sort the metric values for each component
        Double highestAbsoluteValue = -0.1;
        for (int c = 1; c < headers.size(); ++c) {
            String component = headers.get(c);
            List<Pair> values = new ArrayList<>();
            for (int i = 0; i < doc.get(component).size(); ++i) {
                MetricValue mv = doc.get(component).get(i);
                Double v = (Double.parseDouble(mv.toString()));
                if (Math.abs(highestAbsoluteValue) < Math.abs(v)) {
                    highestAbsoluteValue = v;
                }
                String metric = doc.get(headers.get(0)).get(i).toString();
                values.add(new Pair(metric, v));
            }

            Collections.sort(values);
            if (highestAbsoluteValue > 0) {
                Collections.reverse(values);
            }

            int start = startPositions.get(c - 1);
            newDoc.put(component, new ArrayList<>());
            for (int i = 0; i < values.size(); ++i) {
                newDoc.get(component).add(new MetricValue(""));
            }
            for (int i = 0; i < values.size(); ++i) {
                String metric = values.get(i).metric;
                int index = indexes.get(metric);
                if (values.get(i).value == 0) {
                    continue;
                } else {
                    newDoc.get(component).set(index, new MetricValue(values.get(i).value));
                }
            }
            int crt = 0;
            for (int i = 0; i < values.size(); ++i) {
                if (values.get(i).value == 0) {
                    continue;
                }

                String m = values.get(i).metric;
                MetricValue mv = new MetricValue(values.get(i).value);
                int index = indexes.get(m);

                // The current element is at position start + crt, so we swap with the element at that position
                MetricValue aux = newDoc.get(headers.get(0)).get(index); // swap at the first column
                newDoc.get(headers.get(0)).set(index, newDoc.get(headers.get(0)).get(start + crt));
                newDoc.get(headers.get(0)).set(start + crt, aux);

                aux = newDoc.get(component).get(start + crt); // swap at current component column
                newDoc.get(component).set(index, aux);
                newDoc.get(component).set(start + crt, mv);

                crt++;

                // Update the indexes
                String mSwap = newDoc.get(headers.get(0)).get(index).toString();
                indexes.remove(mSwap);
                indexes.put(mSwap, index);
                indexes.remove(m);
                indexes.put(m, start + crt);
            }
        }

        writeToCSV(newDoc, path);
    }

    public static Map<String, Double> parseZscoreCSV(String path) {
        Map<String, Double> document = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 1) continue;
                String[] values = line.split(",", -1);
                document.put(values[0].replace("\"", ""), Double.parseDouble(values[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    public static Map<String, Double> parseAoA(String path) {
        Map<String, Double> document = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 1) continue;
                String[] values = line.split(",", -1);
                try {
                    document.put(values[0].replace("\"", ""), Double.parseDouble(values[1]));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    public void computeScores(String documentPath, String path) {
        String zScoresPath = path + "/zscores";
        String scoresPath = path + "/scores.csv";
        String loadingsPath = path + "/loadings.csv";
        Map<String, ArrayList<MetricValue>> loadings = parseCSV(loadingsPath);
        Map<String, ArrayList<MetricValue>> doc = parseCSV(documentPath);
        Map<String, Double> means = parseZscoreCSV(zScoresPath + "_means.csv");
        Map<String, Double> sds = parseZscoreCSV(zScoresPath + "_sds.csv");

        Map<String, ArrayList<MetricValue>> scores = new LinkedHashMap<>();

        List<String> docHeaders = new ArrayList<>(doc.keySet());
        List<String> loadingsHeaders = new ArrayList<>(loadings.keySet());

        // Put the headers and the first 2 columns
        scores.put(docHeaders.get(0), new ArrayList<>(doc.get(docHeaders.get(0))));
        scores.put(docHeaders.get(1), new ArrayList<>(doc.get(docHeaders.get(1))));
        for (int c = 1; c < loadingsHeaders.size(); ++c) {
            String component = loadingsHeaders.get(c);
            scores.put(component, new ArrayList<MetricValue>());
            // Compute the score for this component for each file
            for (int i = 0; i < doc.get(docHeaders.get(0)).size(); ++i) {
                double score = 0;
                String file = doc.get(docHeaders.get(0)).get(i).toString();
                for (int m = 0; m < loadings.get(component).size(); ++m) {
                    MetricValue mv = loadings.get(component).get(m);
                    try {
                        Double k = Double.parseDouble(mv.toString()); // loading constant
                        String metric = loadings.get(loadingsHeaders.get(0)).get(m).toString().replace("\"", "");
                        try {
                            Double v = Double.parseDouble(doc.get(metric).get(i).toString()); // document measurement
                            v = (v - means.get(metric)) / sds.get(metric); // use z-score
                            score += k * v;
                        } catch (NullPointerException e) {
                            Double v = Double.parseDouble(doc.get(metric.replace(".", " ")).get(i).toString()); // document measurement
                            v = (v - means.get(metric)) / sds.get(metric); // use z-score
                            score += k * v;
                        }

                    } catch (NumberFormatException e) { // Will fail for null-string loadings
                        continue;
                    }
                }
                scores.get(component).add(new MetricValue(score));
            }
        }

        // Compute mean and sdevs for each component
        List<String> scoresHeaders = new ArrayList<>(scores.keySet());
        Map<String, ArrayList<MetricValue>> scoreData = new LinkedHashMap<>();
        MetricValue[] types = {new MetricValue("mean"), new MetricValue("stddev")};
        scoreData.put("type", new ArrayList<>(Arrays.asList(types)));

        for (int c = 2; c < scoresHeaders.size(); ++c) {
            String component = scoresHeaders.get(c);
            int docCount = scores.get(scoresHeaders.get(0)).size();
            double[] v = new double[docCount];
            for (int i = 0; i < docCount; ++i) {
                v[i] = scores.get(component).get(i).value;
            }
            Double mean = VectorAlgebra.mean(v);
            Double stddev = VectorAlgebra.stdev(v);

            MetricValue[] mv = {new MetricValue(mean), new MetricValue(stddev)};
            scoreData.put(component, new ArrayList<>(Arrays.asList(mv)));
        }

        writeToCSV(scores, scoresPath);
        writeToCSV(scoreData, path + "/scoresData.csv");
    }

    public void prettifyLoadings(String path, String output) {
        Map<String, ArrayList<MetricValue>> doc = parseCSV(path);
        List<String> headers = new ArrayList<>(doc.keySet());
        for (int i = 1; i < headers.size(); ++i) {
            List<MetricValue> l = doc.get(headers.get(i));
            for (int j = 0; j < l.size(); ++j) {
                double v = Double.parseDouble(l.get(j).toString());
                if (Math.abs(v) < MINIMUM_LOAD) {
                    doc.get(headers.get(i)).set(j, new MetricValue(0.));
                }
            }
        }
        writeToCSV(doc, output);
    }

    private void writeToCSVFilenameHeaders(Map<String, ArrayList<MetricValue>> doc, String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            List<String> headers = new ArrayList<String>(doc.keySet());
            List<String> v = new ArrayList<>();
            for (int k = 0; k < doc.get(headers.get(0)).size(); ++k) {
                v.add(doc.get(headers.get(1)).get(k).toString());
            }
            bw.write(StringUtils.join(v, ","));
            bw.write("\n");
            for (int k = 2; k < headers.size(); ++k) {
                v = new ArrayList<>();
                for (int i = 0; i < doc.get(headers.get(0)).size(); ++i) {
                    v.add(doc.get(headers.get(k)).get(i).toString());
                }
                bw.write(StringUtils.join(v, ","));
                if (k != headers.size() - 1) {
                    bw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EssayMeasurementsPCA fe = new EssayMeasurementsPCA();

        Map<String, ArrayList<MetricValue>> doc = fe.parseCSV("resources/in/tasa(2+)_demo/tasa2+_no_readability.csv/");
        doc = fe.removeLocalMetrics(doc);
        fe.writeToCSV(doc, "resources/in/tasa(2+)_demo/stage1.csv/");
        doc = fe.removeOutliers(doc, "resources/in/tasa(2+)_demo/stage1.csv");
        fe.writeToCSV(doc, "resources/in/tasa(2+)_demo/stage1.csv");
        doc = fe.pruneSkewnessKurtosis(doc, "resources/in/tasa(2+)_demo/stage1.csv");
        fe.writeToCSV(doc, "resources/in/tasa(2+)_demo/stage12.csv");
        doc = fe.removeCorrelatedMetrics(doc, "resources/in/tasa(2+)_demo/stage12.csv");
        fe.writeToCSV(doc, "resources/in/tasa(2+)_demo/stage123.csv");
        fe.PCAPruning(doc, "resources/in/tasa(2+)_demo/stage123.csv",
                "resources/in/tasa(2+)_demo/loadings.csv",
                "resources/in/tasa(2+)_demo/zscores",
                "resources/in/tasa(2+)_demo");
        fe.computeScores("resources/in/tasa(2+)_demo/stage123.csv",
                "resources/in/tasa(2+)_demo");
    }
}

