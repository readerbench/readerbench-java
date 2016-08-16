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

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;


import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openide.loaders.FileEntry.Folder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webService.ReaderBenchServer;

import org.apache.commons.io.FileUtils;

public class FileProcessor {
	
	private static Logger logger = Logger.getLogger(FileProcessor.class);
	
	private static FileProcessor instance = null;
	protected FileProcessor() {
		
	}
	public static FileProcessor getInstance() {
		if (instance == null) {
			instance = new FileProcessor();
		}
		return instance;
	}
	
	public String saveFile(Part submitedFile) {
		File targetFile = new File("tmp/" + System.currentTimeMillis() + '_' + submitedFile.getSubmittedFileName());
		try {
			FileUtils.copyInputStreamToFile(submitedFile.getInputStream(), targetFile);
		} catch (IOException e) {
			logger.error("Can't save uploaded file!");
		}
		return targetFile.getName();
	}
	
	public String saveFile(Part submitedFile, File folderPath) {
		File targetFile = new File(folderPath +"/" + submitedFile.getSubmittedFileName());
		try {
			FileUtils.copyInputStreamToFile(submitedFile.getInputStream(), targetFile);
		} catch (IOException e) {
			logger.error("Can't save uploaded files!");
		}
		return targetFile.getName();
	}
	
	public File createFolderForVCoPFiles(){
		File newFolder = new File("vCoP_"+System.currentTimeMillis());
		newFolder.mkdir();
		return newFolder;
	}

}
