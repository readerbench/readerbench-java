package runtime.cscl;

import data.cscl.Conversation;
import data.cscl.Utterance;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import services.semanticModels.WordNet.SimilarityType;
import webService.ReaderBenchServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument.SaveType;
import data.Block;
import data.Lang;

public class CSCLContributionSimilarities {

	public Logger logger = Logger.getLogger(CSCLContributionSimilarities.class);
	private String path;

	private String pathToLSA;
	private String pathToLDA;
	private Lang lang;
	private boolean usePOSTagging = false;
	private boolean computeDialogism = false;
	private double threshold = 0.3;
	private int windowSize = 20;
	
	public CSCLContributionSimilarities(String path, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean computeDialogism, double threshold, int windowSize) {
		this.path = path;
		this.pathToLSA = pathToLSA;
		this.pathToLDA = pathToLDA;
		this.lang = lang;
		this.usePOSTagging = usePOSTagging;
		this.computeDialogism = computeDialogism;
		this.threshold = threshold;
		this.windowSize = windowSize;
	}
	
	public void process() {
		StringBuilder capTabel = new StringBuilder();
		capTabel.append("chat,");
		capTabel.append("utt_id,");
		for (int i=1; i<=20; i++) {
			capTabel.append("d" + i + ",");
		}
		capTabel.append("utt_text,"); // contribution text
		capTabel.append("ref_id,"); // reference id
		capTabel.append("ref_text,"); // reference text
		capTabel.append("max_sim,"); // max sim
		capTabel.append("ref_max_sim,"); // id of max sim utt
		capTabel.append("max_sim_norm,"); // max sim normalized
		capTabel.append("mihalcea_sim,"); // Mihalcea's similarity
		capTabel.append('\n');
		
		logger.info("Starting conversation processing...");
		try {
			File fileLSA = new File(path + "similarity_LSA.sim.csv");
			File fileLDA = new File(path + "similarity_LDA.sim.csv");
			File fileLeacock = new File(path + "similarity_LEACOCK_CHODOROW.sim.csv");
			File fileWuPalmer = new File(path + "similarity_WU_PALMER.sim.csv");
			File filePathSim = new File(path + "similarity_PATH_SIM.sim.csv");
			
			try {
				FileUtils.writeStringToFile(fileLSA, capTabel.toString(), "UTF-8");
				FileUtils.writeStringToFile(fileLDA, capTabel.toString(), "UTF-8");
				FileUtils.writeStringToFile(fileLeacock, capTabel.toString(), "UTF-8");
				FileUtils.writeStringToFile(fileWuPalmer, capTabel.toString(), "UTF-8");
				FileUtils.writeStringToFile(filePathSim, capTabel.toString(), "UTF-8");
			} catch (Exception e) {
				logger.info("Exception: " + e.getMessage());
				e.printStackTrace();
			}
			
			Files.walk(Paths.get(path)).forEach(filePath -> {
				String filePathString = filePath.toString();
				String fileExtension = FilenameUtils.getExtension(filePathString);
				if (fileExtension.compareTo("xml") == 0) {
					
					System.out.println("Processing chat " + filePath.getFileName());
					Conversation c = Conversation.load(filePathString, pathToLSA, pathToLDA, lang, usePOSTagging, true);
					c.computeAll(computeDialogism, null, null, SaveType.NONE);
					
					Utterance firstUtt = null, secondUtt = null;
					for (int i = 1; i < c.getBlocks().size(); i++) {
						firstUtt = (Utterance) c.getBlocks().get(i);
						if (firstUtt != null) {
							StringBuilder rowLSA = new StringBuilder();
							StringBuilder rowLDA = new StringBuilder();
							StringBuilder rowLeacock = new StringBuilder();
							StringBuilder rowWuPalmer = new StringBuilder();
							StringBuilder rowPathSim = new StringBuilder();
							
							rowLSA.append(filePath.getFileName() + ",");
							rowLDA.append(filePath.getFileName() + ",");
							rowLeacock.append(filePath.getFileName() + ",");
							rowWuPalmer.append(filePath.getFileName() + ",");
							rowPathSim.append(filePath.getFileName() + ",");
							
							rowLSA.append(firstUtt.getIndex() + ",");
							rowLDA.append(firstUtt.getIndex() + ",");
							rowLeacock.append(firstUtt.getIndex() + ",");
							rowWuPalmer.append(firstUtt.getIndex() + ",");
							rowPathSim.append(firstUtt.getIndex() + ",");
							
							int k = 0;
							double maxLSA = -32000;
							double maxLDA = -32000;
							double maxLeacock = -32000;
							double maxWuPalmer = -32000;
							double maxPathSim = -32000;
							int refMaxLSA = 0;
							int refMaxLDA = 0;
							int refMaxLeacock = 0;
							int refMaxWuPalmer = 0;
							int refMaxPathSim = 0;
							
							for (int j = i - 1; j >= i - windowSize && j > 0; j--) {
								secondUtt = (Utterance) c.getBlocks().get(j);
								if (secondUtt != null) {
									SemanticCohesion sc = new SemanticCohesion(firstUtt, secondUtt);
									
									double sim = sc.getLSASim();
									if (maxLSA < sim) {
										maxLSA = sim;
										refMaxLSA = secondUtt.getIndex();
									}
									rowLSA.append(Formatting.formatNumber(sim) + ",");
									
									sim = sc.getLDASim();
									if (maxLDA < sim) {
										maxLDA = sim;
										refMaxLDA = secondUtt.getIndex();
									}
									rowLDA.append(Formatting.formatNumber(sim) + ",");
									
									sim = sc.getOntologySim().get(SimilarityType.LEACOCK_CHODOROW);
									if (maxLeacock < sim) {
										maxLeacock = sim;
										refMaxLeacock = secondUtt.getIndex();
									}
									rowLeacock.append(Formatting.formatNumber(sim) + ",");
									
									sim = sc.getOntologySim().get(SimilarityType.WU_PALMER);
									if (maxWuPalmer < sim) {
										maxWuPalmer = sim;
										refMaxWuPalmer = secondUtt.getIndex();
									}
									rowWuPalmer.append(Formatting.formatNumber(sim) + ",");
									
									sim = sc.getOntologySim().get(SimilarityType.PATH_SIM);
									if (maxPathSim < sim) {
										maxPathSim = sim;
										refMaxPathSim = secondUtt.getIndex();
									}
									rowPathSim.append(Formatting.formatNumber(sim) + ",");
									
									// Mihalcea's formula
									// sim(T1, T2) = .5 * (
									//	SUM(maxSim(word, T2) * idf(word)) / SUM(word) + // word from T1
									// 	SUM(maxSim(word, T1) * idf(word)) / SUM(word))  // word from T2
									
									// TODO: continue work here 
									/*double leftHandSide = 0;
									double rightHandSide = 0;
									Iterator it = mp.entrySet().iterator();
								    while (it.hasNext()) {
								        Map.Entry pair = (Map.Entry)it.next();
								        System.out.println(pair.getKey() + " = " + pair.getValue());
								        it.remove(); // avoids a ConcurrentModificationException
								    }*/
									
									
									
									k++;
								}
							}
							for (int j = k; j < windowSize; j++) {
								rowLSA.append(",");
								rowLDA.append(",");
								rowLeacock.append(",");
								rowWuPalmer.append(",");
								rowPathSim.append(",");
							}
							
							// utterance text
							rowLSA.append(firstUtt.getProcessedText() + ",");
							rowLDA.append(firstUtt.getProcessedText() + ",");
							rowLeacock.append(firstUtt.getProcessedText() + ",");
							rowWuPalmer.append(firstUtt.getProcessedText() + ",");
							rowPathSim.append(firstUtt.getProcessedText() + ",");
							
							if (firstUtt.getRefBlock() != null && firstUtt.getRefBlock().getIndex() != 0) {
								Block refUtt = c.getBlocks().get(firstUtt.getRefBlock().getIndex());
								if (refUtt != null) {
									// reffered utterance id
									rowLSA.append(refUtt.getIndex() + ",");
									rowLDA.append(refUtt.getIndex() + ",");
									rowLeacock.append(refUtt.getIndex() + ",");
									rowWuPalmer.append(refUtt.getIndex() + ",");
									rowPathSim.append(refUtt.getIndex() + ",");
									
									// reffered utterance text
									rowLSA.append(refUtt.getProcessedText() + ",");
									rowLDA.append(refUtt.getProcessedText() + ",");
									rowLeacock.append(refUtt.getProcessedText() + ",");
									rowWuPalmer.append(refUtt.getProcessedText() + ",");
									rowPathSim.append(refUtt.getProcessedText() + ",");
								}
							}
							// if ref id is not set, fill empty fields
							else {
								// reffered utterance id
								rowLSA.append("" + ",");
								rowLDA.append("" + ",");
								rowLeacock.append("" + ",");
								rowWuPalmer.append("" + ",");
								rowPathSim.append("" + ",");
								
								// reffered utterance text
								rowLSA.append("" + ",");
								rowLDA.append("" + ",");
								rowLeacock.append("" + ",");
								rowWuPalmer.append("" + ",");
								rowPathSim.append("" + ",");
							}
							
							// max sim
							rowLSA.append(maxLSA + ",");
							rowLDA.append(maxLDA + ",");
							rowLeacock.append(maxLeacock + ",");
							rowWuPalmer.append(maxWuPalmer + ",");
							rowPathSim.append(maxPathSim + ",");
							
							// id of max sim
							rowLSA.append(refMaxLSA + ",");
							rowLDA.append(refMaxLDA + ",");
							rowLeacock.append(refMaxLeacock + ",");
							rowWuPalmer.append(refMaxWuPalmer + ",");
							rowPathSim.append(refMaxPathSim + ",");
							
							// TODO: add normalized max
							rowLSA.append("N/A" + ",");
							rowLDA.append("N/A" + ",");
							rowLeacock.append("N/A" + ",");
							rowWuPalmer.append("N/A" + ",");
							rowPathSim.append("N/A" + ",");
							
							// TODO: add Mihalcea's formula
							rowLSA.append("N/A" + ",");
							rowLDA.append("N/A" + ",");
							rowLeacock.append("N/A" + ",");
							rowWuPalmer.append("N/A" + ",");
							rowPathSim.append("N/A" + ",");
							
							rowLSA.append('\n');
							rowLDA.append('\n');
							rowLeacock.append('\n');
							rowWuPalmer.append('\n');
							rowPathSim.append('\n');
							
							try {
								FileUtils.writeStringToFile(fileLSA, rowLSA.toString(), "UTF-8", true);
								FileUtils.writeStringToFile(fileLDA, rowLDA.toString(), "UTF-8", true);
								FileUtils.writeStringToFile(fileLeacock, rowLeacock.toString(), "UTF-8", true);
								FileUtils.writeStringToFile(fileWuPalmer, rowWuPalmer.toString(), "UTF-8", true);
								FileUtils.writeStringToFile(filePathSim, rowPathSim.toString(), "UTF-8", true);
							} catch (Exception e) {
								logger.info("Exception: " + e.getMessage());
								e.printStackTrace();
							}
							
						}
					}
				}
			});
		} catch (IOException e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Finished processing for the folder: " + path);
		
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		ReaderBenchServer.initializeDB();
		
		CSCLContributionSimilarities corpusSample = new CSCLContributionSimilarities("resources/in/corpus_v2_sample/", "resources/config/LSA/tasa_en",
				"resources/config/LDA/tasa_en", Lang.getLang("English"), false, true, 0.3, 20);
		corpusSample.process();
	}
}
