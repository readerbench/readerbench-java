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
package com.readerbench.services.converters;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ConversationReply {
	String participantName;
	String genid;
	String refid;
	String time;
	String message;
}

public class XmlToXlsConversationConverter {
	private String outDir;
	private String inDir;
	
	public XmlToXlsConversationConverter (String outDir, String inDir) {
		this.outDir = outDir;
		this.inDir = inDir;
	}
	public void convert() {
		this.convertFrom(this.outDir, this.inDir);
	}
	private void convertFrom(String currentOutDir, String currentInDir) {
		File outFile = new File(currentOutDir);
		if(!outFile.exists()){
			outFile.mkdirs();
		}
		File inFile = new File(currentInDir);
		File children[] = inFile.listFiles();
		for(File childFile : children) {
			if(childFile.isDirectory()) {
				String subdir = File.separator + childFile.getName();
				this.convertFrom(currentOutDir + subdir, currentInDir + subdir);
				this.moveCommunityFile(currentInDir + subdir, currentOutDir + subdir, childFile.getName());
			}
			else if(childFile.getName().endsWith(".xml") && !childFile.getName().startsWith("checkpoint")) {
				String subfile = File.separator + childFile.getName();
				this.tryToConvert(currentInDir + subfile, currentOutDir + subfile);
			}
		}
	}
	private void tryToConvert(String inFileName, String outFileName) {
		try {	
	         File inputFile = new File(inFileName);
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         NodeList turnList = doc.getElementsByTagName("Turn");
	         
	         List<ConversationReply> replyList = new ArrayList<ConversationReply>();
	         for(int nodeIndex = 0; nodeIndex < turnList.getLength(); nodeIndex ++) {
	        	 Node node = turnList.item(nodeIndex);
	        	 String nickname = this.getAttrValue(node.getAttributes(), "nickname");
	        	 Node utteranceNode = node.getFirstChild();
	        	 if(utteranceNode != null) {
	        		 NamedNodeMap attrs = utteranceNode.getAttributes();
	        		 String genid = this.getAttrValue(attrs, "genid");
	        		 String refid = this.getAttrValue(attrs, "refid");
	        		 String time = this.getAttrValue(attrs, "time");
	        		 String message = utteranceNode.getTextContent();
	        		 
	        		 ConversationReply convReply = new ConversationReply();
	        		 convReply.genid = genid;
	        		 convReply.message = message;
	        		 convReply.participantName = nickname;
	        		 convReply.refid = refid;
	        		 convReply.time = time;
	        		 
	        		 replyList.add(convReply);
	        	 }
	         }
	         this.exportConversationReplyList(replyList, outFileName);
	         
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getAttrValue(NamedNodeMap attrs, String attrName) {
		if(attrs == null) {
			return "";
		}
		Node attr = attrs.getNamedItem(attrName);
		if(attr != null) {
			return attr.getTextContent();
		}
		return "";
	}
	private void moveCommunityFile(String sourceDirName, String destDirName, String fileName) {
		File destDir = new File(destDirName);
		destDir.mkdirs();
		try {
			File sourceFile = new File(sourceDirName + File.separator + fileName + ".csv");
			if(!sourceFile.exists()) {
				return;
			}
			File destFile = new File(destDirName + File.separator + fileName + ".csv");
			this.copyFileUsingStream(sourceFile, destFile);
			System.out.println(sourceFile.getAbsolutePath());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
	
	private void exportConversationReplyList(List<ConversationReply> convList, String outFileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream(outFileName.replace(".xml", "_IM.xls"));
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet("Chat");
			
			HSSFRow row1 = worksheet.createRow((short) 0);
			row1.createCell((short) 0).setCellValue("ID");
			row1.createCell((short) 1).setCellValue("Reference ID");
			row1.createCell((short) 2).setCellValue("Name");
			row1.createCell((short) 3).setCellValue("Time");
			row1.createCell((short) 4).setCellValue("Text");
			
			for(int convIndex = 0; convIndex < convList.size(); convIndex ++) {
				ConversationReply conv = convList.get(convIndex);
				HSSFRow row = worksheet.createRow((short) convIndex + 1);
				row.createCell((short) 0).setCellValue(conv.genid);
				row.createCell((short) 1).setCellValue(conv.refid);
				row.createCell((short) 2).setCellValue(conv.participantName);
				row.createCell((short) 3).setCellValue(conv.time);
				row.createCell((short) 4).setCellValue(conv.message.substring(0, Math.min(conv.message.length(), 32767)));
			}
			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String outDir = "C:\\Users\\Ionut\\Downloads\\blogs_nic_out";
		String inDir = "C:\\Users\\Ionut\\Downloads\\blogs_nic";
		
		XmlToXlsConversationConverter converter = new XmlToXlsConversationConverter(outDir, inDir);
		converter.convert();
	}
}
