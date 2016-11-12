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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.decompositions.SSVD;

import scala.Tuple3;

public class RunSVD {

    public static void runSSVDOnSparseVectors(String inputPath, String outputPath, int rank, int oversampling, int powerIterations)
            throws IOException {
        new File(outputPath).mkdirs();
        Matrix m = MatrixWritable.readMatrix(new DataInputStream(new FileInputStream(inputPath)));

        Tuple3<Matrix, Matrix, Vector> result = SSVD.ssvd(m, rank, oversampling, powerIterations);
        MatrixWritable.writeMatrix(
                new DataOutputStream(new FileOutputStream(outputPath + "/U.ser")),
                result._1());
        MatrixWritable.writeMatrix(
                new DataOutputStream(new FileOutputStream(outputPath + "/Vt.ser")),
                result._2());
        VectorWritable.writeVector(
                new DataOutputStream(new FileOutputStream(outputPath + "/S.ser")),
                result._3());
    }
}
