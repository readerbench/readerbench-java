package services.semanticModels.LSA;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.BasicConfigurator;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.decompositions.SSVD;
import org.apache.mahout.math.drm.DistributedEngine$class;
import org.apache.mahout.math.drm.DrmLike;
import org.openide.util.Exceptions;
import scala.Tuple3;

public class RunSVD {

    /*
	 * public TestMahout() { Matrix A = lowRankMatrixInMemory(20, 20);
	 * SequentialBigSvd s = new SequentialBigSvd(A, 6);
	 * SingularValueDecomposition svd = new SingularValueDecomposition(A);
	 * Matrix v1 = svd.getV().viewPart(0, 20, 0, 3).assign(Functions.ABS);
	 * Matrix v2 = s.getV().viewPart(0, 20, 0, 3).assign(Functions.ABS); }
     */
    public static void main(String[] args) throws IOException {
//        BasicConfigurator.configure();
//        runSSVDOnSparseVectors("in/lastFm/lastFM", "in/lastFm/out", 300, 200,
//                30000, 3, 2, true);
        double[][] m = {{1., 1., 1.}, {1., 1., 1.}, {1., 1., 1.}};
        Tuple3<Matrix, Matrix, Vector> result = SSVD.ssvd(new DenseMatrix(m), 1, 1, 3);
        System.out.println(result._1());
        System.out.println(result._2());
        System.out.println(result._3());
    }

    public static void runSSVDOnSparseVectors(String inputPath,
            String outputPath, int rank, int oversampling, int blocks,
            int reduceTasks, int powerIterations, boolean halfSigma)
            throws IOException {
        new File(outputPath).mkdirs();
        Matrix m = MatrixWritable.readMatrix(new DataInputStream(new FileInputStream(inputPath)));
        
        Tuple3<Matrix, Matrix, Vector> result = SSVD.ssvd(m, rank, oversampling, powerIterations);
        MatrixWritable.writeMatrix(
                new DataOutputStream(new FileOutputStream(outputPath + "/U.ser")),
                result._1());
        MatrixWritable.writeMatrix(
                new DataOutputStream(new FileOutputStream(outputPath + "/V.ser")),
                result._2());
        VectorWritable.writeVector(
                new DataOutputStream(new FileOutputStream(outputPath + "/s.ser")),
                result._3());
    }
}
