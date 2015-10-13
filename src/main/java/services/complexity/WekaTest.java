package services.complexity;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class WekaTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		WekaPrediction weka =new WekaPrediction();
		//weka.makePrediction("D:/Disertatie/TEst/measurements.arff", "J48",10);
		//System.out.println(weka.makePrediction("D:/Disertatie/TEst/measurements.arff", "J48",10));
		try {
			//FileUtils.writeStringToFile(new File("D:/Disertatie/TEst/measurements.txt"), weka.makePrediction2("D:/Disertatie/TEst/measurements.arff","D:/Disertatie/TestImages/measurements.arff", "J48"));
			FileUtils.writeStringToFile(new File("D:/Disertatie/TEst/measurements.txt"), weka.makePrediction("D:/Disertatie/TEst/measurements.arff", "PART", 10));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
