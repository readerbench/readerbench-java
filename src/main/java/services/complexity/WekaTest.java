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
