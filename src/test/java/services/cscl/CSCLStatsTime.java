package services.cscl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import data.Block;
import data.cscl.Conversation;
import data.cscl.Utterance;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;

public class CSCLStatsTime {

	public static Logger logger = Logger.getLogger(CSCLStatsTime.class);

	private static String conversationsPath = "resources/in/corpus_v2/";
	// private static String conversationsPath = "resources/in/corpus_chats/";
	private static int no_references = 0;

	public static void main(String[] args) {

		//Map<String, TimeStats> timeStatsPerChat = new HashMap<String, TimeStats>();
		Map<Integer, TimeStats> timeStatsGlobal = new TreeMap<Integer, TimeStats>();

		try {
			Files.walk(Paths.get(CSCLStatsTime.conversationsPath)).forEach(filePath -> {
				String filePathString = filePath.toString();
				if (filePathString.contains("in.xml")) {
					// if (filePathString.contains(".xml")) {

					logger.info("Processing file " + filePath.getFileName().toString());

					Conversation c = Conversation.load(
							filePathString,
							"resources/config/LSA/tasa_en",
							"resources/config/LDA/tasa_en",
							Lang.eng, false, true);
					c.computeAll(true, null, null, true);

					Utterance firstUtt = null;
					for (int i = 1; i < c.getBlocks().size(); i++) {
						firstUtt = (Utterance) c.getBlocks().get(i);
						if (firstUtt != null)
							break;
					}
					Utterance lastUtt = null;
					for (int i = c.getBlocks().size() - 1; i > 0; i--) {
						lastUtt = (Utterance) c.getBlocks().get(i);
						if (lastUtt != null)
							break;
					}

					int timp = 0;
					if (firstUtt != null && lastUtt != null) {
						// add 24 hours if last utterance's time is lower then first utterance's time (midnight passed)
						if (firstUtt.getTime().after(lastUtt.getTime())) {
							DateUtils.addHours(lastUtt.getTime(), 24);
						}
						timp = (int) getDateDiff(firstUtt.getTime(), lastUtt.getTime(), TimeUnit.SECONDS);
					}
					// save conversation info
					timeStatsGlobal.put(
							//filePath.getFileName().toString(),
							timp,
							new TimeStats(
									0, // references
									0, // same speaker references
									0 // different speaker references
					));

					logger.info("Conversation has " + c.getBlocks().size() + " blocks.");

					for (int i = 0; i < c.getBlocks().size(); i++) {
						Block block1 = c.getBlocks().get(i);
						if (block1 != null) {
							Utterance utterance1 = (Utterance) block1;
							logger.info("Processing contribution " + block1.getText());
							if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
								Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
								if (block2 != null) {
									
									// count new reference
									no_references++;
									Utterance utterance2 = (Utterance) block2;
									timeStatsGlobal.get(timp).setExplicitLinks(
											timeStatsGlobal.get(timp).getExplicitLinks() + 1);
									logger.info("Processing refered contribution " + block2.getText());
									//int distance = getBlockDistance(block1, block2);

									// global information for the conversation
									// corpus
									if (utterance1.getParticipant() == utterance2.getParticipant()) {
										timeStatsGlobal.get(timp).setSameSpeaker(timeStatsGlobal.get(timp).getSameSpeaker() + 1);
									}
									else {
										timeStatsGlobal.get(timp).setDifferentSpeaker(timeStatsGlobal.get(timp).getDifferentSpeaker() + 1);
									}
								}
							}
						}
					}

					//logger.info("Printing contribution distances for chat " + c.getPath());
					//logger.info("Max distance for chat: " + blockDistances.size());
					/*Iterator it = blockDistances.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry) it.next();
						logger.info(pair.getKey() + " = " + pair.getValue());
					}*/

				}

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Printing final contribution times for conversations.");
		//logger.info("Max times for all conersations: " + Collections.max(timeStatsGlobal.values()));
		/*Iterator it = blockDistances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			logger.info(pair.getKey() + " = " + pair.getValue());
		}*/

		//printTimesToCSVFile(timeStatsGlobal, no_references);
		printConversationStatsToCSVFile(timeStatsGlobal);

	}

	private static void printConversationStatsToCSVFile(Map<Integer, TimeStats> timeStatsGlobal) {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(
					"sep=,\ntime,explicit links,same speaker,different speaker\n");

			Iterator it = timeStatsGlobal.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				TimeStats cs = (TimeStats) pair.getValue();
				sb.append(
						pair.getKey() + "," +
						cs.getExplicitLinks() + "," +
						cs.getSameSpeaker() + "," +
						cs.getDifferentSpeaker()
				);

				sb.append("\n");
			}

			File file = new File(conversationsPath + "time_stats.csv");
			try {
				FileUtils.writeStringToFile(file, sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("Printed conversation time stats to CSV file: " + file.getAbsolutePath());

		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

	}

	/*private static void printDistancesToCSVFile(Map<Integer, DistanceStatsNew> blockDistances, int no_references) {
		// String prependPath =
		// "/Users/Berilac/Projects/Eclipse/readerbench/resources/";

		try {

			StringBuilder sb = new StringBuilder();
			sb.append("sep=,\ndistance,total,same speaker,different speaker,%,same speaker first,different speaker first\n");

			Iterator it = blockDistances.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				DistanceStatsNew ds = (DistanceStatsNew) pair.getValue();
				sb.append(
						pair.getKey() + "," +
						ds.getTotal() + ", " +
						ds.getSameSpeaker() + "," +
						ds.getDifferentSpeaker() + "," +
						(ds.getTotal() / no_references) + "," +
						ds.getSameSpeakerFirst() + ", " + 
						ds.getDifferentSpeakerFirst()
				);
				sb.append("\n");
			}

			File file = new File(conversationsPath + "distances.csv");
			try {
				FileUtils.writeStringToFile(file, sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("Printed distances to CSV file: " + file.getAbsolutePath());

		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

	}*/

	private static int getBlockDistance(Block block1, Block block2) {
		return Math.abs(block2.getIndex() - block1.getIndex());
	}

	private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

}

class TimeStats {

	private int explicitLinks;
	private int sameSpeaker;
	private int differentSpeaker;
	
	public TimeStats(int explicitLinks, int sameSpeaker, int differentSpeaker) {
		super();
		this.explicitLinks = explicitLinks;
		this.sameSpeaker = sameSpeaker;
		this.differentSpeaker = differentSpeaker;
	}
	public int getExplicitLinks() {
		return explicitLinks;
	}
	public void setExplicitLinks(int explicitLinks) {
		this.explicitLinks = explicitLinks;
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
	
	

}