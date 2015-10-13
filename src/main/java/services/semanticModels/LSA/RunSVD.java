package services.semanticModels.LSA;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.BasicConfigurator;
import org.apache.mahout.math.hadoop.stochasticsvd.SSVDSolver;

public class RunSVD {

	/*
	 * public TestMahout() { Matrix A = lowRankMatrixInMemory(20, 20);
	 * SequentialBigSvd s = new SequentialBigSvd(A, 6);
	 * SingularValueDecomposition svd = new SingularValueDecomposition(A);
	 * Matrix v1 = svd.getV().viewPart(0, 20, 0, 3).assign(Functions.ABS);
	 * Matrix v2 = s.getV().viewPart(0, 20, 0, 3).assign(Functions.ABS); }
	 */

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();
		runSSVDOnSparseVectors("in/lastFm/lastFM", "in/lastFm/out", 300, 200,
				30000, 3, 2, true);
	}

	public static void runSSVDOnSparseVectors(String inputPath,
			String outputPath, int rank, int oversampling, int blocks,
			int reduceTasks, int powerIterations, boolean halfSigma)
			throws IOException {
		Configuration conf = new Configuration();
		// get number of reduce tasks from config?
		SSVDSolver solver = new SSVDSolver(conf, new Path[] { new Path(
				inputPath) }, new Path(outputPath), blocks, rank, oversampling,
				reduceTasks);
		solver.setQ(powerIterations);
		if (halfSigma) {
			solver.setcUHalfSigma(true);
			solver.setcVHalfSigma(true);
		}
		solver.run();
	}

}
