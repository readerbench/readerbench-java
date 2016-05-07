package data.cscl;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocument.SaveType;
import data.AnalysisElement;
import data.Block;
import data.Word;
import data.discourse.Topic;
import data.Lang;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.complexity.discourse.ConnectivesComplexity;
import services.complexity.discourse.DiscourseComplexity;
import services.complexity.discourse.SemanticCohesionComplexity;
import services.complexity.entityDensity.EntityDensityComplexity;
import services.complexity.surface.EntropyComplexity;
import services.complexity.surface.LengthComplexity;
import services.complexity.surface.SurfaceStatisticsComplexity;
import services.complexity.syntax.POSComplexity;
import services.complexity.syntax.PronounsComplexity;
import services.complexity.syntax.TreeComplexity;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.topicMining.TopicModeling;
import services.replicatedWorker.SerialCorpusAssessment;
import view.widgets.cscl.ParticipantInteractionView;
import view.widgets.document.corpora.PaperConceptView;

public class Community extends AnalysisElement {
	private static final long serialVersionUID = 2836361816092262953L;
	private static final int MIN_NO_CONTRIBUTIONS = 3;
	private static final int MIN_NO_CONTENT_WORDS = 50;

	static Logger logger = Logger.getLogger(Community.class);

	private List<Participant> participants;
	private List<Conversation> documents;
	private List<Community> timeframeSubCommunities;
	private double[][] participantContributions;
	private Date startDate, endDate;
	private Date fistContributionDate, lastContributionDate;
	private boolean[] selectedIndices;

	public Community(Date startDate, Date endDate) {
		super(null, 0, null, null, null);
		this.startDate = startDate;
		this.endDate = endDate;
		participants = new ArrayList<Participant>();
		documents = new ArrayList<Conversation>();
		timeframeSubCommunities = new ArrayList<Community>();
	}

	private void updateParticipantContributions() {
		for (Conversation c : documents) {
			// update the community correspondingly
			for (Participant p : c.getParticipants()) {
				int index = participants.indexOf(p);
				Participant participantToUpdate = null;
				if (index >= 0) {
					participantToUpdate = participants.get(index);
				} else {
					participantToUpdate = new Participant(p.getName(), c);
					participants.add(participantToUpdate);
				}

				for (Block b : p.getInterventions().getBlocks()) {
					Utterance u = (Utterance) b;
					// select contributions in imposed timeframe
					if (u != null && u.isEligible(startDate, endDate)) {
						// determine first timestamp of considered contributions
						if (fistContributionDate == null)
							fistContributionDate = u.getTime();
						if (u.getTime().before(fistContributionDate))
							fistContributionDate = u.getTime();
						Calendar date = new GregorianCalendar(2010, Calendar.JANUARY, 1);
						if (u.getTime().before(date.getTime()))
							System.err.println(
									"Incorrect time!!! " + c.getPath() + " / " + u.getIndex() + " : " + u.getTime());
						if (u.getTime().after(new Date()))
							System.err.println(
									"Incorrect time!!! " + c.getPath() + " / " + u.getIndex() + " : " + u.getTime());

						if (lastContributionDate == null)
							lastContributionDate = u.getTime();
						if (u.getTime().after(lastContributionDate))
							lastContributionDate = u.getTime();
						b.setIndex(-1);
						Block.addBlock(participantToUpdate.getInterventions(), b);
						if (b.isSignificant()) {
							Block.addBlock(participantToUpdate.getSignificantInterventions(), b);
						}

						participantToUpdate.getIndices().put(CSCLIndices.NO_CONTRIBUTION,
								participantToUpdate.getIndices().get(CSCLIndices.NO_CONTRIBUTION) + 1);

						for (Entry<Word, Integer> entry : u.getWordOccurences().entrySet()) {
							if (entry.getKey().getPOS() != null) {
								if (entry.getKey().getPOS().startsWith("N"))
									participantToUpdate.getIndices().put(CSCLIndices.NO_NOUNS,
											participantToUpdate.getIndices().get(CSCLIndices.NO_NOUNS)
													+ entry.getValue());
								if (entry.getKey().getPOS().startsWith("V"))
									participantToUpdate.getIndices().put(CSCLIndices.NO_VERBS,
											participantToUpdate.getIndices().get(CSCLIndices.NO_VERBS)
													+ entry.getValue());
							}
						}

						int noSelectedTopics = 0;
						for (Topic topic : c.getTopics()) {
							if (topic.getWord().getPOS() == null || (topic.getWord().getPOS().startsWith("N")
									|| topic.getWord().getPOS().startsWith("V"))) {
								noSelectedTopics++;
								if (u.getWordOccurences().containsKey(topic.getWord())) {
									double relevance = u.getWordOccurences().get(topic.getWord())
											* topic.getRelevance();
									participantToUpdate.getIndices().put(CSCLIndices.RELEVANCE_TOP10_TOPICS,
											participantToUpdate.getIndices().get(CSCLIndices.RELEVANCE_TOP10_TOPICS)
													+ relevance);
								}
								if (noSelectedTopics == 10)
									break;
							}
						}
					}
				}
			}
		}

		participantContributions = new double[participants.size()][participants.size()];

		for (Conversation d : documents) {
			// determine strength of links
			for (int i = 0; i < d.getBlocks().size(); i++) {
				Utterance u = (Utterance) d.getBlocks().get(i);
				// select contributions in imposed timeframe
				if (u != null && u.isEligible(startDate, endDate)) {
					Participant p1 = u.getParticipant();
					int index1 = participants.indexOf(p1);
					if (index1 >= 0) {
						// participantContributions[index1][index1] += d
						// .getBlocks().get(i).getCombinedScore();
						Participant participantToUpdate = participants.get(index1);
						participantToUpdate.getIndices().put(CSCLIndices.OVERALL_SCORE,
								participantToUpdate.getIndices().get(CSCLIndices.OVERALL_SCORE) + u.getOverallScore());
						participantToUpdate.getIndices().put(CSCLIndices.PERSONAL_KB,
								participantToUpdate.getIndices().get(CSCLIndices.PERSONAL_KB) + u.getPersonalKB());
						participantToUpdate.getIndices().put(CSCLIndices.SOCIAL_KB,
								participantToUpdate.getIndices().get(CSCLIndices.SOCIAL_KB) + u.getSocialKB());

						for (int j = 0; j < i; j++) {
							if (d.getPrunnedBlockDistances()[i][j] != null) {
								Participant p2 = ((Utterance) d.getBlocks().get(j)).getParticipant();
								int index2 = participants.indexOf(p2);
								if (index2 >= 0) {
									// model knowledge building effect
									double addedKB = d.getBlocks().get(i).getIndividualScore()
											* d.getPrunnedBlockDistances()[i][j].getCohesion();
									participantContributions[index1][index2] += addedKB;
								}
							}
						}
					}
				}
			}
			for (Participant p : d.getParticipants()) {
				Participant participantToUpdate = participants.get(participants.indexOf(p));
				participantToUpdate.getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
						participantToUpdate.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
								+ p.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE));
			}
		}
	}

	protected Set<Participant> extractArrayListfromSet(List<Participant> community) {
		Set<Participant> ls = new TreeSet<Participant>();
		for (Participant p : community) {
			ls.add(p);
		}
		return ls;
	}

	public void computeMetrics(boolean useTextualComplexity, boolean modelTimeEvolution) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		if (startDate != null && endDate != null && participants != null && participants.size() > 0)
			logger.info("Processing timeframe between " + dateFormat.format(startDate) + " and "
					+ dateFormat.format(endDate) + " having " + participants.size() + " participants.");

		if (startDate != null && endDate != null)
			ParticipantEvaluation.performSNA(participants, participantContributions, true,
					"out/graph_" + dateFormat.format(startDate) + "_" + dateFormat.format(endDate) + ".pdf");
		else
			ParticipantEvaluation.performSNA(participants, participantContributions, true,
					"out/graph_" + System.currentTimeMillis() + ".pdf");
		// update surface statistics
		for (AbstractDocument d : documents) {
			Participant p = null;
			for (int i = 0; i < d.getBlocks().size(); i++) {
				if (d.getBlocks().get(i) != null) {
					if (p == null) {
						p = ((Utterance) d.getBlocks().get(i)).getParticipant();
						Participant participantToUpdate = participants.get(participants.indexOf(p));
						participantToUpdate.getIndices().put(CSCLIndices.NO_NEW_THREADS,
								participantToUpdate.getIndices().get(CSCLIndices.NO_NEW_THREADS) + 1);
						participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_OVERALL_SCORE,
								participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_OVERALL_SCORE)
										+ d.getOverallScore());
						participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB,
								participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB)
										+ VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()));
						participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE,
								participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE)
										+ VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()));
						participantToUpdate.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
								participantToUpdate.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
										+ d.getBlocks().get(i).getText().length());
						break;
					}
				}
			}
		}

		for (Participant p : participants) {
			if (p.getIndices().get(CSCLIndices.NO_NEW_THREADS) != 0)
				p.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
						p.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
								/ p.getIndices().get(CSCLIndices.NO_NEW_THREADS));
		}

		if (useTextualComplexity) {
			selectedIndices = new boolean[ComplexityIndices.NO_COMPLEXITY_INDICES];

			IComplexityFactors[] complexityFactors = { new LengthComplexity(), new SurfaceStatisticsComplexity(),
					new EntropyComplexity(), new POSComplexity(), new PronounsComplexity(), new TreeComplexity(),
					new EntityDensityComplexity(), new ConnectivesComplexity(getLanguage()), new DiscourseComplexity(),
					new SemanticCohesionComplexity(1), new SemanticCohesionComplexity(3),
					new SemanticCohesionComplexity(4) };
			for (IComplexityFactors f : complexityFactors) {
				for (int index : f.getIDs()) {
					selectedIndices[index] = true;
				}
			}

			// determine complexity indices
			for (Participant p : participants) {
				// establish minimum criteria
				int noContentWords = 0;
				for (Block b : p.getSignificantInterventions().getBlocks()) {
					if (b != null) {
						for (Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
							noContentWords += entry.getValue();
						}
					}
				}
				p.getSignificantInterventions()
						.setComplexityIndices(new double[ComplexityIndices.NO_COMPLEXITY_INDICES]);

				if (p.getSignificantInterventions().getBlocks().size() >= MIN_NO_CONTRIBUTIONS
						&& noContentWords >= MIN_NO_CONTENT_WORDS) {
					// build cohesion graph for additional indices
					CohesionGraph.buildCohesionGraph(p.getSignificantInterventions());

					for (IComplexityFactors f : complexityFactors) {
						f.computeComplexityFactors(p.getSignificantInterventions());
					}
				}
			}
		}

		if (modelTimeEvolution) {
			modelEvolution();
		}
	}

	public void modelEvolution() {
		logger.info("Modeling time evolution for " + participants.size() + " participants");
		for (CSCLIndices index : CSCLIndices.values()) {
			if (index.isUsedForTimeModeling()) {
				logger.info("Modeling based on " + index.getDescription());
				int no = 0;
				for (Participant p : participants) {
					// model time evolution of each participant
					double[] values = new double[timeframeSubCommunities.size()];
					for (int i = 0; i < timeframeSubCommunities.size(); i++) {
						int localParticipantIndex = timeframeSubCommunities.get(i).getParticipants().indexOf(p);
						if (localParticipantIndex != -1)
							values[i] = timeframeSubCommunities.get(i).getParticipants().get(localParticipantIndex)
									.getIndices().get(index);
					}
					if (++no % 100 == 0)
						logger.info("Finished evaluating the time evolution of " + no + " participants");
					for (CSCLCriteria crit : CSCLCriteria.values()) {
						p.getLongitudinalIndices().put(
								new AbstractMap.SimpleEntry<CSCLIndices, CSCLCriteria>(index, crit),
								CSCLCriteria.getValue(crit, values));
					}
				}
			}
		}
	}

	private static Community getSubCommunity(Community community, Date startSubCommunities, Date endSubCommunities) {
		Community subCommunity = new Community(startSubCommunities, endSubCommunities);
		for (Conversation c : community.getDocuments())
			subCommunity.getDocuments().add(c);
		subCommunity.updateParticipantContributions();
		subCommunity.computeMetrics(false, false);
		return subCommunity;
	}

	public static Community loadMultipleConversations(String rootPath, Date startDate, Date endDate, int monthIncrement,
			int dayIncrement) {
		logger.info("Loading all files in " + rootPath);

		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".ser");
			}
		};

		Community community = new Community(startDate, endDate);
		File dir = new File(rootPath);
		if (!dir.isDirectory())
			return null;
		File[] filesTODO = dir.listFiles(filter);
		for (File f : filesTODO) {
			Conversation c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
			if (c != null)
				community.getDocuments().add(c);
		}

		community.updateParticipantContributions();

		// create corresponding sub-communities
		Calendar cal = Calendar.getInstance();
		Date startSubCommunities = community.getFistContributionDate();
		cal.setTime(startSubCommunities);
		cal.add(Calendar.MONTH, monthIncrement);
		cal.add(Calendar.DATE, dayIncrement);
		Date endSubCommunities = cal.getTime();

		while (endSubCommunities.before(community.getLastContributionDate())) {
			community.getTimeframeSubCommunities()
					.add(getSubCommunity(community, startSubCommunities, endSubCommunities));

			// update timeStamps
			startSubCommunities = endSubCommunities;
			cal.add(Calendar.MONTH, monthIncrement);
			cal.add(Calendar.DATE, dayIncrement);
			endSubCommunities = cal.getTime();
		}
		// create partial community with remaining contributions
		community.getTimeframeSubCommunities()
				.add(getSubCommunity(community, startSubCommunities, community.getLastContributionDate()));

		logger.info("Finished creating " + community.getTimeframeSubCommunities().size()
				+ " timeframe sub-communities spanning from " + community.getFistContributionDate() + " to "
				+ community.getLastContributionDate());

		return community;
	}

	public void generateParticipantView(final String path) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ParticipantInteractionView view = new ParticipantInteractionView("Member", path, participants,
						participantContributions, true, true);
				view.setVisible(true);
			}
		});
	}

	public void generateConceptView(final String path) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				PaperConceptView conceptView = new PaperConceptView(TopicModeling.getCollectionTopics(documents), path);
				conceptView.setVisible(true);
			}
		});
	}

	public void export(String pathToFile) {
		try {
			logger.info("Writing document collection export");
			File output = new File(pathToFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);

			// print participant statistics
			if (participants.size() > 0) {
				out.write("Participant involvement and interaction\n");
				out.write("Participant name,Anonymized name");
				for (CSCLIndices CSCLindex : CSCLIndices.values())
					out.write("," + CSCLindex.getDescription() + "(" + CSCLindex.getAcronym() + ")");
				for (CSCLIndices CSCLindex : CSCLIndices.values()) {
					if (CSCLindex.isUsedForTimeModeling()) {
						for (CSCLCriteria crit : CSCLCriteria.values()) {
							out.write("," + crit.getDescription() + "(" + CSCLindex.getAcronym() + ")");
						}
					}
				}
				if (selectedIndices != null) {
					for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
						if (selectedIndices[i]) {
							out.write("," + ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[i]);
						}
					}
				}
				out.write("\n");

				for (int index = 0; index < participants.size(); index++) {
					Participant p = participants.get(index);
					out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index);
					for (CSCLIndices CSCLindex : CSCLIndices.values())
						out.write("," + Formatting.formatNumber(p.getIndices().get(CSCLindex)));
					for (CSCLIndices CSCLindex : CSCLIndices.values()) {
						if (CSCLindex.isUsedForTimeModeling()) {
							for (CSCLCriteria crit : CSCLCriteria.values()) {
								out.write("," + p.getLongitudinalIndices()
										.get(new AbstractMap.SimpleEntry<CSCLIndices, CSCLCriteria>(CSCLindex, crit)));
							}
						}
					}
					if (selectedIndices != null) {
						for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
							if (selectedIndices[i]) {
								out.write("," + Formatting
										.formatNumber(p.getSignificantInterventions().getComplexityIndices()[i]));
							}
						}
					}
					out.write("\n");
				}

				// print discussed topics
				out.write("\nDiscussed topics\n");
				out.write("Concept,Relevance\n");
				List<Topic> topicL = new ArrayList<Topic>();
				Iterator<Map.Entry<Word, Double>> mapIter = TopicModeling.getCollectionTopics(documents).entrySet()
						.iterator();
				while (mapIter.hasNext()) {
					Map.Entry<Word, Double> entry = mapIter.next();
					topicL.add(new Topic(entry.getKey(), entry.getValue()));
				}
				Collections.sort(topicL);
				for (Topic t : topicL) {
					out.write(t.getWord().getLemma() + "," + t.getRelevance() + "\n");
				}

				// print general statistic per thread
				out.write("\nIndividual thread statistics\n");
				out.write(
						"Thread path,No. contributions,No. involved paticipants,Overall score,Cummulative inter-animation,Cummulative social knowledge-building\n");
				for (AbstractDocument d : documents) {
					int noBlocks = 0;
					for (Block b : d.getBlocks()) {
						if (b != null) {
							noBlocks++;
						}
					}

					out.write(
							new File(d.getPath()).getName() + "," + noBlocks + ","
									+ ((Conversation) d).getParticipants().size() + ","
									+ Formatting.formatNumber(d.getOverallScore()) + ","
									+ Formatting.formatNumber(
											VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()))
									+ ","
									+ Formatting.formatNumber(
											VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()))
									+ "\n");
				}

				// print interaction matrix
				out.write("\nInteraction matrix\n");
				for (Participant p : participants)
					out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
				out.write("\n");
				for (int i = 0; i < participants.size(); i++) {
					out.write(participants.get(i).getName().replaceAll(",", "").replaceAll("\\s+", " "));
					for (int j = 0; j < participants.size(); j++)
						out.write("," + Formatting.formatNumber(participantContributions[i][j]));
					out.write("\n");
				}
			}

			out.close();
			logger.info("Successfully finished writing document collection export");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void processDocumentCollection(String rootPath, boolean useTextualComplexity, Date startDate,
			Date endDate, int monthIncrement, int dayIncrement) {
		Community dc = Community.loadMultipleConversations(rootPath, startDate, endDate, monthIncrement, dayIncrement);
		if (dc != null) {
			dc.computeMetrics(useTextualComplexity, true);
			File f = new File(rootPath);
			dc.export(rootPath + "/" + f.getName() + ".csv");
			dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
			dc.generateConceptView(rootPath + "/" + f.getName() + "_concepts.pdf");
		}
	}

	public static void processAllFolders(String folder, String prefix, boolean restartProcessing, String pathToLSA,
			String pathToLDA, Lang lang, boolean usePOSTagging, boolean useTextualComplexity, Date startDate,
			Date endDate, int monthIncrement, int dayIncrement) {
		File dir = new File(folder);

		if (dir.isDirectory()) {
			File[] communityFolder = dir.listFiles();
			for (File f : communityFolder) {
				if (f.isDirectory() && f.getName().startsWith(prefix)) {
					if (restartProcessing) {
						// remove checkpoint file
						File checkpoint = new File(f.getPath() + "/checkpoint.xml");
						if (checkpoint.exists())
							checkpoint.delete();
					}
					SerialCorpusAssessment.processCorpus(f.getAbsolutePath(), pathToLSA, pathToLDA, lang, usePOSTagging,
							true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);
					Community.processDocumentCollection(f.getAbsolutePath(), useTextualComplexity, startDate, endDate,
							monthIncrement, dayIncrement);
				}
			}
		}
		System.out.println("Finished processsing all files...");
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participant> community) {
		this.participants = community;
	}

	public List<Conversation> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Conversation> documents) {
		this.documents = documents;
	}

	public Date getFistContributionDate() {
		return fistContributionDate;
	}

	public void setFistContributionDate(Date fistContributionDate) {
		this.fistContributionDate = fistContributionDate;
	}

	public Date getLastContributionDate() {
		return lastContributionDate;
	}

	public void setLastContributionDate(Date lastContributionDate) {
		this.lastContributionDate = lastContributionDate;
	}

	public List<Community> getTimeframeSubCommunities() {
		return timeframeSubCommunities;
	}

	public void setTimeframeSubCommunities(List<Community> timeframeSubCommunities) {
		this.timeframeSubCommunities = timeframeSubCommunities;
	}
}
