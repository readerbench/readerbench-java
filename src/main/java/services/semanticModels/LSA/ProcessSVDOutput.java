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
package services.semanticModels.LSA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;


import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

import services.commons.Formatting;
import services.commons.ObjectManipulation;
import data.Lang;
import org.openide.util.Exceptions;

public class ProcessSVDOutput extends LSA {

    private static Logger logger = Logger.getLogger("");

    private double[][] readMatrix(String path) {
        // determine all files in input folder
        if (!new File(path).isDirectory()) {
            return null;
        }
        File[] files = new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.equals("_SUCCESS")) {
                    return false;
                }
                if (name.contains(".")) {
                    return false;
                }
                return true;
            }
        });

        // read all files
        Map<Integer, DenseVector> vectors = new TreeMap<Integer, DenseVector>();
        int idMax = 0;
        int kMax = 0;
        for (File f : files) {
            logger.info("Reading input file - " + f.getName());
            final Configuration conf = new Configuration();
            SequenceFile.Reader reader;
            try {
                reader = new SequenceFile.Reader(conf, Reader.file(new Path(f.getPath())));

                IntWritable key = new IntWritable();
                VectorWritable vec = new VectorWritable();

                while (reader.next(key, vec)) {
                    // System.out.println("key " + key);
                    DenseVector vect = (DenseVector) vec.get();
                    vectors.put(key.get(), vect);
                    idMax = Math.max(idMax, key.get());
                    kMax = Math.max(kMax, vect.size());
                }
                reader.close();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < idMax; i++) {
            if (!vectors.keySet().contains(i)) {
                logger.severe("Missing information for element " + i + "!");
            }
        }

        logger.info("The final matrix has " + (idMax + 1) + " rows and " + kMax + " columns");

        double[][] matrix = new double[idMax + 1][kMax];

        for (Entry<Integer, DenseVector> entry : vectors.entrySet()) {
            int key = entry.getKey();
            Iterator<Vector.Element> iter = entry.getValue().iterateNonZero();

            while (iter.hasNext()) {
                Vector.Element element = iter.next();
                matrix[key][element.index()] = entry.getValue().getQuick(element.index());
            }
        }

        return matrix;
    }

    private double[] readvector(String path) {
        final Configuration conf = new Configuration();
        double[] vector = null;
        int no = 0;
        try {
            final SequenceFile.Reader reader = new SequenceFile.Reader(conf, Reader.file(new Path(path)));
            IntWritable key = new IntWritable();
            VectorWritable vec = new VectorWritable();

            while (reader.next(key, vec)) {
                if (no > 1) {
                    logger.severe("Input matrix contains too many rows!");
                }
                DenseVector vect = (DenseVector) vec.get();
                vector = new double[vect.size()];
                Iterator<Vector.Element> iter = vect.iterateNonZero();

                while (iter.hasNext()) {
                    Vector.Element element = iter.next();
                    vector[element.index()] = vect.getQuick(element.index());
                }
                no++;
            }
            reader.close();
        } catch (IllegalArgumentException | IOException e) {
            Exceptions.printStackTrace(e);
        }
        return vector;
    }

    public void performPostProcessing(String path, Lang language, boolean halfSigma)
            throws FileNotFoundException, IOException {
        ProcessSVDOutput lsa = new ProcessSVDOutput();
        lsa.setLanguage(language);
        lsa.loadWordList(path);
        lsa.loadIdf(path);

        // determine Uk
        logger.info("Building Uk matrix");
        if (halfSigma) {
            lsa.setUk(readMatrix(path + "/svd_out/UHalfSigma"));
        } else {
            lsa.setUk(readMatrix(path + "/svd_out/U"));
        }
        ObjectManipulation.saveObject(lsa.getUk(), path + "/U.ser");

        // determine VTk
        logger.info("Building Vtk matrix");
        if (halfSigma) {
            lsa.setVtk(readMatrix(path + "/svd_out/VHalfSigma"));
        } else {
            lsa.setVtk(readMatrix(path + "/svd_out/V"));
        }
        ObjectManipulation.saveObject(lsa.getVtk(), path + "/Vt.ser");

        // determine Sigma
        logger.info("Building Sk vector");
        lsa.setSk(readvector(path + "/svd_out/Sigma/svalues.seq"));

        for (int i = 0; i < lsa.getSk().length; i++) {
            if (lsa.getSk()[i] < 1) {
                logger.info("First index for Sigma_k <1: " + i + " (" + Formatting.formatNumber(lsa.getSk()[i]) + ")");
            }
        }
        logger.info("Last entry in Sigma_k: " + Formatting.formatNumber(lsa.getSk()[lsa.getSk().length - 1]));

        ObjectManipulation.saveObject(lsa.getSk(), path + "/S.ser");

        logger.info("Finished all computations");
    }

    public static void main(String[] args) {
        
        try {
            // post-process
            ProcessSVDOutput processing = new ProcessSVDOutput();
            processing.performPostProcessing("resources/config/ES/LSA/Jose Antonio", Lang.es, true);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            logger.severe("Error during learning process");
        }
    }
}
