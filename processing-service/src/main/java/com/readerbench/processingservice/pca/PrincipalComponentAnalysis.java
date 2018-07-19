package com.readerbench.processingservice.pca;

import com.readerbench.coreservices.commons.VectorAlgebra;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A PCA implementation that is meant to be functionally equivalent to the `prcomp` function of the R language
 * <p>
 * This is meant for computing principal components, loadings and variances for a dataset. The dataset should contain
 * observations on rows corresponding to variables on columns.
 *
 * @author Botarleanu Robert
 */
public class PrincipalComponentAnalysis {

    private int variables;
    private int observations;

    private List<String> variableNames;
    private double[][] data;
    private double[] means;
    private double[] stdevs;
    private SingularValueDecomposition svd;
    private double[] pcStdevs;

    private double[] pcVariances;
    private double[][] pcRotation;
    private double[][] loadings;
    private int nComponents;

    public PrincipalComponentAnalysis(Map<String, ArrayList<EssayMeasurementsPCA.MetricValue>> measurements) {
        variableNames = new ArrayList<>();
        measurements.keySet().iterator().forEachRemaining(variableNames::add);
        variableNames.remove(0);
        variableNames.remove(0);

        variables = variableNames.size();
        observations = measurements.get(variableNames.get(0)).size();
        data = new double[observations][];
        for (int i = 0; i < observations; ++i) {
            data[i] = new double[variables];
            for (int j = 0; j < variables; ++j) {
                data[i][j] = measurements.get(variableNames.get(j)).get(i).value;
            }
        }
    }

    public PrincipalComponentAnalysis(double[][] measurements) {
        variables = measurements.length;
        observations = measurements[0].length;
        data = new double[observations][];
        for (int i = 0; i < observations; ++i) {
            data[i] = new double[variables];
            for (int j = 0; j < variables; ++j) {
                data[i][j] = measurements[i][j];
            }
        }
    }


    private void scaleData() {
        stdevs = new double[variables];
        means = new double[variables];

        for (int j = 0; j < variables; ++j) {
            double[] columnValues = new double[observations];
            for (int i = 0; i < observations; ++i)
                columnValues[i] = data[i][j];
            stdevs[j] = VectorAlgebra.stdev(columnValues);
            means[j] = VectorAlgebra.mean(columnValues);
            for (int i = 0; i < observations; ++i)
                data[i][j] = (data[i][j] - means[j]) / stdevs[j];
        }
    }

    public void pca(boolean scale) {
        if (scale)
            scaleData();
        svd = new SingularValueDecomposition(new Array2DRowRealMatrix(data));
        double[] sv = svd.getSingularValues();

        //  s$d <- s$d / sqrt(max(1, nrow(x) - 1))
        pcStdevs = new double[variables];
        for (int i = 0; i < variables; ++i)
            pcStdevs[i] = sv[i] / Math.sqrt(Math.max(1, observations)); // we don't need to subtract 1

        // variances <- pca$sdev ^ 2 / sum(pca $ sdev ^ 2)
        pcVariances = new double[variables];
        double s = 0;
        for (int i = 0; i < variables; ++i)
            s += pcStdevs[i] * pcStdevs[i];
        for (int i = 0; i < variables; ++i)
            pcVariances[i] = pcStdevs[i] * pcStdevs[i] / s;

        // V holds the eigenvectors
        pcRotation = new double[variables][variables];
        for (int i = 0; i < variables; ++i)
            for (int j = 0; j < variables; ++j)
                pcRotation[i][j] = svd.getV().getEntry(i, j);

    }

    private void writeToFile(double[] v, String filename) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.println("\"\",\"x\"");
            for (int i = 0; i < variables; ++i)
                writer.println("\"" + variableNames.get(i) + "\"," + v[i]);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void computeComponentLoadings(int k) {
        nComponents = k;
        // rawLoadings <- pca$rotation[, 1:k] %*% diag(pca$stdev, k, k)
        double[][] rawLoadings = new double[variables][k];
        for (int i = 0; i < variables; ++i) {
            for (int j = 0; j < k; ++j) {
                rawLoadings[i][j] = 0;
                for (int p = 0; p < k; ++p)
                    rawLoadings[i][j] += pcRotation[i][p] * ((p == j) ? pcStdevs[p] : 0.0);
            }
        }

        // rotatedLoadings <- varimax(rawLoadings)$loadings
        loadings = varimaxLoadings(rawLoadings, k);
    }

    public double[][] varimaxLoadings(double[][] dataMatrix, int k) {
        // https://github.com/SurajGupta/r-source/blob/master/src/library/stats/R/factanal.R
        // varimax implementation with normalization and eps=1e-5
        final double eps = 1e-5;

        double[] sc = new double[variables];
        for (int i = 0; i < variables; ++i) {
            sc[i] = 0;
            for (int j = 0; j < k; ++j)
                sc[i] += dataMatrix[i][j] * dataMatrix[i][j];
            sc[i] = Math.sqrt(sc[i]);
        }
        for (int i = 0; i < variables; ++i)
            for (int j = 0; j < k; ++j)
                dataMatrix[i][j] /= sc[i];

        int p = variables;
        int nc = k;
        double d = 0;
        double dpast = 0;
        RealMatrix TT = new Array2DRowRealMatrix(k, k);
        for (int i = 0; i < k; ++i)
            for (int j = 0; j < k; ++j)
                TT.setEntry(i, j, (i == j) ? 1 : 0);

        RealMatrix x = new Array2DRowRealMatrix(dataMatrix);
        RealMatrix z;
        for (int iter = 0; iter < 1000; ++iter) {
            z = x.multiply(TT);
            double[] ones = new double[p];
            for (int i = 0; i < p; ++i) ones[i] = 1;
            RealMatrix z2 = new Array2DRowRealMatrix(z.getRowDimension(), z.getColumnDimension());
            RealMatrix z3 = new Array2DRowRealMatrix(z.getRowDimension(), z.getColumnDimension());
            for (int i = 0; i < z.getRowDimension(); ++i) {
                for (int j = 0; j < z.getColumnDimension(); ++j) {
                    z2.setEntry(i, j, z.getEntry(i, j) * z.getEntry(i, j));
                    z3.setEntry(i, j, z.getEntry(i, j) * z.getEntry(i, j) * z.getEntry(i, j));
                }
            }

            RealMatrix aux = new Array2DRowRealMatrix(ones).transpose().multiply(z2);
            RealMatrix aux2 = new Array2DRowRealMatrix(k, k);
            for (int i = 0; i < k; ++i)
                for (int j = 0; j < k; ++j)
                    aux2.setEntry(i, j, (i == j) ? aux.getEntry(0, i) : 0);
            RealMatrix b = x.transpose().multiply(z3.subtract(z.multiply(aux2).scalarMultiply(1. / p)));
            SingularValueDecomposition bSvd = new SingularValueDecomposition(b);
            TT = bSvd.getU().multiply(bSvd.getVT());
            dpast = d;
            d = 0;
            double[][] svdD = bSvd.getS().getData();
            for (int i = 0; i < svdD.length; ++i)
                for (int j = 0; j < svdD[i].length; ++j)
                    d += svdD[i][j];

            if (d < dpast * (1 + eps))
                break;
        }

        z = x.multiply(TT);
        for (int i = 0; i < z.getRowDimension(); ++i)
            for (int j = 0; j < z.getColumnDimension(); ++j)
                z.setEntry(i, j, z.getEntry(i, j) * sc[i]);

        return z.getData();
    }

    public int getVariables() {
        return variables;
    }

    public int getObservations() {
        return observations;
    }

    public void writeMeansToFile(String filename) {
        writeToFile(means, filename);
    }

    public void writeStdevsToFile(String filename) {
        writeToFile(stdevs, filename);
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public double[][] getData() {
        return data;
    }

    public double[] getMeans() {
        return means;
    }

    public double[] getStdevs() {
        return stdevs;
    }

    public SingularValueDecomposition getSvd() {
        return svd;
    }

    public double[] getPcStdevs() {
        return pcStdevs;
    }

    public double[] getPcVariances() {
        return pcVariances;
    }

    public double[][] getPcRotation() {
        return pcRotation;
    }

    public double[][] getLoadings() {
        return loadings;
    }

    public void writeLoadingsToFile(String fullLoadingsPath) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fullLoadingsPath, "UTF-8");
            // Write header
            StringBuilder line = new StringBuilder();
            line.append("\"\",");
            for (int i = 1; i <= nComponents; ++i)
                line.append("V" + i + ",");
            writer.println(line);

            for (int i = 0; i < variables; ++i) {
                line.delete(0, line.length());
                line.append(variableNames.get(i));
                for (int j = 0; j < nComponents; ++j)
                    line.append("," + loadings[i][j]);
                writer.println(line);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
