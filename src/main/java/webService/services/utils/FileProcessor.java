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
package webService.services.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import webService.result.ResultFile;

public class FileProcessor {
	
	private static final Logger LOGGER = Logger.getLogger("");
	
	private static FileProcessor instance = null;
	protected FileProcessor() {
		
	}
	public static FileProcessor getInstance() {
		if (instance == null) {
			instance = new FileProcessor();
		}
		return instance;
	}
	
	public ResultFile saveFile(Part submitedFile) {
		File targetFile = new File("tmp/" + System.currentTimeMillis() + '_' + submitedFile.getName());
		try {
			FileUtils.copyInputStreamToFile(submitedFile.getInputStream(), targetFile);
		} catch (IOException e) {
			LOGGER.severe("Can't save uploaded file!");
		}
		return new ResultFile(targetFile.getName(), targetFile.length());
	}
	
	public ResultFile saveFile(Part submitedFile, File folderPath) {
		File targetFile = new File(folderPath +"/" + submitedFile.getName());
		try {
			FileUtils.copyInputStreamToFile(submitedFile.getInputStream(), targetFile);
		} catch (IOException e) {
			LOGGER.severe("Can't save uploaded files!");
		}
		return new ResultFile(targetFile.getName(), targetFile.length());
	}
	
	public File createFolderForVCoPFiles(){
		File newFolder = new File("vCoP_"+System.currentTimeMillis());
		newFolder.mkdir();
		return newFolder;
	}

}
