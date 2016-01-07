package services.discourse.CSCL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.Interval;

import data.Block;
import data.cscl.Conversation;
import data.cscl.Utterance;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import view.widgets.ReaderBenchView;

public class ConversationProcessing {
	
	public static Logger logger = Logger.getLogger(ConversationProcessing.class);
	
	private static int WINDOW_SIZE = 20;
	private static String conversationsPath = "resources/in/corpus_v2/";
	//private static String conversationsPath = "resources/in/corpus_chats/";

	public static void main(String[] args) {
		
		Map<Integer, DistanceStats> blockDistances = new HashMap<Integer, DistanceStats>();
		Map<String, ChatStats> chatStats = new HashMap<String, ChatStats>();
		
		try {
			Files.walk(Paths.get(ConversationProcessing.conversationsPath)).forEach(filePath -> {
				String filePathString = filePath.toString();
				if (filePathString.contains("in.xml")) {
				//if (filePathString.contains(".xml")) {
				
					logger.info("Processing file " + filePath.getFileName().toString());
				
					Conversation c = Conversation.load(filePathString, "resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.eng, false,
						true);
					
					Utterance firstUtt = null;
					for (int i = 1; i < c.getBlocks().size(); i++) {
						firstUtt = (Utterance)c.getBlocks().get(i);
						if (firstUtt != null) break;
					}
					Utterance lastUtt = null;
					for (int i = c.getBlocks().size() - 1; i > 0; i--) {
						lastUtt = (Utterance)c.getBlocks().get(i);
						if (lastUtt != null) break;
					}
					
					int timp = 0;
					if (firstUtt != null && lastUtt != null) {
						timp = (int) getDateDiff(firstUtt.getTime(), lastUtt.getTime(),TimeUnit.MINUTES);
					}
					// save conversation info
					chatStats.put(filePath.getFileName().toString(), new ChatStats(
						c.getBlocks().size(), // contributions
						c.getParticipants().size(), // participants
						timp, // durations
						0, // explicitLinks
						0, // same block
						0, // coverage
						null // references
					));
					
					Map<Integer, Integer> references = new HashMap<Integer, Integer>();
					
					// first parameter = distance
					// second parameter = number of links
						
					logger.info("Conversation has " + c.getBlocks().size() + " blocks.");
					
					for (int i = 0; i < c.getBlocks().size(); i++) {
						Block block1 = c.getBlocks().get(i);
						if (block1 != null) {
							Utterance utterance1 = (Utterance) block1;
							logger.info("Processing contribution " + block1.getText());
							if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
								Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
								if (block2 != null) {
									Utterance utterance2 = (Utterance) block2;
									chatStats.get(filePath.getFileName().toString()).setExplicitLinks(chatStats.get(filePath.getFileName().toString()).getExplicitLinks() + 1);
									chatStats.get(filePath.getFileName().toString()).setCoverage((double) chatStats.get(filePath.getFileName().toString()).getExplicitLinks() / chatStats.get(filePath.getFileName().toString()).getContributions());
									logger.info("Processing refered contribution " + block2.getText());
								//for (int j = i - 20; j < i && j > 0 && (block2 = c.getBlocks().get(j)) != null && block2.getRefBlock() != null && block2.getRefBlock().getIndex() != 0; j++) {
									int distance = getBlockDistance(block1, block2);
									
									// global information for the conversation corpus
									if (blockDistances.get(distance) != null) {
										//blockDistances.put(distance, blockDistances.get(distance) + 1);
										DistanceStats ds = blockDistances.get(distance);
										ds.setTotal(ds.getTotal() + 1);
										if (utterance1.getParticipant() == utterance2.getParticipant()) {
											ds.setSameSpeaker(ds.getSameSpeaker() + 1);
											if (distance == 1) ds.setSameSpeakerFirst(ds.getSameSpeakerFirst() + 1);
											// check if same block
											boolean sameBlock = true;
											for (int k = block2.getIndex() + 1; k < block1.getIndex(); k++) {
												Utterance aux = (Utterance) c.getBlocks().get(k);
												if (aux.getParticipant() != utterance1.getParticipant()) {
													sameBlock = false;
													break;
												}
											}
											if (sameBlock) chatStats.get(filePath.getFileName().toString()).setSameBlock(chatStats.get(filePath.getFileName().toString()).getSameBlock() + 1);
											// end check if same block
										}
										else {
											ds.setDifferentSpeaker(ds.getDifferentSpeaker() + 1);
										}
										
									}
									else {
										DistanceStats ds;
										if (utterance1.getParticipant() == utterance2.getParticipant()) {
											ds = new DistanceStats(1, 1, 0, 0);
											if (distance == 1) ds.setSameSpeakerFirst(ds.getSameSpeakerFirst() + 1);
											boolean sameBlock = true;
											for (int k = block2.getIndex() + 1; k < block1.getIndex(); k++) {
												Utterance aux = (Utterance) c.getBlocks().get(k);
												if (aux.getParticipant() != utterance1.getParticipant()) {
													sameBlock = false;
													break;
												}
											}
											if (sameBlock) chatStats.get(filePath.getFileName().toString()).setSameBlock(chatStats.get(filePath.getFileName().toString()).getSameBlock() + 1);
										}
										else {
											ds = new DistanceStats(1, 0, 1, 0);
										}
										blockDistances.put(distance, ds);
									}
									
									// local information for the conversation file stats
									if (distance <= 5) {
										if (references.get(distance) != null) {
											references.put(distance, references.get(distance) + 1);
										}
										else {
											references.put(distance, 1);
										}
									}
								//}
								}
							}
						}
				    }
					
					chatStats.get(filePath.getFileName().toString()).setReferences(references);
				
					logger.info("Printing contribution distances for chat " + c.getPath());
					logger.info("Max distance for chat: " + blockDistances.size());
					Iterator it = blockDistances.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        logger.info(pair.getKey() + " = " + pair.getValue());
				    }
				    
				}
				
			});
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Printing final contribution distances for conversations.");
		logger.info("Max distance for all conersations: " + blockDistances.size());
		Iterator it = blockDistances.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        logger.info(pair.getKey() + " = " + pair.getValue());
	    }
	    
	    printDistancesToCSVFile(blockDistances);
	    printConversationStatsToCSVFile(chatStats);
	    
	}
	
	private static void printConversationStatsToCSVFile(Map<String, ChatStats> chatStats) {
		
		try {
			
				StringBuilder sb = new StringBuilder();
				sb.append("sep=,\nchat id,contrubtions,participants,duration,explicit links,same block,coverage,d1,d2,d3,d4,d5\n");
				
				Iterator it = chatStats.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        ChatStats cs = (ChatStats)pair.getValue();
			        sb.append(
			        	pair.getKey() + "," +
			        	cs.getContributions() + ", " +
			        	cs.getParticipants() + "," +
			        	cs.getDurations() + "," +
			        	cs.getExplicitLinks() + "," +
			        	cs.getSameBlock() + "," +
			        	Formatting.formatNumber(cs.getCoverage()) + ","
			        );
			        
			        logger.info("References for " + pair.getKey() + " file: " + cs.getReferences().size());
			        Iterator itReferences = cs.getReferences().entrySet().iterator();
			        while (itReferences.hasNext()) {
			        	Map.Entry pairReference = (Map.Entry)itReferences.next();
			        	sb.append(
				        	pairReference.getValue() + ","
				        );
			        }
			        
			        sb.append("\n");
			    }
				
			    File file = new File(conversationsPath + "stats.csv");
				try {
					FileUtils.writeStringToFile(file,sb.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Printed conversation stats to CSV file: " + file.getAbsolutePath());
			
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

	}
	
	private static void printDistancesToCSVFile(Map<Integer, DistanceStats> blockDistances) {
		//String prependPath = "/Users/Berilac/Projects/Eclipse/readerbench/resources/";
		
		try {
			
				StringBuilder sb = new StringBuilder();
				sb.append("sep=,\ndistance,total,same speaker,different speaker,same speaker first\n");
				
				Iterator it = blockDistances.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        DistanceStats ds = (DistanceStats)pair.getValue();
			        sb.append(
			        	pair.getKey() + "," +
			        	ds.getTotal() + ", " +
			        	ds.getSameSpeaker() + "," +
			        	ds.getDifferentSpeaker() + "," +
			        	ds.getSameSpeakerFirst()
			        );
			        sb.append("\n");
			    }
				
				File file = new File(conversationsPath + "distances.csv");
				try {
					FileUtils.writeStringToFile(file,sb.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Printed distances to CSV file: " + file.getAbsolutePath());
			
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

	}
	
	private static int getBlockDistance(Block block1, Block block2) {
		return Math.abs(block2.getIndex() - block1.getIndex());
	}
	
	private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}

}

class DistanceStats {
	
	private int total;
	private int sameSpeaker;
	private int differentSpeaker;
	private int sameSpeakerFirst;
	
	public DistanceStats(int total, int sameSpeaker, int differentSpeaker, int sameSpeakerFirst) {
		super();
		this.total = total;
		this.sameSpeaker = sameSpeaker;
		this.differentSpeaker = differentSpeaker;
		this.sameSpeakerFirst = sameSpeakerFirst;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getSameSpeaker() {
		return sameSpeaker;
	}

	public void setSameSpeaker(int sameSpeaker) {
		this.sameSpeaker = sameSpeaker;
	}

	public int getDifferentSpeaker() {
		return differentSpeaker;
	}

	public void setDifferentSpeaker(int differentSpeaker) {
		this.differentSpeaker = differentSpeaker;
	}

	public int getSameSpeakerFirst() {
		return sameSpeakerFirst;
	}

	public void setSameSpeakerFirst(int sameSpeakerFirst) {
		this.sameSpeakerFirst = sameSpeakerFirst;
	}
	
}

class ChatStats {
	
	private int contributions;
	private int participants;
	private int durations; // timestamp
	private int explicitLinks;
	private int sameBlock;
	private double coverage;
	private Map<Integer, Integer> references; // number of references to distance 1, 2, ... 5
	
	public ChatStats(int contributions, int participants, int durations, int explicitLinks,
			int sameBlock,
			double coverage,
			Map<Integer, Integer> references) {
		super();
		this.contributions = contributions;
		this.participants = participants;
		this.durations = durations;
		this.explicitLinks = explicitLinks;
		this.sameBlock = sameBlock;
		this.coverage = coverage;
		this.references = references;
	}
	
	public int getContributions() {
		return contributions;
	}
	
	public void setContributions(int contributions) {
		this.contributions = contributions;
	}
	
	public int getParticipants() {
		return participants;
	}
	
	public void setParticipants(int participants) {
		this.participants = participants;
	}
	
	public int getDurations() {
		return durations;
	}
	
	public void setDurations(int durations) {
		this.durations = durations;
	}
	
	public int getExplicitLinks() {
		return explicitLinks;
	}
	
	public void setExplicitLinks(int explicitLinks) {
		this.explicitLinks = explicitLinks;
	}
	
	public int getSameBlock() {
		return explicitLinks;
	}
	
	public void setSameBlock(int sameBlock) {
		this.sameBlock = sameBlock;
	}
	
	public double getCoverage() {
		return coverage;
	}
	
	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}
	
	public Map<Integer, Integer> getReferences() {
		return references;
	}
	
	public void setReferences(Map<Integer, Integer> references) {
		this.references = references;
	}
	
}