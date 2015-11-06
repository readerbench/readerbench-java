package services.semanticModels.LSA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.log4j.BasicConfigurator;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

public class ReadInputMatrix {

	public static void readSVD(String in, String out) {
		try {
			final Configuration conf = new Configuration();
			final SequenceFile.Reader reader = new SequenceFile.Reader(conf,
					Reader.file(new Path(in)));
			BufferedWriter br = new BufferedWriter(new FileWriter(out));
			IntWritable key = new IntWritable();
			VectorWritable vec = new VectorWritable();

			while (reader.next(key, vec)) {
				// System.out.println("key " + key);
				SequentialAccessSparseVector vect = (SequentialAccessSparseVector) vec
						.get();
				System.out.println("key " + key + " value: " + vect);
				Iterator<Vector.Element> iter = vect.iterateNonZero();

				while (iter.hasNext()) {
					Vector.Element element = iter.next();
					br.write(key + "," + element.index() + ","
							+ vect.getQuick(element.index()) + "\n");
				}
			}

			reader.close();
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 *
	 * @param args
	 *            [0] - input svd file
	 * @param args
	 *            [1] - output txt
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		readSVD("resources/config/LSA/lak_en/matrix.svd", "resources/config/LSA/lak_en/matrix.txt");
	}
}