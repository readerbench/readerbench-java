package runtime.cscl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument.SaveType;
import data.Lang;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Utterance;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.WordNet.SimilarityType;
import webService.ReaderBenchServer;

public class CSCLContributionSimilarities {

	public Logger logger = Logger.getLogger(CSCLContributionSimilarities.class);
	private String path;

	private String pathToLSA;
	private String pathToLDA;
	private Lang lang;
	private boolean usePOSTagging = false;
	private boolean computeDialogism = false;
	private List<Integer> windowSizes = null;
	private int maxWindowSize;
	private LSA lsa;
	private LDA lda;
	
	private Map<Integer, Integer> totalSimDetectedLSA;
	private Map<Integer, Integer> totalSimInBlockLSA;
	private Map<Integer, Integer> totalNormSimDetectedLSA;
	private Map<Integer, Integer> totalNormSimInBlockLSA;
	private Map<Integer, Integer> totalMihalceaSimDetectedLSA;
	private Map<Integer, Integer> totalMihalceaSimInBlockLSA;
	
	private Map<Integer, Integer> totalSimDetectedLDA;
	private Map<Integer, Integer> totalSimInBlockLDA;
	private Map<Integer, Integer> totalNormSimDetectedLDA;
	private Map<Integer, Integer> totalNormSimInBlockLDA;
	private Map<Integer, Integer> totalMihalceaSimDetectedLDA;
	private Map<Integer, Integer> totalMihalceaSimInBlockLDA;

	private Map<Integer, Integer> totalSimDetectedLeacock;
	private Map<Integer, Integer> totalSimInBlockLeacock;
	private Map<Integer, Integer> totalNormSimDetectedLeacock;
	private Map<Integer, Integer> totalNormSimInBlockLeacock;
	private Map<Integer, Integer> totalMihalceaSimDetectedLeacock;
	private Map<Integer, Integer> totalMihalceaSimInBlockLeacock;

	private Map<Integer, Integer> totalSimDetectedWuPalmer;
	private Map<Integer, Integer> totalSimInBlockWuPalmer;
	private Map<Integer, Integer> totalNormSimDetectedWuPalmer;
	private Map<Integer, Integer> totalNormSimInBlockWuPalmer;
	private Map<Integer, Integer> totalMihalceaSimDetectedWuPalmer;
	private Map<Integer, Integer> totalMihalceaSimInBlockWuPalmer;
	
	private Map<Integer, Integer> totalSimDetectedPathSim;
	private Map<Integer, Integer> totalSimInBlockPathSim;
	private Map<Integer, Integer> totalNormSimDetectedPathSim;
	private Map<Integer, Integer> totalNormSimInBlockPathSim;
	private Map<Integer, Integer> totalMihalceaSimDetectedPathSim;
	private Map<Integer, Integer> totalMihalceaSimInBlockPathSim;

	public CSCLContributionSimilarities(String path, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, boolean computeDialogism, List<Integer> windowSizes, LSA lsa, LDA lda) {
		this.path = path;
		this.pathToLSA = pathToLSA;
		this.pathToLDA = pathToLDA;
		this.lang = lang;
		this.usePOSTagging = usePOSTagging;
		this.computeDialogism = computeDialogism;
		Collections.sort(windowSizes, Collections.reverseOrder());
		this.windowSizes = windowSizes;
		this.maxWindowSize = Collections.max(windowSizes);
		this.lsa = lsa;
		this.lda = lda;
		
		totalSimDetectedLSA = new HashMap<Integer, Integer>();
		totalSimInBlockLSA = new HashMap<Integer, Integer>();
		totalNormSimDetectedLSA = new HashMap<Integer, Integer>();
		totalNormSimInBlockLSA = new HashMap<Integer, Integer>(); 
		totalMihalceaSimDetectedLSA = new HashMap<Integer, Integer>();
		totalMihalceaSimInBlockLSA = new HashMap<Integer, Integer>();
		
		totalSimDetectedLDA = new HashMap<Integer, Integer>();
		totalSimInBlockLDA = new HashMap<Integer, Integer>();
		totalNormSimDetectedLDA = new HashMap<Integer, Integer>();
		totalNormSimInBlockLDA = new HashMap<Integer, Integer>();
		totalMihalceaSimDetectedLDA = new HashMap<Integer, Integer>();
		totalMihalceaSimInBlockLDA = new HashMap<Integer, Integer>();

		totalSimDetectedLeacock = new HashMap<Integer, Integer>();
		totalSimInBlockLeacock = new HashMap<Integer, Integer>();
		totalNormSimDetectedLeacock = new HashMap<Integer, Integer>();
		totalNormSimInBlockLeacock = new HashMap<Integer, Integer>();
		totalMihalceaSimDetectedLeacock = new HashMap<Integer, Integer>();
		totalMihalceaSimInBlockLeacock = new HashMap<Integer, Integer>();

		totalSimDetectedWuPalmer = new HashMap<Integer, Integer>();
		totalSimInBlockWuPalmer = new HashMap<Integer, Integer>();
		totalNormSimDetectedWuPalmer = new HashMap<Integer, Integer>();
		totalNormSimInBlockWuPalmer = new HashMap<Integer, Integer>();
		totalMihalceaSimDetectedWuPalmer = new HashMap<Integer, Integer>();
		totalMihalceaSimInBlockWuPalmer = new HashMap<Integer, Integer>();
		
		totalSimDetectedPathSim = new HashMap<Integer, Integer>();
		totalSimInBlockPathSim = new HashMap<Integer, Integer>();
		totalNormSimDetectedPathSim = new HashMap<Integer, Integer>();
		totalNormSimInBlockPathSim = new HashMap<Integer, Integer>();
		totalMihalceaSimDetectedPathSim = new HashMap<Integer, Integer>();
		totalMihalceaSimInBlockPathSim = new HashMap<Integer, Integer>();
	}
	
	private boolean isInBlock(Conversation c, int min, int max) {
		boolean isInBlock = false;
		if (min != -1) {
			isInBlock = true;
			if (min == max)
				isInBlock = true;
			if (!((Utterance) c.getBlocks().get(min)).getParticipant().getName()
					.equals(((Utterance) c.getBlocks().get(max)).getParticipant().getName())) {
				isInBlock = false;
			}
			else {
				for (int j = min + 1; j <= max - 1; j++) {
					Utterance secondUtt = (Utterance) c.getBlocks().get(j);
					if (secondUtt != null) {
						if (secondUtt.getParticipant() == null || !secondUtt.getParticipant()
								.getName().equals(((Utterance) c.getBlocks().get(min)).getParticipant().getName())) {
							isInBlock = false;
							break;
						}
					}
				}
			}
		}
		return isInBlock;
	}

	public void process() {
		StringBuilder capTabel = new StringBuilder();
		capTabel.append("chat,");
		capTabel.append("utt_id,");
		capTabel.append("utt_participant,");
		for (int i = 1; i <= 20; i++) {
			capTabel.append("d" + i + ",");
			capTabel.append("d" + i + "_norm,");
			capTabel.append("d" + i + "_mihalcea,");
		}
		capTabel.append("utt_text,"); // contribution text
		capTabel.append("ref_id,"); // reference id
		capTabel.append("ref_participant,"); // participant
		capTabel.append("ref_text,"); // reference text
		
		for(Integer windowSize: windowSizes) {
			capTabel.append("max_sim" + windowSize + ","); // max sim
			capTabel.append("max_sim_id" + windowSize + ","); // id of max sim utt
			capTabel.append("max_sim_participant" + windowSize + ","); // participant of max sim utt
			capTabel.append("max_sim_ref_detected" + windowSize + ","); // ref detected
			capTabel.append("max_sim_ref_in_block" + windowSize + ","); // ref in block
			
			capTabel.append("max_sim_norm" + windowSize + ","); // max sim normalized - how ?? (based
												// on distance i guess)
			capTabel.append("max_sim_norm_id" + windowSize + ","); // id of max_sim_norm utt
			capTabel.append("max_sim_norm_participant" + windowSize + ","); // participant of
															// max_sim_norm utt
			capTabel.append("max_sim_norm_ref_detected" + windowSize + ","); // ref detected
			capTabel.append("max_sim_norm_ref_in_block" + windowSize + ","); // ref in block
			
			capTabel.append("mihalcea_sim" + windowSize + ","); // Mihalcea's similarity
			capTabel.append("mihalcea_sim_id" + windowSize + ","); // id of Mihalcea's similarity utt
			capTabel.append("mihalcea_sim_participant" + windowSize + ","); // participant of
															// Mihalcea's similarity
															// utt
			capTabel.append("mihalcea_sim_ref_detected" + windowSize + ","); // ref detected
			capTabel.append("mihalcea_sim_ref_in_block" + windowSize + ","); // ref in block
			
			totalSimDetectedLSA.put(windowSize, 0);
			totalSimInBlockLSA.put(windowSize, 0);
			totalNormSimDetectedLSA.put(windowSize, 0);
			totalNormSimInBlockLSA.put(windowSize, 0); 
			totalMihalceaSimDetectedLSA.put(windowSize, 0);
			totalMihalceaSimInBlockLSA.put(windowSize, 0);
			
			totalSimDetectedLDA.put(windowSize, 0);
			totalSimInBlockLDA.put(windowSize, 0);
			totalNormSimDetectedLDA.put(windowSize, 0);
			totalNormSimInBlockLDA.put(windowSize, 0);
			totalMihalceaSimDetectedLDA.put(windowSize, 0);
			totalMihalceaSimInBlockLDA.put(windowSize, 0);

			totalSimDetectedLeacock.put(windowSize, 0);
			totalSimInBlockLeacock.put(windowSize, 0);
			totalNormSimDetectedLeacock.put(windowSize, 0);
			totalNormSimInBlockLeacock.put(windowSize, 0);
			totalMihalceaSimDetectedLeacock.put(windowSize, 0);
			totalMihalceaSimInBlockLeacock.put(windowSize, 0);

			totalSimDetectedWuPalmer.put(windowSize, 0);
			totalSimInBlockWuPalmer.put(windowSize, 0);
			totalNormSimDetectedWuPalmer.put(windowSize, 0);
			totalNormSimInBlockWuPalmer.put(windowSize, 0);
			totalMihalceaSimDetectedWuPalmer.put(windowSize, 0);
			totalMihalceaSimInBlockWuPalmer.put(windowSize, 0);
			
			totalSimDetectedPathSim.put(windowSize, 0);
			totalSimInBlockPathSim.put(windowSize, 0);
			totalNormSimDetectedPathSim.put(windowSize, 0);
			totalNormSimInBlockPathSim.put(windowSize, 0);
			totalMihalceaSimDetectedPathSim.put(windowSize, 0);
			totalMihalceaSimInBlockPathSim.put(windowSize, 0);
		}
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
					
					Map<Integer, Integer> simDetectedLSA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> simInBlockLSA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimDetectedLSA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimInBlockLSA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimDetectedLSA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimInBlockLSA = new HashMap<Integer, Integer>();
					
					Map<Integer, Integer> simDetectedLDA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> simInBlockLDA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimDetectedLDA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimInBlockLDA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimDetectedLDA = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimInBlockLDA = new HashMap<Integer, Integer>();
					
					Map<Integer, Integer> simDetectedLeacock = new HashMap<Integer, Integer>();
					Map<Integer, Integer> simInBlockLeacock = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimDetectedLeacock = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimInBlockLeacock = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimDetectedLeacock = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimInBlockLeacock = new HashMap<Integer, Integer>();
					
					Map<Integer, Integer> simDetectedWuPalmer = new HashMap<Integer, Integer>();
					Map<Integer, Integer> simInBlockWuPalmer = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimDetectedWuPalmer = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimInBlockWuPalmer = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimDetectedWuPalmer = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimInBlockWuPalmer = new HashMap<Integer, Integer>();
					
					Map<Integer, Integer> simDetectedPathSim = new HashMap<Integer, Integer>();
					Map<Integer, Integer> simInBlockPathSim = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimDetectedPathSim = new HashMap<Integer, Integer>();
					Map<Integer, Integer> normSimInBlockPathSim = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimDetectedPathSim = new HashMap<Integer, Integer>();
					Map<Integer, Integer> mihalceaSimInBlockPathSim = new HashMap<Integer, Integer>();
					
					for (Integer windowSize : windowSizes) {
						simDetectedLSA.put(windowSize, 0);
						simDetectedLDA.put(windowSize, 0);
						simDetectedLeacock.put(windowSize, 0);
						simDetectedWuPalmer.put(windowSize, 0);
						simDetectedPathSim.put(windowSize, 0);
						
						normSimDetectedLSA.put(windowSize, 0);
						normSimDetectedLDA.put(windowSize, 0);
						normSimDetectedLeacock.put(windowSize, 0);
						normSimDetectedWuPalmer.put(windowSize, 0);
						normSimDetectedPathSim.put(windowSize, 0);
						
						mihalceaSimDetectedLSA.put(windowSize, 0);
						mihalceaSimDetectedLDA.put(windowSize, 0);
						mihalceaSimDetectedLeacock.put(windowSize, 0);
						mihalceaSimDetectedWuPalmer.put(windowSize, 0);
						mihalceaSimDetectedPathSim.put(windowSize, 0);
						
						simInBlockLSA.put(windowSize, 0);
						simInBlockLDA.put(windowSize, 0);
						simInBlockLeacock.put(windowSize, 0);
						simInBlockWuPalmer.put(windowSize, 0);
						simInBlockPathSim.put(windowSize, 0);
						
						normSimInBlockLSA.put(windowSize, 0);
						normSimInBlockLDA.put(windowSize, 0);
						normSimInBlockLeacock.put(windowSize, 0);
						normSimInBlockWuPalmer.put(windowSize, 0);
						normSimInBlockPathSim.put(windowSize, 0);
						
						mihalceaSimInBlockLSA.put(windowSize, 0);
						mihalceaSimInBlockLDA.put(windowSize, 0);
						mihalceaSimInBlockLeacock.put(windowSize, 0);
						mihalceaSimInBlockWuPalmer.put(windowSize, 0);
						mihalceaSimInBlockPathSim.put(windowSize, 0);
					}

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

							rowLSA.append(firstUtt.getParticipant().getName() + ",");
							rowLDA.append(firstUtt.getParticipant().getName() + ",");
							rowLeacock.append(firstUtt.getParticipant().getName() + ",");
							rowWuPalmer.append(firstUtt.getParticipant().getName() + ",");
							rowPathSim.append(firstUtt.getParticipant().getName() + ",");

							int k = 0;
							
							Map<Integer, Double> maxLSA = new HashMap<Integer, Double>();
							Map<Integer, Double> maxLDA = new HashMap<Integer, Double>();
							Map<Integer, Double> maxLeacock = new HashMap<Integer, Double>();
							Map<Integer, Double> maxWuPalmer = new HashMap<Integer, Double>();
							Map<Integer, Double> maxPathSim = new HashMap<Integer, Double>();
							
							Map<Integer, Integer> refMaxLSA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMaxLDA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMaxLeacock = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMaxWuPalmer = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMaxPathSim = new HashMap<Integer, Integer>();
							
							Map<Integer, String> participantMaxLSA = new HashMap<Integer, String>();
							Map<Integer, String> participantMaxLDA = new HashMap<Integer, String>();
							Map<Integer, String> participantMaxLeacock = new HashMap<Integer, String>();
							Map<Integer, String> participantMaxWuPalmer = new HashMap<Integer, String>();
							Map<Integer, String> participantMaxPathSim = new HashMap<Integer, String>();
							
							Map<Integer, Double> normMaxLSA = new HashMap<Integer, Double>();
							Map<Integer, Double> normMaxLDA = new HashMap<Integer, Double>();
							Map<Integer, Double> normMaxLeacock = new HashMap<Integer, Double>();
							Map<Integer, Double> normMaxWuPalmer = new HashMap<Integer, Double>();
							Map<Integer, Double> normMaxPathSim = new HashMap<Integer, Double>();
							
							Map<Integer, Integer> refNormMaxLSA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refNormMaxLDA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refNormMaxLeacock = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refNormMaxWuPalmer = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refNormMaxPathSim = new HashMap<Integer, Integer>();
							
							Map<Integer, String> participantNormMaxLSA = new HashMap<Integer, String>();
							Map<Integer, String> participantNormMaxLDA = new HashMap<Integer, String>();
							Map<Integer, String> participantNormMaxLeacock = new HashMap<Integer, String>();
							Map<Integer, String> participantNormMaxWuPalmer = new HashMap<Integer, String>();
							Map<Integer, String> participantNormMaxPathSim = new HashMap<Integer, String>();
							
							Map<Integer, Double> mihalceaMaxLSA = new HashMap<Integer, Double>();
							Map<Integer, Double> mihalceaMaxLDA = new HashMap<Integer, Double>();
							Map<Integer, Double> mihalceaMaxLeacock = new HashMap<Integer, Double>();
							Map<Integer, Double> mihalceaMaxWuPalmer = new HashMap<Integer, Double>();
							Map<Integer, Double> mihalceaMaxPathSim = new HashMap<Integer, Double>();
							
							Map<Integer, Integer> refMihalceaMaxLSA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMihalceaMaxLDA = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMihalceaMaxLeacock = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMihalceaMaxWuPalmer = new HashMap<Integer, Integer>();
							Map<Integer, Integer> refMihalceaMaxPathSim = new HashMap<Integer, Integer>();
							
							Map<Integer, String> participantMihalceaMaxLSA = new HashMap<Integer, String>();
							Map<Integer, String> participantMihalceaMaxLDA = new HashMap<Integer, String>();
							Map<Integer, String> participantMihalceaMaxLeacock = new HashMap<Integer, String>();
							Map<Integer, String> participantMihalceaMaxWuPalmer = new HashMap<Integer, String>();
							Map<Integer, String> participantMihalceaMaxPathSim = new HashMap<Integer, String>();
							
							for(Integer windowSize : windowSizes) {
								maxLSA.put(windowSize, -1.0);
								maxLDA.put(windowSize, -1.0);
								maxLeacock.put(windowSize, -1.0);
								maxWuPalmer.put(windowSize, -1.0);
								maxPathSim.put(windowSize, -1.0);
								
								refMaxLSA.put(windowSize, -1);
								refMaxLDA.put(windowSize, -1);
								refMaxLeacock.put(windowSize, -1);
								refMaxWuPalmer.put(windowSize, -1);
								refMaxPathSim.put(windowSize, -1);
								
								participantMaxLSA.put(windowSize, null);
								participantMaxLDA.put(windowSize, null);
								participantMaxLeacock.put(windowSize, null);
								participantMaxWuPalmer.put(windowSize, null);
								participantMaxPathSim.put(windowSize, null);
								
								normMaxLSA.put(windowSize, -1.0);
								normMaxLDA.put(windowSize, -1.0);
								normMaxLeacock.put(windowSize, -1.0);
								normMaxWuPalmer.put(windowSize, -1.0);
								normMaxPathSim.put(windowSize, -1.0);
								
								refNormMaxLSA.put(windowSize, -1);
								refNormMaxLDA.put(windowSize, -1);
								refNormMaxLeacock.put(windowSize, -1);
								refNormMaxWuPalmer.put(windowSize, -1);
								refNormMaxPathSim.put(windowSize, -1);
								
								participantNormMaxLSA.put(windowSize, null);
								participantNormMaxLDA.put(windowSize, null);
								participantNormMaxLeacock.put(windowSize, null);
								participantNormMaxWuPalmer.put(windowSize, null);
								participantNormMaxPathSim.put(windowSize, null);
								
								mihalceaMaxLSA.put(windowSize, -1.0);
								mihalceaMaxLDA.put(windowSize, -1.0);
								mihalceaMaxLeacock.put(windowSize, -1.0);
								mihalceaMaxWuPalmer.put(windowSize, -1.0);
								mihalceaMaxPathSim.put(windowSize, -1.0);
								
								refMihalceaMaxLSA.put(windowSize, -1);
								refMihalceaMaxLDA.put(windowSize, -1);
								refMihalceaMaxLeacock.put(windowSize, -1);
								refMihalceaMaxWuPalmer.put(windowSize, -1);
								refMihalceaMaxPathSim.put(windowSize, -1);
								
								participantMihalceaMaxLSA.put(windowSize, null);
								participantMihalceaMaxLDA.put(windowSize, null);
								participantMihalceaMaxLeacock.put(windowSize, null);
								participantMihalceaMaxWuPalmer.put(windowSize, null);
								participantMihalceaMaxPathSim.put(windowSize, null);
							}
							
							// windowSize
							for (int j = i - 1; j >= i - maxWindowSize && j > 0; j--) {
								secondUtt = (Utterance) c.getBlocks().get(j);
								if (secondUtt != null) {
									int distance = i - j;

									double sim;
									// Mihalcea start
									// Mihalcea's formula
									// sim(T1, T2) = .5 * (
									// SUM(maxSim(word, T2) * idf(word)) /
									// SUM(word) + // word from T1
									// SUM(maxSim(word, T1) * idf(word)) /
									// SUM(word)) // word from T2
									double leftHandSideUpLSA = 0;
									double leftHandSideDownLSA = 0;
									double rightHandSideUpLSA = 0;
									double rightHandSideDownLSA = 0;

									double leftHandSideUpLDA = 0;
									double leftHandSideDownLDA = 0;
									double rightHandSideUpLDA = 0;
									double rightHandSideDownLDA = 0;

									double leftHandSideUpLeacock = 0;
									double leftHandSideDownLeacock = 0;
									double rightHandSideUpLeacock = 0;
									double rightHandSideDownLeacock = 0;

									double leftHandSideUpWuPalmer = 0;
									double leftHandSideDownWuPalmer = 0;
									double rightHandSideUpWuPalmer = 0;
									double rightHandSideDownWuPalmer = 0;

									double leftHandSideUpPathSim = 0;
									double leftHandSideDownPathSim = 0;
									double rightHandSideUpPathSim = 0;
									double rightHandSideDownPathSim = 0;
									// iterate through words of first sentence
									Iterator<Entry<Word, Integer>> itFirstUtt = firstUtt.getWordOccurences().entrySet().iterator();
									while (itFirstUtt.hasNext()) {
										Map.Entry<Word, Integer> pairFirstUtt = (Map.Entry<Word, Integer>) itFirstUtt.next();
										// System.out.println(pair.getKey() + "
										// = " + pair.getValue());
										Word wordFirstUtt = (Word) pairFirstUtt.getKey();

										// iterate through words of second
										// sentence
										double maxSimForWordWithOtherUttLSA = 0;
										double maxSimForWordWithOtherUttLDA = 0;
										double maxSimForWordWithOtherUttLeacock = 0;
										double maxSimForWordWithOtherUttWuPalmer = 0;
										double maxSimForWordWithOtherUttPathSim = 0;
										Iterator<Entry<Word, Integer>> itSecondUtt = secondUtt.getWordOccurences().entrySet().iterator();
										while (itSecondUtt.hasNext()) {
											Map.Entry<Word, Integer> pairSecondUtt = (Map.Entry<Word, Integer>) itSecondUtt.next();
											Word wordSecondUtt = (Word) pairSecondUtt.getKey();

											sim = lsa.getSimilarity(wordFirstUtt, wordSecondUtt);
											if (sim > maxSimForWordWithOtherUttLSA) {
												maxSimForWordWithOtherUttLSA = sim;
											}

											sim = lda.getSimilarity(wordFirstUtt, wordSecondUtt);
											if (sim > maxSimForWordWithOtherUttLDA) {
												maxSimForWordWithOtherUttLDA = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.LEACOCK_CHODOROW);
											if (sim > maxSimForWordWithOtherUttLeacock) {
												maxSimForWordWithOtherUttLeacock = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.WU_PALMER);
											if (sim > maxSimForWordWithOtherUttWuPalmer) {
												maxSimForWordWithOtherUttWuPalmer = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.PATH_SIM);
											if (sim > maxSimForWordWithOtherUttPathSim) {
												maxSimForWordWithOtherUttPathSim = sim;
											}
										}

										leftHandSideUpLSA += maxSimForWordWithOtherUttLSA
												* lsa.getWordIDf(wordFirstUtt);
										leftHandSideDownLSA += lsa.getWordIDf(wordFirstUtt);

										leftHandSideUpLDA += maxSimForWordWithOtherUttLDA
												* lsa.getWordIDf(wordFirstUtt);
										leftHandSideDownLDA += lsa.getWordIDf(wordFirstUtt);

										leftHandSideUpLeacock += maxSimForWordWithOtherUttLeacock
												* lsa.getWordIDf(wordFirstUtt);
										leftHandSideDownLeacock += lsa.getWordIDf(wordFirstUtt);

										leftHandSideUpWuPalmer += maxSimForWordWithOtherUttWuPalmer
												* lsa.getWordIDf(wordFirstUtt);
										leftHandSideDownWuPalmer += lsa.getWordIDf(wordFirstUtt);

										leftHandSideUpPathSim += maxSimForWordWithOtherUttPathSim
												* lsa.getWordIDf(wordFirstUtt);
										leftHandSideDownPathSim += lsa.getWordIDf(wordFirstUtt);
									}

									itFirstUtt = secondUtt.getWordOccurences().entrySet().iterator();
									while (itFirstUtt.hasNext()) {
										Map.Entry<Word, Integer> pairFirstUtt = (Map.Entry<Word, Integer>) itFirstUtt.next();
										// System.out.println(pair.getKey() + "
										// = " + pair.getValue());
										Word wordFirstUtt = (Word) pairFirstUtt.getKey();

										// iterate through words of second
										// sentence
										double maxSimForWordWithOtherUttLSA = 0;
										double maxSimForWordWithOtherUttLDA = 0;
										double maxSimForWordWithOtherUttLeacock = 0;
										double maxSimForWordWithOtherUttWuPalmer = 0;
										double maxSimForWordWithOtherUttPathSim = 0;
										Iterator<Entry<Word, Integer>> itSecondUtt = firstUtt.getWordOccurences().entrySet().iterator();
										while (itSecondUtt.hasNext()) {
											Map.Entry<Word, Integer> pairSecondUtt = (Map.Entry<Word, Integer>) itSecondUtt.next();
											Word wordSecondUtt = (Word) pairSecondUtt.getKey();

											sim = lsa.getSimilarity(wordFirstUtt, wordSecondUtt);
											if (sim > maxSimForWordWithOtherUttLSA) {
												maxSimForWordWithOtherUttLSA = sim;
											}

											sim = lda.getSimilarity(wordFirstUtt, wordSecondUtt);
											if (sim > maxSimForWordWithOtherUttLDA) {
												maxSimForWordWithOtherUttLDA = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.LEACOCK_CHODOROW);
											if (sim > maxSimForWordWithOtherUttLeacock) {
												maxSimForWordWithOtherUttLeacock = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.WU_PALMER);
											if (sim > maxSimForWordWithOtherUttWuPalmer) {
												maxSimForWordWithOtherUttWuPalmer = sim;
											}

											sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
													SimilarityType.PATH_SIM);
											if (sim > maxSimForWordWithOtherUttPathSim) {
												maxSimForWordWithOtherUttPathSim = sim;
											}
										}

										rightHandSideUpLSA += maxSimForWordWithOtherUttLSA
												* lsa.getWordIDf(wordFirstUtt);
										rightHandSideDownLSA += lsa.getWordIDf(wordFirstUtt);

										rightHandSideUpLDA += maxSimForWordWithOtherUttLDA
												* lsa.getWordIDf(wordFirstUtt);
										rightHandSideDownLDA += lsa.getWordIDf(wordFirstUtt);

										rightHandSideUpLeacock += maxSimForWordWithOtherUttLeacock
												* lsa.getWordIDf(wordFirstUtt);
										rightHandSideDownLeacock += lsa.getWordIDf(wordFirstUtt);

										rightHandSideUpWuPalmer += maxSimForWordWithOtherUttWuPalmer
												* lsa.getWordIDf(wordFirstUtt);
										rightHandSideDownWuPalmer += lsa.getWordIDf(wordFirstUtt);

										rightHandSideUpPathSim += maxSimForWordWithOtherUttPathSim
												* lsa.getWordIDf(wordFirstUtt);
										rightHandSideDownPathSim += lsa.getWordIDf(wordFirstUtt);
									}
									// Mihalcea end

									SemanticCohesion sc = new SemanticCohesion(firstUtt, secondUtt);

									// ===== LSA =====
									
									// ReaderBench similarity
									sim = sc.getLSASim();
									rowLSA.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && maxLSA.get(windowSize) < sim) {
											maxLSA.put(windowSize, sim);
											refMaxLSA.put(windowSize, secondUtt.getIndex());
											participantMaxLSA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// normalized similarity
									double normSim = sim / (i - j + 1);
									rowLSA.append(Formatting.formatNumber(normSim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && normMaxLSA.get(windowSize) < normSim) {
											normMaxLSA.put(windowSize, normSim);
											refNormMaxLSA.put(windowSize, secondUtt.getIndex());
											participantNormMaxLSA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// Mihalcea's similarity
									sim = .5 * (((leftHandSideDownLSA > 0) ? (leftHandSideUpLSA / leftHandSideDownLSA)
											: 0)
											+ ((rightHandSideDownLSA > 0) ? (rightHandSideUpLSA / rightHandSideDownLSA)
													: 0));
									rowLSA.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && mihalceaMaxLSA.get(windowSize) < sim) {
											mihalceaMaxLSA.put(windowSize, sim);
											refMihalceaMaxLSA.put(windowSize, secondUtt.getIndex());
											participantMihalceaMaxLSA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// ===== LDA =====
									
									// ReaderBench similarity
									sim = sc.getLDASim();
									rowLDA.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && maxLDA.get(windowSize) < sim) {
											maxLDA.put(windowSize, sim);
											refMaxLDA.put(windowSize, secondUtt.getIndex());
											participantMaxLDA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// normalized similarity
									normSim = sim / (i - j + 1);
									rowLDA.append(Formatting.formatNumber(normSim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && normMaxLDA.get(windowSize) < normSim) {
											normMaxLDA.put(windowSize, normSim);
											refNormMaxLDA.put(windowSize, secondUtt.getIndex());
											participantNormMaxLDA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// ReaderBench similarity
									sim = .5 * (((leftHandSideDownLDA > 0) ? (leftHandSideUpLDA / leftHandSideDownLDA)
											: 0)
											+ ((rightHandSideDownLDA > 0) ? (rightHandSideUpLDA / rightHandSideDownLDA)
													: 0));
									rowLDA.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && mihalceaMaxLDA.get(windowSize) < sim) {
											mihalceaMaxLDA.put(windowSize, sim);
											refMihalceaMaxLDA.put(windowSize, secondUtt.getIndex());
											participantMihalceaMaxLDA.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// ===== Leacock =====

									// ReaderBench similarity
									sim = sc.getOntologySim().get(SimilarityType.LEACOCK_CHODOROW);
									// sim =
									// OntologySupport.semanticSimilarity(firstUtt,
									// secondUtt,
									// SimilarityType.LEACOCK_CHODOROW);
									rowLeacock.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && maxLeacock.get(windowSize) < sim) {
											maxLeacock.put(windowSize, sim);
											refMaxLeacock.put(windowSize, secondUtt.getIndex());
											participantMaxLeacock.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// normalized similarity
									normSim = sim / (i - j + 1);
									rowLeacock.append(Formatting.formatNumber(normSim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && normMaxLeacock.get(windowSize) < normSim) {
											normMaxLeacock.put(windowSize, normSim);
											refNormMaxLeacock.put(windowSize, secondUtt.getIndex());
											participantNormMaxLeacock.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// ReaderBench similarity
									sim = .5 * (((leftHandSideDownLeacock > 0)
											? (leftHandSideUpLeacock / leftHandSideDownLeacock) : 0)
											+ ((rightHandSideDownLeacock > 0)
													? (rightHandSideUpLeacock / rightHandSideDownLeacock) : 0));
									rowLeacock.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && mihalceaMaxLeacock.get(windowSize) < sim) {
											mihalceaMaxLeacock.put(windowSize, sim);
											refMihalceaMaxLeacock.put(windowSize, secondUtt.getIndex());
											participantMihalceaMaxLeacock.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// ===== WU PALMER =====
									
									// ReaderBench similarity
									sim = sc.getOntologySim().get(SimilarityType.WU_PALMER);
									rowWuPalmer.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && maxWuPalmer.get(windowSize) < sim) {
											maxWuPalmer.put(windowSize, sim);
											refMaxWuPalmer.put(windowSize, secondUtt.getIndex());
											participantMaxWuPalmer.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// normalized similarity
									normSim = sim / (i - j + 1);
									rowWuPalmer.append(Formatting.formatNumber(normSim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && normMaxWuPalmer.get(windowSize) < normSim) {
											normMaxWuPalmer.put(windowSize, normSim);
											refNormMaxWuPalmer.put(windowSize, secondUtt.getIndex());
											participantNormMaxWuPalmer.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// ReaderBench similarity
									sim = .5 * (((leftHandSideDownWuPalmer > 0)
											? (leftHandSideUpWuPalmer / leftHandSideDownWuPalmer) : 0)
											+ ((rightHandSideDownWuPalmer > 0)
													? (rightHandSideUpWuPalmer / rightHandSideDownWuPalmer) : 0));
									rowWuPalmer.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && mihalceaMaxWuPalmer.get(windowSize) < sim) {
											mihalceaMaxWuPalmer.put(windowSize, sim);
											refMihalceaMaxWuPalmer.put(windowSize, secondUtt.getIndex());
											participantMihalceaMaxWuPalmer.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									// ===== PATH SIM =====
									
									// ReaderBench similarity
									sim = sc.getOntologySim().get(SimilarityType.PATH_SIM);
									rowPathSim.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && maxPathSim.get(windowSize) < sim) {
											maxPathSim.put(windowSize, sim);
											refMaxPathSim.put(windowSize, secondUtt.getIndex());
											participantMaxPathSim.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// normalized similarity
									normSim = sim / (i - j + 1);
									rowPathSim.append(Formatting.formatNumber(normSim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && normMaxPathSim.get(windowSize) < normSim) {
											normMaxPathSim.put(windowSize, normSim);
											refNormMaxPathSim.put(windowSize, secondUtt.getIndex());
											participantNormMaxPathSim.put(windowSize, secondUtt.getParticipant().getName());
										}
									}
									
									// ReaderBench similarity
									sim = .5 * (((leftHandSideDownPathSim > 0)
											? (leftHandSideUpPathSim / leftHandSideDownPathSim) : 0)
											+ ((rightHandSideDownPathSim > 0)
													? (rightHandSideUpPathSim / rightHandSideDownPathSim) : 0));
									rowPathSim.append(Formatting.formatNumber(sim) + ",");
									for(Integer windowSize: windowSizes) {
										if (distance <= windowSize && mihalceaMaxPathSim.get(windowSize) < sim) {
											mihalceaMaxPathSim.put(windowSize, sim);
											refMihalceaMaxPathSim.put(windowSize, secondUtt.getIndex());
											participantMihalceaMaxPathSim.put(windowSize, secondUtt.getParticipant().getName());
										}
									}

									k++;
								}
							}
							for (int j = k; j < maxWindowSize; j++) {
								// two commas because of similarity, normalized
								// similarity and Mihalcea similarity
								rowLSA.append(",,,");
								rowLDA.append(",,,");
								rowLeacock.append(",,,");
								rowWuPalmer.append(",,,");
								rowPathSim.append(",,,");
							}

							// utterance text
							rowLSA.append(firstUtt.getProcessedText() + ",");
							rowLDA.append(firstUtt.getProcessedText() + ",");
							rowLeacock.append(firstUtt.getProcessedText() + ",");
							rowWuPalmer.append(firstUtt.getProcessedText() + ",");
							rowPathSim.append(firstUtt.getProcessedText() + ",");

							double refId = -1;
							if (firstUtt.getRefBlock() != null && firstUtt.getRefBlock().getIndex() != 0) {
								Utterance refUtt = (Utterance) c.getBlocks().get(firstUtt.getRefBlock().getIndex());
								if (refUtt != null) {
									// referred utterance id
									refId = refUtt.getIndex();
									rowLSA.append(refId + ",");
									rowLDA.append(refId + ",");
									rowLeacock.append(refId + ",");
									rowWuPalmer.append(refId + ",");
									rowPathSim.append(refId + ",");

									// referred participant name
									rowLSA.append(refUtt.getParticipant().getName() + ",");
									rowLDA.append(refUtt.getParticipant().getName() + ",");
									rowLeacock.append(refUtt.getParticipant().getName() + ",");
									rowWuPalmer.append(refUtt.getParticipant().getName() + ",");
									rowPathSim.append(refUtt.getParticipant().getName() + ",");

									// referred utterance text
									rowLSA.append(refUtt.getProcessedText() + ",");
									rowLDA.append(refUtt.getProcessedText() + ",");
									rowLeacock.append(refUtt.getProcessedText() + ",");
									rowWuPalmer.append(refUtt.getProcessedText() + ",");
									rowPathSim.append(refUtt.getProcessedText() + ",");
								}
							}
							// if ref id is not set, fill empty fields
							else {
								// referred utterance id
								rowLSA.append("" + ",");
								rowLDA.append("" + ",");
								rowLeacock.append("" + ",");
								rowWuPalmer.append("" + ",");
								rowPathSim.append("" + ",");

								// referred participant name
								rowLSA.append("" + ",");
								rowLDA.append("" + ",");
								rowLeacock.append("" + ",");
								rowWuPalmer.append("" + ",");
								rowPathSim.append("" + ",");

								// referred utterance text
								rowLSA.append("" + ",");
								rowLDA.append("" + ",");
								rowLeacock.append("" + ",");
								rowWuPalmer.append("" + ",");
								rowPathSim.append("" + ",");
							}

							Integer minRef;
							Integer maxRef;
							minRef = 0;
							maxRef = new Double(Double.POSITIVE_INFINITY).intValue();

							for(Integer windowSize: windowSizes) {
								
								if (refId != -1) {
									if (refId == refMaxLSA.get(windowSize)) {
										simDetectedLSA.put(windowSize, simDetectedLSA.get(windowSize) + 1);
									}
									if (refId == refNormMaxLSA.get(windowSize)) {
										normSimDetectedLSA.put(windowSize, normSimDetectedLDA.get(windowSize) + 1);
									}
									if (refId == refMihalceaMaxLSA.get(windowSize)) {
										mihalceaSimDetectedLSA.put(windowSize, mihalceaSimDetectedLSA.get(windowSize) + 1);
									}
									
									if (refId == refMaxLDA.get(windowSize)) {
										simDetectedLDA.put(windowSize, simDetectedLDA.get(windowSize) + 1);
									}
									if (refId == refNormMaxLDA.get(windowSize)) {
										normSimDetectedLDA.put(windowSize, normSimDetectedLDA.get(windowSize) + 1);
									}
									if (refId == refMihalceaMaxLDA.get(windowSize)) {
										mihalceaSimDetectedLDA.put(windowSize, mihalceaSimDetectedLDA.get(windowSize) + 1);
									}
									
									if (refId == refMaxLeacock.get(windowSize)) {
										simDetectedLeacock.put(windowSize, simDetectedLeacock.get(windowSize) + 1);
									}
									if (refId == refNormMaxLeacock.get(windowSize)) {
										normSimDetectedLeacock.put(windowSize, normSimDetectedLeacock.get(windowSize) + 1);
									}
									if (refId == refMihalceaMaxLeacock.get(windowSize)) {
										mihalceaSimDetectedLeacock.put(windowSize, mihalceaSimDetectedLeacock.get(windowSize) + 1);
									}
									
									if (refId == refMaxWuPalmer.get(windowSize)) {
										simDetectedWuPalmer.put(windowSize, simDetectedWuPalmer.get(windowSize) + 1);
									}
									if (refId == refNormMaxWuPalmer.get(windowSize)) {
										normSimDetectedWuPalmer.put(windowSize, normSimDetectedWuPalmer.get(windowSize) + 1);
									}
									if (refId == refMihalceaMaxWuPalmer.get(windowSize)) {
										mihalceaSimDetectedWuPalmer.put(windowSize, mihalceaSimDetectedWuPalmer.get(windowSize) + 1);
									}
									
									if (refId == refMaxPathSim.get(windowSize)) {
										simDetectedPathSim.put(windowSize, simDetectedPathSim.get(windowSize) + 1);
									}
									if (refId == refNormMaxPathSim.get(windowSize)) {
										normSimDetectedPathSim.put(windowSize, normSimDetectedPathSim.get(windowSize) + 1);
									}
									if (refId == refMihalceaMaxPathSim.get(windowSize)) {
										mihalceaSimDetectedPathSim.put(windowSize, mihalceaSimDetectedPathSim.get(windowSize) + 1);
									}
								}
								
								// max sim
								rowLSA.append(maxLSA.get(windowSize) + ",");
								rowLDA.append(maxLDA.get(windowSize) + ",");
								rowLeacock.append(maxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(maxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(maxPathSim.get(windowSize) + ",");
	
								// id of max sim
								rowLSA.append(refMaxLSA.get(windowSize) + ",");
								rowLDA.append(refMaxLDA.get(windowSize) + ",");
								rowLeacock.append(refMaxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(refMaxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(refMaxPathSim.get(windowSize) + ",");
	
								// max sim participant name
								rowLSA.append(((participantMaxLSA.get(windowSize) != null) ? participantMaxLSA.get(windowSize) : "") + ",");
								rowLDA.append(((participantMaxLDA.get(windowSize) != null) ? participantMaxLDA.get(windowSize) : "") + ",");
								rowLeacock.append(((participantMaxLeacock.get(windowSize) != null) ? participantMaxLeacock.get(windowSize) : "") + ",");
								rowWuPalmer.append(((participantMaxWuPalmer.get(windowSize) != null) ? participantMaxWuPalmer.get(windowSize) : "") + ",");
								rowPathSim.append(((participantMaxPathSim.get(windowSize) != null) ? participantMaxPathSim.get(windowSize) : "") + ",");

								// ref detected?
								rowLSA.append(((refId != -1 && refId == refMaxLSA.get(windowSize)) ? 1 : 0) + ",");
								rowLDA.append(((refId != -1 && refId == refMaxLDA.get(windowSize)) ? 1 : 0) + ",");
								rowLeacock.append(((refId != -1 && refId == refMaxLeacock.get(windowSize)) ? 1 : 0) + ",");
								rowWuPalmer.append(((refId != -1 && refId == refMaxWuPalmer.get(windowSize)) ? 1 : 0) + ",");
								rowPathSim.append(((refId != -1 && refId == refMaxPathSim.get(windowSize)) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMaxLSA.get(windowSize));
								maxRef = (int) Math.max(refId, refMaxLSA.get(windowSize));
								boolean isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									simInBlockLSA.put(windowSize, simInBlockLSA.get(windowSize) + 1);
								}
								rowLSA.append(((isInBlock) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMaxLDA.get(windowSize));
								maxRef = (int) Math.max(refId, refMaxLDA.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									simInBlockLDA.put(windowSize, simInBlockLDA.get(windowSize) + 1);
								}
								rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMaxLeacock.get(windowSize));
								maxRef = (int) Math.max(refId, refMaxLeacock.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									simInBlockLeacock.put(windowSize, simDetectedLeacock.get(windowSize) + 1);
								}
								rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMaxWuPalmer.get(windowSize));
								maxRef = (int) Math.max(refId, refMaxWuPalmer.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									simInBlockWuPalmer.put(windowSize, simInBlockWuPalmer.get(windowSize) + 1);
								}
								rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMaxPathSim.get(windowSize));
								maxRef = (int) Math.max(refId, refMaxPathSim.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									simInBlockPathSim.put(windowSize, simInBlockPathSim.get(windowSize) + 1);
								}
								rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");
								
								// max (sim normalized)
								rowLSA.append(normMaxLSA.get(windowSize) + ",");
								rowLDA.append(normMaxLDA.get(windowSize) + ",");
								rowLeacock.append(normMaxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(normMaxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(normMaxPathSim.get(windowSize) + ",");
	
								// id of max (sim normalized)
								rowLSA.append(refNormMaxLSA.get(windowSize) + ",");
								rowLDA.append(refNormMaxLDA.get(windowSize) + ",");
								rowLeacock.append(refNormMaxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(refNormMaxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(refNormMaxPathSim.get(windowSize) + ",");
	
								// max sim participant name
								rowLSA.append(((participantNormMaxLSA.get(windowSize) != null) ? participantNormMaxLSA.get(windowSize) : "") + ",");
								rowLDA.append(((participantNormMaxLDA.get(windowSize) != null) ? participantNormMaxLDA.get(windowSize) : "") + ",");
								rowLeacock.append(
										((participantNormMaxLeacock.get(windowSize) != null) ? participantNormMaxLeacock.get(windowSize) : "") + ",");
								rowWuPalmer.append(
										((participantNormMaxWuPalmer.get(windowSize) != null) ? participantNormMaxWuPalmer.get(windowSize) : "") + ",");
								rowPathSim.append(
										((participantNormMaxPathSim.get(windowSize) != null) ? participantNormMaxPathSim.get(windowSize) : "") + ",");
	
								// ref detected?
								rowLSA.append(((refId != -1 && refId == refNormMaxLSA.get(windowSize)) ? 1 : 0) + ",");
								rowLDA.append(((refId != -1 && refId == refNormMaxLDA.get(windowSize)) ? 1 : 0) + ",");
								rowLeacock.append(((refId != -1 && refId == refNormMaxLeacock.get(windowSize)) ? 1 : 0) + ",");
								rowWuPalmer.append(((refId != -1 && refId == refNormMaxWuPalmer.get(windowSize)) ? 1 : 0) + ",");
								rowPathSim.append(((refId != -1 && refId == refNormMaxPathSim.get(windowSize)) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refNormMaxLSA.get(windowSize));
								maxRef = (int) Math.max(refId, refNormMaxLSA.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									normSimInBlockLSA.put(windowSize, normSimInBlockLSA.get(windowSize) + 1);
								}
								rowLSA.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refNormMaxLDA.get(windowSize));
								maxRef = (int) Math.max(refId, refNormMaxLDA.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									normSimInBlockLDA.put(windowSize, normSimInBlockLDA.get(windowSize) + 1);
								}
								rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refNormMaxLeacock.get(windowSize));
								maxRef = (int) Math.max(refId, refNormMaxLeacock.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									normSimInBlockLeacock.put(windowSize, normSimInBlockLeacock.get(windowSize) + 1);
								}
								rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refNormMaxWuPalmer.get(windowSize));
								maxRef = (int) Math.max(refId, refNormMaxWuPalmer.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									normSimInBlockWuPalmer.put(windowSize, normSimInBlockWuPalmer.get(windowSize) + 1);
								}
								rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refNormMaxPathSim.get(windowSize));
								maxRef = (int) Math.max(refId, refNormMaxPathSim.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									normSimInBlockPathSim.put(windowSize, normSimInBlockPathSim.get(windowSize) + 1);
								}
								rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");
	
								// Mihalcea's similarity
								rowLSA.append(mihalceaMaxLSA.get(windowSize) + ",");
								rowLDA.append(mihalceaMaxLDA.get(windowSize) + ",");
								rowLeacock.append(mihalceaMaxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(mihalceaMaxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(mihalceaMaxPathSim.get(windowSize) + ",");
	
								// id of max (Mihalcea's similarity)
								rowLSA.append(refMihalceaMaxLSA.get(windowSize) + ",");
								rowLDA.append(refMihalceaMaxLDA.get(windowSize) + ",");
								rowLeacock.append(refMihalceaMaxLeacock.get(windowSize) + ",");
								rowWuPalmer.append(refMihalceaMaxWuPalmer.get(windowSize) + ",");
								rowPathSim.append(refMihalceaMaxPathSim.get(windowSize) + ",");
	
								// max sim participant name (Mihalcea's similarity)
								rowLSA.append(((participantMihalceaMaxLSA.get(windowSize) != null) ? participantMihalceaMaxLSA.get(windowSize) : "") + ",");
								rowLDA.append(((participantMihalceaMaxLDA.get(windowSize) != null) ? participantMihalceaMaxLDA.get(windowSize) : "") + ",");
								rowLeacock.append(
										((participantMihalceaMaxLeacock.get(windowSize) != null) ? participantMihalceaMaxLeacock.get(windowSize) : "")
												+ ",");
								rowWuPalmer.append(
										((participantMihalceaMaxWuPalmer.get(windowSize) != null) ? participantMihalceaMaxWuPalmer.get(windowSize) : "")
												+ ",");
								rowPathSim.append(
										((participantMihalceaMaxPathSim.get(windowSize) != null) ? participantMihalceaMaxPathSim.get(windowSize) : "")
												+ ",");
	
								// ref detected?
								rowLSA.append(((refId != -1 && refId == refMihalceaMaxLSA.get(windowSize)) ? 1 : 0) + ",");
								rowLDA.append(((refId != -1 && refId == refMihalceaMaxLDA.get(windowSize)) ? 1 : 0) + ",");
								rowLeacock.append(((refId != -1 && refId == refMihalceaMaxLeacock.get(windowSize)) ? 1 : 0) + ",");
								rowWuPalmer.append(((refId != -1 && refId == refMihalceaMaxWuPalmer.get(windowSize)) ? 1 : 0) + ",");
								rowPathSim.append(((refId != -1 && refId == refMihalceaMaxPathSim.get(windowSize)) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMihalceaMaxLSA.get(windowSize));
								maxRef = (int) Math.max(refId, refMihalceaMaxLSA.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									mihalceaSimInBlockLSA.put(windowSize, mihalceaSimInBlockLSA.get(windowSize) + 1);
								}
								rowLSA.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMihalceaMaxLDA.get(windowSize));
								maxRef = (int) Math.max(refId, refMihalceaMaxLDA.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									mihalceaSimInBlockLDA.put(windowSize, mihalceaSimInBlockLDA.get(windowSize) + 1);
								}
								rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMihalceaMaxLeacock.get(windowSize));
								maxRef = (int) Math.max(refId, refMihalceaMaxLeacock.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									mihalceaSimInBlockLeacock.put(windowSize, mihalceaSimInBlockLeacock.get(windowSize) + 1);
								}
								rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMihalceaMaxWuPalmer.get(windowSize));
								maxRef = (int) Math.max(refId, refMihalceaMaxWuPalmer.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									mihalceaSimInBlockWuPalmer.put(windowSize, mihalceaSimInBlockWuPalmer.get(windowSize) + 1);
								}
								rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");
	
								minRef = (int) Math.min(refId, refMihalceaMaxPathSim.get(windowSize));
								maxRef = (int) Math.max(refId, refMihalceaMaxPathSim.get(windowSize));
								isInBlock = isInBlock(c, minRef, maxRef);
								if (isInBlock) {
									mihalceaSimInBlockPathSim.put(windowSize, mihalceaSimInBlockPathSim.get(windowSize) + 1);
								}
								rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");

							}
							// 3 x cautat alta referinta in blocul dat de
							// replicile aceleiasi
							// daca toate replicile intre minim si maxim
							// (ref_id, max_det_algoritm) au ac speaker
							// pe viitor : un bloc de replici ale aceleasi
							// persoane sa fie un singur paragraf

							rowLSA.append('\n');
							rowLDA.append('\n');
							rowLeacock.append('\n');
							rowWuPalmer.append('\n');
							rowPathSim.append('\n');
							
							if (i == c.getBlocks().size() - 1) {
								// totals
								rowLSA.append(",,,");
								rowLDA.append(",,,");
								rowLeacock.append(",,,");
								rowWuPalmer.append(",,,");
								rowPathSim.append(",,,");
								for (int j = 0; j < maxWindowSize; j++) {
									rowLSA.append(",,,");
									rowLDA.append(",,,");
									rowLeacock.append(",,,");
									rowWuPalmer.append(",,,");
									rowPathSim.append(",,,");
								}
								rowLSA.append(",,,,");
								rowLDA.append(",,,,");
								rowLeacock.append(",,,,");
								rowWuPalmer.append(",,,,");
								rowPathSim.append(",,,,");
								
								for(Integer windowSize : windowSizes) {
									
									rowLSA.append(",,,");
									rowLDA.append(",,,");
									rowLeacock.append(",,,");
									rowWuPalmer.append(",,,");
									rowPathSim.append(",,,");
									
									rowLSA.append(simDetectedLSA.get(windowSize) + ",");
									rowLDA.append(simDetectedLDA.get(windowSize) + ",");
									rowLeacock.append(simDetectedLeacock.get(windowSize) + ",");
									rowWuPalmer.append(simDetectedWuPalmer.get(windowSize) + ",");
									rowPathSim.append(simDetectedPathSim.get(windowSize) + ",");
									
									rowLSA.append(simInBlockLSA.get(windowSize) + ",");
									rowLDA.append(simInBlockLDA.get(windowSize) + ",");
									rowLeacock.append(simInBlockLeacock.get(windowSize) + ",");
									rowWuPalmer.append(simInBlockWuPalmer.get(windowSize) + ",");
									rowPathSim.append(simInBlockPathSim.get(windowSize) + ",");
									
									totalSimDetectedLSA.put(windowSize, totalSimDetectedLSA.get(windowSize) + simDetectedLSA.get(windowSize));
									totalSimDetectedLDA.put(windowSize, totalSimDetectedLDA.get(windowSize) + simDetectedLDA.get(windowSize));
									totalSimDetectedLeacock.put(windowSize, totalSimDetectedLeacock.get(windowSize) + simDetectedLeacock.get(windowSize));
									totalSimDetectedWuPalmer.put(windowSize, totalSimDetectedWuPalmer.get(windowSize) + simDetectedWuPalmer.get(windowSize));
									totalSimDetectedPathSim.put(windowSize, totalSimDetectedPathSim.get(windowSize) + simDetectedPathSim.get(windowSize));
									
									totalSimInBlockLSA.put(windowSize, totalSimInBlockLSA.get(windowSize) + simInBlockLSA.get(windowSize));
									totalSimInBlockLDA.put(windowSize, totalSimInBlockLDA.get(windowSize) + simInBlockLDA.get(windowSize));
									totalSimInBlockLeacock.put(windowSize, totalSimInBlockLeacock.get(windowSize) + simInBlockLeacock.get(windowSize));
									totalSimInBlockWuPalmer.put(windowSize, totalSimInBlockWuPalmer.get(windowSize) + simInBlockWuPalmer.get(windowSize));
									totalSimInBlockPathSim.put(windowSize, totalSimInBlockPathSim.get(windowSize) + simInBlockPathSim.get(windowSize));
									
									rowLSA.append(",,,");
									rowLDA.append(",,,");
									rowLeacock.append(",,,");
									rowWuPalmer.append(",,,");
									rowPathSim.append(",,,");
									
									rowLSA.append(normSimDetectedLSA.get(windowSize) + ",");
									rowLDA.append(normSimDetectedLDA.get(windowSize) + ",");
									rowLeacock.append(normSimDetectedLeacock.get(windowSize) + ",");
									rowWuPalmer.append(normSimDetectedWuPalmer.get(windowSize) + ",");
									rowPathSim.append(normSimDetectedPathSim.get(windowSize) + ",");
									
									rowLSA.append(normSimInBlockLSA.get(windowSize) + ",");
									rowLDA.append(normSimInBlockLDA.get(windowSize) + ",");
									rowLeacock.append(normSimInBlockLeacock.get(windowSize) + ",");
									rowWuPalmer.append(normSimInBlockWuPalmer.get(windowSize) + ",");
									rowPathSim.append(normSimInBlockPathSim.get(windowSize) + ",");
									
									totalNormSimDetectedLSA.put(windowSize, totalNormSimDetectedLSA.get(windowSize) + normSimDetectedLSA.get(windowSize));
									totalNormSimDetectedLDA.put(windowSize, totalNormSimDetectedLDA.get(windowSize) + normSimDetectedLDA.get(windowSize));
									totalNormSimDetectedLeacock.put(windowSize, totalNormSimDetectedLeacock.get(windowSize) + normSimDetectedLeacock.get(windowSize));
									totalNormSimDetectedWuPalmer.put(windowSize, totalNormSimDetectedWuPalmer.get(windowSize) + normSimDetectedWuPalmer.get(windowSize));
									totalNormSimDetectedPathSim.put(windowSize, totalNormSimDetectedPathSim.get(windowSize) + normSimDetectedPathSim.get(windowSize));
									
									totalNormSimInBlockLSA.put(windowSize, totalNormSimInBlockLSA.get(windowSize) + normSimInBlockLSA.get(windowSize));
									totalNormSimInBlockLDA.put(windowSize, totalNormSimInBlockLDA.get(windowSize) + normSimInBlockLDA.get(windowSize));
									totalNormSimInBlockLeacock.put(windowSize, totalNormSimInBlockLeacock.get(windowSize) + normSimInBlockLeacock.get(windowSize));
									totalNormSimInBlockWuPalmer.put(windowSize, totalNormSimInBlockWuPalmer.get(windowSize) + normSimInBlockWuPalmer.get(windowSize));
									totalNormSimInBlockPathSim.put(windowSize, totalNormSimInBlockPathSim.get(windowSize) + normSimInBlockPathSim.get(windowSize));
									
									rowLSA.append(",,,");
									rowLDA.append(",,,");
									rowLeacock.append(",,,");
									rowWuPalmer.append(",,,");
									rowPathSim.append(",,,");
									
									rowLSA.append(mihalceaSimDetectedLSA.get(windowSize) + ",");
									rowLDA.append(mihalceaSimDetectedLDA.get(windowSize) + ",");
									rowLeacock.append(mihalceaSimDetectedLeacock.get(windowSize) + ",");
									rowWuPalmer.append(mihalceaSimDetectedWuPalmer.get(windowSize) + ",");
									rowPathSim.append(mihalceaSimDetectedPathSim.get(windowSize) + ",");
									
									rowLSA.append(mihalceaSimInBlockLSA.get(windowSize) + ",");
									rowLDA.append(mihalceaSimInBlockLDA.get(windowSize) + ",");
									rowLeacock.append(mihalceaSimInBlockLeacock.get(windowSize) + ",");
									rowWuPalmer.append(mihalceaSimInBlockWuPalmer.get(windowSize) + ",");
									rowPathSim.append(mihalceaSimInBlockPathSim.get(windowSize) + ",");
									
									totalMihalceaSimDetectedLSA.put(windowSize, totalMihalceaSimDetectedLSA.get(windowSize) + mihalceaSimDetectedLSA.get(windowSize));
									totalMihalceaSimDetectedLDA.put(windowSize, totalMihalceaSimDetectedLDA.get(windowSize) + mihalceaSimDetectedLDA.get(windowSize));
									totalMihalceaSimDetectedLeacock.put(windowSize, totalMihalceaSimDetectedLeacock.get(windowSize) + mihalceaSimDetectedLeacock.get(windowSize));
									totalMihalceaSimDetectedWuPalmer.put(windowSize, totalMihalceaSimDetectedWuPalmer.get(windowSize) + mihalceaSimDetectedWuPalmer.get(windowSize));
									totalMihalceaSimDetectedPathSim.put(windowSize, totalMihalceaSimDetectedPathSim.get(windowSize) + mihalceaSimDetectedPathSim.get(windowSize));
									
									totalMihalceaSimInBlockLSA.put(windowSize, totalMihalceaSimInBlockLSA.get(windowSize) + mihalceaSimInBlockLSA.get(windowSize));
									totalMihalceaSimInBlockLDA.put(windowSize, totalMihalceaSimInBlockLDA.get(windowSize) + mihalceaSimInBlockLDA.get(windowSize));
									totalMihalceaSimInBlockLeacock.put(windowSize, totalMihalceaSimInBlockLeacock.get(windowSize) + mihalceaSimInBlockLeacock.get(windowSize));
									totalMihalceaSimInBlockWuPalmer.put(windowSize, totalMihalceaSimInBlockWuPalmer.get(windowSize) + mihalceaSimInBlockWuPalmer.get(windowSize));
									totalMihalceaSimInBlockPathSim.put(windowSize, totalMihalceaSimInBlockPathSim.get(windowSize) + mihalceaSimInBlockPathSim.get(windowSize));
								}
								
								rowLSA.append('\n');
								rowLDA.append('\n');
								rowLeacock.append('\n');
								rowWuPalmer.append('\n');
								rowPathSim.append('\n');
								
							}

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
			
			StringBuilder rowLSA = new StringBuilder();
			StringBuilder rowLDA = new StringBuilder();
			StringBuilder rowLeacock = new StringBuilder();
			StringBuilder rowWuPalmer = new StringBuilder();
			StringBuilder rowPathSim = new StringBuilder();
			
			// append totals
			rowLSA.append(",,,");
			rowLDA.append(",,,");
			rowLeacock.append(",,,");
			rowWuPalmer.append(",,,");
			rowPathSim.append(",,,");
			for (int i = 0; i < maxWindowSize; i++) {
				rowLSA.append(",,,");
				rowLDA.append(",,,");
				rowLeacock.append(",,,");
				rowWuPalmer.append(",,,");
				rowPathSim.append(",,,");
			}
			rowLSA.append(",,,,");
			rowLDA.append(",,,,");
			rowLeacock.append(",,,,");
			rowWuPalmer.append(",,,,");
			rowPathSim.append(",,,,");
			
			for(Integer windowSize : windowSizes) {
			
				rowLSA.append(",,,");
				rowLDA.append(",,,");
				rowLeacock.append(",,,");
				rowWuPalmer.append(",,,");
				rowPathSim.append(",,,");
				
				rowLSA.append(totalSimDetectedLSA.get(windowSize) + ",");
				rowLDA.append(totalSimDetectedLDA.get(windowSize) + ",");
				rowLeacock.append(totalSimDetectedLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalSimDetectedWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalSimDetectedPathSim.get(windowSize) + ",");
				
				rowLSA.append(totalSimInBlockLSA.get(windowSize) + ",");
				rowLDA.append(totalSimInBlockLDA.get(windowSize) + ",");
				rowLeacock.append(totalSimInBlockLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalSimInBlockWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalSimInBlockPathSim.get(windowSize) + ",");
				
				rowLSA.append(",,,");
				rowLDA.append(",,,");
				rowLeacock.append(",,,");
				rowWuPalmer.append(",,,");
				rowPathSim.append(",,,");
				
				rowLSA.append(totalNormSimDetectedLSA.get(windowSize) + ",");
				rowLDA.append(totalNormSimDetectedLDA.get(windowSize) + ",");
				rowLeacock.append(totalNormSimDetectedLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalNormSimDetectedWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalNormSimDetectedPathSim.get(windowSize) + ",");
				
				rowLSA.append(totalNormSimInBlockLSA.get(windowSize) + ",");
				rowLDA.append(totalNormSimInBlockLDA.get(windowSize) + ",");
				rowLeacock.append(totalNormSimInBlockLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalNormSimInBlockWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalNormSimInBlockPathSim.get(windowSize) + ",");
				
				rowLSA.append(",,,");
				rowLDA.append(",,,");
				rowLeacock.append(",,,");
				rowWuPalmer.append(",,,");
				rowPathSim.append(",,,");
				
				rowLSA.append(totalMihalceaSimDetectedLSA.get(windowSize) + ",");
				rowLDA.append(totalMihalceaSimDetectedLDA.get(windowSize) + ",");
				rowLeacock.append(totalMihalceaSimDetectedLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalMihalceaSimDetectedWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalMihalceaSimDetectedPathSim.get(windowSize) + ",");
				
				rowLSA.append(totalMihalceaSimInBlockLSA.get(windowSize) + ",");
				rowLDA.append(totalMihalceaSimInBlockLDA.get(windowSize) + ",");
				rowLeacock.append(totalMihalceaSimInBlockLeacock.get(windowSize) + ",");
				rowWuPalmer.append(totalMihalceaSimInBlockWuPalmer.get(windowSize) + ",");
				rowPathSim.append(totalMihalceaSimInBlockPathSim.get(windowSize) + ",");
			
			}
			
			rowLSA.append('\n');
			rowLDA.append('\n');
			rowLeacock.append('\n');
			rowWuPalmer.append('\n');
			rowPathSim.append('\n');
			
			FileUtils.writeStringToFile(fileLSA, rowLSA.toString(), "UTF-8", true);
			FileUtils.writeStringToFile(fileLDA, rowLDA.toString(), "UTF-8", true);
			FileUtils.writeStringToFile(fileLeacock, rowLeacock.toString(), "UTF-8", true);
			FileUtils.writeStringToFile(fileWuPalmer, rowWuPalmer.toString(), "UTF-8", true);
			FileUtils.writeStringToFile(filePathSim, rowPathSim.toString(), "UTF-8", true);
			
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

		LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_lak_en", Lang.eng);
		LDA lda = LDA.loadLDA("resources/config/LDA/tasa_lak_en", Lang.eng);
		
		List<Integer> windowSizes = new ArrayList<Integer>();
		windowSizes.add(20);
		windowSizes.add(10);
		windowSizes.add(5);
		windowSizes.add(3);

		CSCLContributionSimilarities corpusSample = new CSCLContributionSimilarities("resources/in/corpus_v2/",
				"resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.getLang("English"), true, true,
				windowSizes, lsa, lda);
		corpusSample.process();
	}
}
