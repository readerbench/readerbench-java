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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.mahout.math.DiagonalMatrix;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.decompositions.SSVD;

import scala.Tuple3;
import services.commons.ObjectManipulation;

public class RunSVD {

    public static void runSSVDOnSparseVectors(String inputPath, String outputPath, int rank, int oversampling, int powerIterations)
            throws IOException {
        Matrix m = MatrixWritable.readMatrix(new DataInputStream(new FileInputStream(inputPath)));

        //perform SVD
        Tuple3<Matrix, Matrix, Vector> result = SSVD.ssvd(m, rank, oversampling, powerIterations);
        Vector halfsigma = result._3();

        for (int i = 0; i < halfsigma.size(); i++) {
            halfsigma.set(i, Math.sqrt(halfsigma.get(i)));
        }

        //compute U * S^1/2
        Matrix U = result._1().times(new DiagonalMatrix(halfsigma));

        double[][] u = new double[U.numRows()][rank];
        for (int i = 0; i < U.numRows(); i++) {
            for (int j = 0; j < Math.min(U.numCols(), rank); j++) {
                u[i][j] = U.get(i, j);
            }
        }

        //save U
        ObjectManipulation.saveObject(u, outputPath + "/U.ser");

        //remove input matrix
        new File(inputPath).delete();
    }
}
