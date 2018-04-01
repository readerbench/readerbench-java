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
package runtime.cscl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;



public class CSCLDeleteChats {
	
	public static Logger logger = Logger.getLogger("");

	private static String conversationsPath = "resources/in/corpus_v2/";	
	
	private static final Set<String> ALLOWED_FILES = new HashSet<String>(Arrays.asList(
		     new String[] {
		    		 "cirstea_351c2",
		    		 "Curteanu_m3",
		    		 "tatar_351C2",
		    		 "Boldisor_353C3",
		    		 "Duguleana352C1",
		    		 "Stefanescu_m7",
		    		 "CarcuDobrin_Grupa38",
		    		 "Dumitru_353C1",
		    		 "Murarasu_4",
		    		 "truca_351c2",
		    		 "BarsanIulia_Fil1251e",
		    		 "Jurcuta_SPBA",
		    		 "Stan_351C4",
		    		 "salageanu_1251e",
		    		 "Morosan_grupa21",
		    		 "ToaderRadulescu_352C2",
		    		 "Ursachi_351C4",
		    		 "Constantinescu_4",
		    		 "ionescu_351C1",
		    		 "Ionescu_354C2",
		    		 "Iacob_352C3",
		    		 "giusca_grupa35",
		    		 "Dan 1251E",
		    		 "CRACAN_353C2",
		    		 "Matei_352C1",
		    		 "CalavrezoDan_352C2",
		    		 "Stoenescu_352C2",
		    		 "Nunvailer_Grupa27",
		    		 "Ivanes_352C1",
		    		 "Cocanu_353C3",
		    		 "olaru_351C4",
		    		 "dochiu_352C1",
		    		 "Floarea_353C3",
		    		 "Nistor_SPBA",
		    		 "Popescu_352c2",
		    		 "Ionescu_351C2",
		    		 "Boldea_351C3",
		    		 "Voda_1251E",
		    		 "milescu_352C3",
		    		 "Diaconu_352C2",
		    		 "Stoica_354C3",
		    		 "Badea_352C1",
		    		 "Radu_353C1",
		    		 "bardac_352C3",
		    		 "Papadima_351C1",
		    		 "Petrescu_352C1",
		    		 "Rusu_FILS1251E",
		    		 "gulie_352c2",
		    		 "Zachia-Zlatea_Irina_FILS1251E",
		    		 "Cazacu_352C2",
		    		 "Bizadea_353C2",
		    		 "Vasile_351C4",
		    		 "Raianu_351C3",
		    		 "crisan_351C1",
		    		 "radu_354C2",
		     }
		));
	
	public static int deletedFiles = 0;
	public static int keptFiles = 0;
	
	public static void main(String[] args) {
		
		try {
			Files.walk(Paths.get(CSCLDeleteChats.conversationsPath)).forEach(filePath -> {

				// remove _in.ext or _out.ext from the file
			    int endIndex = filePath.getFileName().toString().lastIndexOf("_");
			    // remove one more section for _in_adv.csv files
			    if (endIndex != -1 && filePath.getFileName().toString().substring(0, endIndex).contains("_in") ) {
			    	endIndex = filePath.getFileName().toString().substring(0, endIndex).lastIndexOf("_");
			    	//logger.info("File " + filePath.getFileName().toString() + " does contain '_in' after process. New index: " + endIndex);
				}
			    
			    // if string does not contain _, remove it (we identified all these files as duplicates)
			    if (endIndex == -1) {
			    	logger.info("File " + filePath.getFileName().toString() + " does not contain '_' and will be deleted.");
			    	try {
						Files.delete(filePath);
					} catch (Exception e) {
						logger.severe("File " + filePath.getFileName().toString() + " could not be deleted.");
						e.printStackTrace();
					}
					deletedFiles++;
			    }
			    else {
			    	// if file is not in the allowed files array, the delete it
			    	//logger.info("Looking for file " + filePath.getFileName().toString() + " in the array. Short file: " + filePath.getFileName().toString().substring(0, endIndex));
			    	if (!ALLOWED_FILES.contains(filePath.getFileName().toString().substring(0, endIndex))) {
						logger.info("File " + filePath.getFileName().toString() + " will be deleted.");
						try {
							Files.delete(filePath);
						} catch (Exception e) {
							logger.severe("File " + filePath.getFileName().toString() + " could not be deleted.");
							e.printStackTrace();
						}
						deletedFiles++;
					}
					else {
						logger.info("File " + filePath.getFileName().toString() + " will be kept.");
						keptFiles++;
					}
			    }
			});
			
			logger.info("A total of " + deletedFiles + " files will be deleted.");
			logger.info("A total of " + keptFiles + " files will be kept.");
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
