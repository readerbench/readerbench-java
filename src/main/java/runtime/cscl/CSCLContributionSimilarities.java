package runtime.cscl;

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
	private double threshold = 0.3;
	private int windowSize = 20;
	private LSA lsa;
	private LDA lda;

	public CSCLContributionSimilarities(String path, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, boolean computeDialogism, double threshold, int windowSize, LSA lsa, LDA lda) {
		this.path = path;
		this.pathToLSA = pathToLSA;
		this.pathToLDA = pathToLDA;
		this.lang = lang;
		this.usePOSTagging = usePOSTagging;
		this.computeDialogism = computeDialogism;
		this.threshold = threshold;
		this.windowSize = windowSize;
		this.lsa = lsa;
		this.lda = lda;
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
		capTabel.append("max_sim,"); // max sim
		capTabel.append("max_sim_id,"); // id of max sim utt
		capTabel.append("max_sim_participant,"); // participant of max sim utt
		capTabel.append("max_sim_ref_detected,"); // ref detected
		capTabel.append("max_sim_ref_in_block,"); // ref in block
		capTabel.append("max_sim_norm,"); // max sim normalized - how ?? (based
											// on distance i guess)
		capTabel.append("max_sim_norm_id,"); // id of max_sim_norm utt
		capTabel.append("max_sim_norm_participant,"); // participant of
														// max_sim_norm utt
		capTabel.append("max_sim_norm_ref_detected,"); // ref detected
		capTabel.append("max_sim_norm_ref_in_block,"); // ref in block
		capTabel.append("mihalcea_sim,"); // Mihalcea's similarity
		capTabel.append("mihalcea_sim_id,"); // id of Mihalcea's similarity utt
		capTabel.append("mihalcea_sim_participant,"); // participant of
														// Mihalcea's similarity
														// utt
		capTabel.append("mihalcea_sim_ref_detected,"); // ref detected
		capTabel.append("mihalcea_sim_ref_in_block,"); // ref in block
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

							rowLSA.append(firstUtt.getParticipant().getName() + ",");
							rowLDA.append(firstUtt.getParticipant().getName() + ",");
							rowLeacock.append(firstUtt.getParticipant().getName() + ",");
							rowWuPalmer.append(firstUtt.getParticipant().getName() + ",");
							rowPathSim.append(firstUtt.getParticipant().getName() + ",");

							int k = 0;
							double maxLSA = -1;
							double maxLDA = -1;
							double maxLeacock = -1;
							double maxWuPalmer = -1;
							double maxPathSim = -1;
							int refMaxLSA = -1;
							int refMaxLDA = -1;
							int refMaxLeacock = -1;
							int refMaxWuPalmer = -1;
							int refMaxPathSim = -1;
							String participantMaxLSA = null;
							String participantMaxLDA = null;
							String participantMaxLeacock = null;
							String participantMaxWuPalmer = null;
							String participantMaxPathSim = null;

							double normMaxLSA = -1;
							double normMaxLDA = -1;
							double normMaxLeacock = -1;
							double normMaxWuPalmer = -1;
							double normMaxPathSim = -1;
							int refNormMaxLSA = -1;
							int refNormMaxLDA = -1;
							int refNormMaxLeacock = -1;
							int refNormMaxWuPalmer = -1;
							int refNormMaxPathSim = -1;
							String participantNormMaxLSA = null;
							String participantNormMaxLDA = null;
							String participantNormMaxLeacock = null;
							String participantNormMaxWuPalmer = null;
							String participantNormMaxPathSim = null;

							double mihalceaMaxLSA = -1;
							double mihalceaMaxLDA = -1;
							double mihalceaMaxLeacock = -1;
							double mihalceaMaxWuPalmer = -1;
							double mihalceaMaxPathSim = -1;
							int refMihalceaMaxLSA = -1;
							int refMihalceaMaxLDA = -1;
							int refMihalceaMaxLeacock = -1;
							int refMihalceaMaxWuPalmer = -1;
							int refMihalceaMaxPathSim = -1;
							String participantMihalceaMaxLSA = null;
							String participantMihalceaMaxLDA = null;
							String participantMihalceaMaxLeacock = null;
							String participantMihalceaMaxWuPalmer = null;
							String participantMihalceaMaxPathSim = null;

							for (int j = i - 1; j >= i - windowSize && j > 0; j--) {
								secondUtt = (Utterance) c.getBlocks().get(j);
								if (secondUtt != null) {

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
									Iterator itFirstUtt = firstUtt.getWordOccurences().entrySet().iterator();
									while (itFirstUtt.hasNext()) {
										Map.Entry pairFirstUtt = (Map.Entry) itFirstUtt.next();
										// System.out.println(pair.getKey() + "
										// = " + pair.getValue());
										Word wordFirstUtt = (Word) pairFirstUtt.getKey();
										Integer occWordFirstUtt = (Integer) pairFirstUtt.getValue();

										// iterate through words of second
										// sentence
										double maxSimForWordWithOtherUttLSA = 0;
										double maxSimForWordWithOtherUttLDA = 0;
										double maxSimForWordWithOtherUttLeacock = 0;
										double maxSimForWordWithOtherUttWuPalmer = 0;
										double maxSimForWordWithOtherUttPathSim = 0;
										Iterator itSecondUtt = secondUtt.getWordOccurences().entrySet().iterator();
										while (itSecondUtt.hasNext()) {
											Map.Entry pairSecondUtt = (Map.Entry) itSecondUtt.next();
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
										Map.Entry pairFirstUtt = (Map.Entry) itFirstUtt.next();
										// System.out.println(pair.getKey() + "
										// = " + pair.getValue());
										Word wordFirstUtt = (Word) pairFirstUtt.getKey();
										Integer occWordFirstUtt = (Integer) pairFirstUtt.getValue();

										// iterate through words of second
										// sentence
										double maxSimForWordWithOtherUttLSA = 0;
										double maxSimForWordWithOtherUttLDA = 0;
										double maxSimForWordWithOtherUttLeacock = 0;
										double maxSimForWordWithOtherUttWuPalmer = 0;
										double maxSimForWordWithOtherUttPathSim = 0;
										Iterator itSecondUtt = firstUtt.getWordOccurences().entrySet().iterator();
										while (itSecondUtt.hasNext()) {
											Map.Entry pairSecondUtt = (Map.Entry) itSecondUtt.next();
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

									sim = sc.getLSASim();
									if (maxLSA < sim) {
										maxLSA = sim;
										refMaxLSA = secondUtt.getIndex();
										participantMaxLSA = secondUtt.getParticipant().getName();
									}
									rowLSA.append(Formatting.formatNumber(sim) + ",");

									double normSim = sim / (i - j + 1);
									if (normMaxLSA < normSim) {
										normMaxLSA = normSim;
										refNormMaxLSA = secondUtt.getIndex();
										participantNormMaxLSA = secondUtt.getParticipant().getName();
									}
									rowLSA.append(Formatting.formatNumber(normSim) + ",");

									sim = .5 * (((leftHandSideDownLSA > 0) ? (leftHandSideUpLSA / leftHandSideDownLSA)
											: 0)
											+ ((rightHandSideDownLSA > 0) ? (rightHandSideUpLSA / rightHandSideDownLSA)
													: 0));
									if (mihalceaMaxLSA < sim) {
										mihalceaMaxLSA = sim;
										refMihalceaMaxLSA = secondUtt.getIndex();
										participantMihalceaMaxLSA = secondUtt.getParticipant().getName();
									}
									rowLSA.append(Formatting.formatNumber(sim) + ",");

									sim = sc.getLDASim();
									if (maxLDA < sim) {
										maxLDA = sim;
										refMaxLDA = secondUtt.getIndex();
										participantMaxLDA = secondUtt.getParticipant().getName();
									}
									rowLDA.append(Formatting.formatNumber(sim) + ",");

									normSim = sim / (i - j + 1);
									if (normMaxLDA < normSim) {
										normMaxLDA = normSim;
										refNormMaxLDA = secondUtt.getIndex();
										participantNormMaxLDA = secondUtt.getParticipant().getName();
									}
									rowLDA.append(Formatting.formatNumber(normSim) + ",");

									sim = .5 * (((leftHandSideDownLDA > 0) ? (leftHandSideUpLDA / leftHandSideDownLDA)
											: 0)
											+ ((rightHandSideDownLDA > 0) ? (rightHandSideUpLDA / rightHandSideDownLDA)
													: 0));
									if (mihalceaMaxLDA < sim) {
										mihalceaMaxLDA = sim;
										refMihalceaMaxLDA = secondUtt.getIndex();
										participantMihalceaMaxLDA = secondUtt.getParticipant().getName();
									}
									rowLDA.append(Formatting.formatNumber(sim) + ",");

									sim = sc.getOntologySim().get(SimilarityType.LEACOCK_CHODOROW);
									// sim =
									// OntologySupport.semanticSimilarity(firstUtt,
									// secondUtt,
									// SimilarityType.LEACOCK_CHODOROW);
									if (maxLeacock < sim) {
										maxLeacock = sim;
										refMaxLeacock = secondUtt.getIndex();
										participantMaxLeacock = secondUtt.getParticipant().getName();
									}
									rowLeacock.append(Formatting.formatNumber(sim) + ",");

									normSim = sim / (i - j + 1);
									if (normMaxLeacock < normSim) {
										normMaxLeacock = normSim;
										refNormMaxLeacock = secondUtt.getIndex();
										participantNormMaxLeacock = secondUtt.getParticipant().getName();
									}
									rowLeacock.append(Formatting.formatNumber(normSim) + ",");

									sim = .5 * (((leftHandSideDownLeacock > 0)
											? (leftHandSideUpLeacock / leftHandSideDownLeacock) : 0)
											+ ((rightHandSideDownLeacock > 0)
													? (rightHandSideUpLeacock / rightHandSideDownLeacock) : 0));
									if (mihalceaMaxLeacock < sim) {
										mihalceaMaxLeacock = sim;
										refMihalceaMaxLeacock = secondUtt.getIndex();
										participantMihalceaMaxLeacock = secondUtt.getParticipant().getName();
									}
									rowLeacock.append(Formatting.formatNumber(sim) + ",");

									sim = sc.getOntologySim().get(SimilarityType.WU_PALMER);
									if (maxWuPalmer < sim) {
										maxWuPalmer = sim;
										refMaxWuPalmer = secondUtt.getIndex();
										participantMaxWuPalmer = secondUtt.getParticipant().getName();
									}
									rowWuPalmer.append(Formatting.formatNumber(sim) + ",");

									normSim = sim / (i - j + 1);
									if (normMaxWuPalmer < normSim) {
										normMaxWuPalmer = normSim;
										refNormMaxWuPalmer = secondUtt.getIndex();
										participantNormMaxWuPalmer = secondUtt.getParticipant().getName();
									}
									rowWuPalmer.append(Formatting.formatNumber(normSim) + ",");

									sim = .5 * (((leftHandSideDownWuPalmer > 0)
											? (leftHandSideUpWuPalmer / leftHandSideDownWuPalmer) : 0)
											+ ((rightHandSideDownWuPalmer > 0)
													? (rightHandSideUpWuPalmer / rightHandSideDownWuPalmer) : 0));
									if (mihalceaMaxWuPalmer < sim) {
										mihalceaMaxWuPalmer = sim;
										refMihalceaMaxWuPalmer = secondUtt.getIndex();
										participantMihalceaMaxWuPalmer = secondUtt.getParticipant().getName();
									}
									rowWuPalmer.append(Formatting.formatNumber(sim) + ",");

									sim = sc.getOntologySim().get(SimilarityType.PATH_SIM);
									if (maxPathSim < sim) {
										maxPathSim = sim;
										refMaxPathSim = secondUtt.getIndex();
										participantMaxPathSim = secondUtt.getParticipant().getName();
									}
									rowPathSim.append(Formatting.formatNumber(sim) + ",");

									normSim = sim / (i - j + 1);
									if (normMaxPathSim < normSim) {
										normMaxPathSim = normSim;
										refNormMaxPathSim = secondUtt.getIndex();
										participantNormMaxPathSim = secondUtt.getParticipant().getName();
									}
									rowPathSim.append(Formatting.formatNumber(normSim) + ",");

									sim = .5 * (((leftHandSideDownPathSim > 0)
											? (leftHandSideUpPathSim / leftHandSideDownPathSim) : 0)
											+ ((rightHandSideDownPathSim > 0)
													? (rightHandSideUpPathSim / rightHandSideDownPathSim) : 0));
									if (mihalceaMaxPathSim < sim) {
										mihalceaMaxPathSim = sim;
										refMihalceaMaxPathSim = secondUtt.getIndex();
										participantMihalceaMaxPathSim = secondUtt.getParticipant().getName();
									}
									rowPathSim.append(Formatting.formatNumber(sim) + ",");

									k++;
								}
							}
							for (int j = k; j < windowSize; j++) {
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
							String refParticipantName = null;
							if (firstUtt.getRefBlock() != null && firstUtt.getRefBlock().getIndex() != 0) {
								Utterance refUtt = (Utterance) c.getBlocks().get(firstUtt.getRefBlock().getIndex());
								if (refUtt != null) {
									// referred utterance id
									refId = refUtt.getIndex();
									refParticipantName = refUtt.getParticipant().getName();
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

							boolean isInBlock;
							boolean atLeastOne;
							Integer minRef;
							Integer maxRef;
							minRef = 0;
							maxRef = new Double(Double.POSITIVE_INFINITY).intValue();

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

							// max sim participant name
							rowLSA.append(((participantMaxLSA != null) ? participantMaxLSA : "") + ",");
							rowLDA.append(((participantMaxLDA != null) ? participantMaxLDA : "") + ",");
							rowLeacock.append(((participantMaxLeacock != null) ? participantMaxLeacock : "") + ",");
							rowWuPalmer.append(((participantMaxWuPalmer != null) ? participantMaxWuPalmer : "") + ",");
							rowPathSim.append(((participantMaxPathSim != null) ? participantMaxPathSim : "") + ",");

							// ref detected?
							rowLSA.append(((refId == refMaxLSA) ? 1 : 0) + ",");
							rowLDA.append(((refId == refMaxLDA) ? 1 : 0) + ",");
							rowLeacock.append(((refId == refMaxLeacock) ? 1 : 0) + ",");
							rowWuPalmer.append(((refId == refMaxWuPalmer) ? 1 : 0) + ",");
							rowPathSim.append(((refId == refMaxPathSim) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMaxLSA);
							maxRef = (int) Math.max(refId, refMaxLSA);
							isInBlock = false;

							if (minRef != -1) {
								isInBlock = true;
								if (minRef == maxRef)
									isInBlock = true;
								if (!((Utterance) c.getBlocks().get(minRef)).getParticipant().getName()
										.equals(((Utterance) c.getBlocks().get(maxRef)).getParticipant().getName()))
									isInBlock = false;
								else {
									for (int j = minRef + 1; j <= maxRef - 1; j++) {
										secondUtt = (Utterance) c.getBlocks().get(j);
										if (secondUtt != null) {
											if (secondUtt.getParticipant() == null || !secondUtt.getParticipant()
													.getName().equals(refParticipantName)) {
												isInBlock = false;
												break;
											}
										}
									}
								}
							}
							// logger.info("Este in block ? " + ((isInBlock ==
							// true) ? "DA" : "NU"));
							// if (!atLeastOne) isInBlock = false;
							// logger.info("Este inca block ? " + ((isInBlock ==
							// true) ? "DA" : "NU"));
							rowLSA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMaxLDA);
							maxRef = (int) Math.max(refId, refMaxLDA);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											atLeastOne = true;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMaxLeacock);
							maxRef = (int) Math.max(refId, refMaxLeacock);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMaxWuPalmer);
							maxRef = (int) Math.max(refId, refMaxWuPalmer);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMaxPathSim);
							maxRef = (int) Math.max(refId, refMaxPathSim);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");

							// max (sim normalized)
							rowLSA.append(normMaxLSA + ",");
							rowLDA.append(normMaxLDA + ",");
							rowLeacock.append(normMaxLeacock + ",");
							rowWuPalmer.append(normMaxWuPalmer + ",");
							rowPathSim.append(normMaxPathSim + ",");

							// id of max (sim normalized)
							rowLSA.append(refNormMaxLSA + ",");
							rowLDA.append(refNormMaxLDA + ",");
							rowLeacock.append(refNormMaxLeacock + ",");
							rowWuPalmer.append(refNormMaxWuPalmer + ",");
							rowPathSim.append(refNormMaxPathSim + ",");

							// max sim participant name
							rowLSA.append(((participantNormMaxLSA != null) ? participantNormMaxLSA : "") + ",");
							rowLDA.append(((participantNormMaxLDA != null) ? participantNormMaxLDA : "") + ",");
							rowLeacock.append(
									((participantNormMaxLeacock != null) ? participantNormMaxLeacock : "") + ",");
							rowWuPalmer.append(
									((participantNormMaxWuPalmer != null) ? participantNormMaxWuPalmer : "") + ",");
							rowPathSim.append(
									((participantNormMaxPathSim != null) ? participantNormMaxPathSim : "") + ",");

							// ref detected?
							rowLSA.append(((refId == refNormMaxLSA) ? 1 : 0) + ",");
							rowLDA.append(((refId == refNormMaxLDA) ? 1 : 0) + ",");
							rowLeacock.append(((refId == refNormMaxLeacock) ? 1 : 0) + ",");
							rowWuPalmer.append(((refId == refNormMaxWuPalmer) ? 1 : 0) + ",");
							rowPathSim.append(((refId == refNormMaxPathSim) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refNormMaxLSA);
							maxRef = (int) Math.max(refId, refNormMaxLSA);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLSA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refNormMaxLDA);
							maxRef = (int) Math.max(refId, refNormMaxLDA);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refNormMaxLeacock);
							maxRef = (int) Math.max(refId, refNormMaxLeacock);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refNormMaxWuPalmer);
							maxRef = (int) Math.max(refId, refNormMaxWuPalmer);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refNormMaxPathSim);
							maxRef = (int) Math.max(refId, refNormMaxPathSim);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");

							// Mihalcea's similarity
							rowLSA.append(mihalceaMaxLSA + ",");
							rowLDA.append(mihalceaMaxLDA + ",");
							rowLeacock.append(mihalceaMaxLeacock + ",");
							rowWuPalmer.append(mihalceaMaxWuPalmer + ",");
							rowPathSim.append(mihalceaMaxPathSim + ",");

							// id of max (Mihalcea's similarity)
							rowLSA.append(refMihalceaMaxLSA + ",");
							rowLDA.append(refMihalceaMaxLDA + ",");
							rowLeacock.append(refMihalceaMaxLeacock + ",");
							rowWuPalmer.append(refMihalceaMaxWuPalmer + ",");
							rowPathSim.append(refMihalceaMaxPathSim + ",");

							// max sim participant name (Mihalcea's similarity)
							rowLSA.append(((participantMihalceaMaxLSA != null) ? participantMihalceaMaxLSA : "") + ",");
							rowLDA.append(((participantMihalceaMaxLDA != null) ? participantMihalceaMaxLDA : "") + ",");
							rowLeacock.append(
									((participantMihalceaMaxLeacock != null) ? participantMihalceaMaxLeacock : "")
											+ ",");
							rowWuPalmer.append(
									((participantMihalceaMaxWuPalmer != null) ? participantMihalceaMaxWuPalmer : "")
											+ ",");
							rowPathSim.append(
									((participantMihalceaMaxPathSim != null) ? participantMihalceaMaxPathSim : "")
											+ ",");

							// ref detected?
							rowLSA.append(((refId == refMihalceaMaxLSA) ? 1 : 0) + ",");
							rowLDA.append(((refId == refMihalceaMaxLDA) ? 1 : 0) + ",");
							rowLeacock.append(((refId == refMihalceaMaxLeacock) ? 1 : 0) + ",");
							rowWuPalmer.append(((refId == refMihalceaMaxWuPalmer) ? 1 : 0) + ",");
							rowPathSim.append(((refId == refMihalceaMaxPathSim) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMihalceaMaxLSA);
							maxRef = (int) Math.max(refId, refMihalceaMaxLSA);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLSA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMihalceaMaxLDA);
							maxRef = (int) Math.max(refId, refMihalceaMaxLDA);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLDA.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMihalceaMaxLeacock);
							maxRef = (int) Math.max(refId, refMihalceaMaxLeacock);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowLeacock.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMihalceaMaxWuPalmer);
							maxRef = (int) Math.max(refId, refMihalceaMaxWuPalmer);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowWuPalmer.append(((isInBlock == true) ? 1 : 0) + ",");

							minRef = (int) Math.min(refId, refMihalceaMaxPathSim);
							maxRef = (int) Math.max(refId, refMihalceaMaxPathSim);
							isInBlock = true;
							if (minRef != -1)
								for (int j = minRef; j <= maxRef; j++) {
									secondUtt = (Utterance) c.getBlocks().get(j);
									if (secondUtt != null) {
										if (secondUtt.getParticipant() == null || secondUtt.getParticipant().getName()
												.compareTo(refParticipantName) != 0) {
											isInBlock = false;
											break;
										}
									}
								}
							else
								isInBlock = false;
							rowPathSim.append(((isInBlock == true) ? 1 : 0) + ",");

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

		LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_lak_en", Lang.eng);
		LDA lda = LDA.loadLDA("resources/config/LDA/tasa_lak_en", Lang.eng);

		CSCLContributionSimilarities corpusSample = new CSCLContributionSimilarities("resources/in/corpus_v2_sample/",
				"resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.getLang("English"), true, true,
				0.3, 20, lsa, lda);
		corpusSample.process();
	}
}
