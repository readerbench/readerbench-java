package DAO.cscl;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import DAO.AbstractDocument;
import DAO.AnalysisElement;
import DAO.Block;
import DAO.Word;
import DAO.discourse.Topic;
import edu.cmu.lti.jawjaw.pobj.Lang;
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
import view.widgets.chat.ParticipantInvolvementView;
import view.widgets.document.corpora.PaperConceptView;

public class Community extends AnalysisElement {
	private static final long serialVersionUID = 2836361816092262953L;
	private static final int MIN_NO_CONTRIBUTIONS = 3;
	private static final int MIN_NO_CONTENT_WORDS = 50;

	static Logger logger = Logger.getLogger(Community.class);

	private List<Participant> community;
	private List<Conversation> documents;
	private double[][] participantContributions;
	private Date startDate;
	private Date endDate;
	private boolean[] selectedIndices;

	public Community(Date startDate, Date endDate) {
		super(null, 0, null, null, null);
		this.startDate = startDate;
		this.endDate = endDate;
		community = new LinkedList<Participant>();
		documents = new LinkedList<Conversation>();
	}

	private void updateParticipantContributions() {
		for (Conversation c : documents) {
			// update the community correspondingly
			for (Participant p : c.getParticipants()) {
				int index = community.indexOf(p);
				Participant participantToUpdate = null;
				if (index >= 0) {
					participantToUpdate = community.get(index);
				} else {
					participantToUpdate = new Participant(p.getName(), c);
					community.add(participantToUpdate);
				}

				for (Block b : p.getInterventions().getBlocks()) {
					Utterance u = (Utterance) b;
					// select contributions in imposed timeframe
					if (u != null && u.isEligible(startDate, endDate)) {
						b.setIndex(-1);
						Block.addBlock(participantToUpdate.getInterventions(), b);
						if (b.isSignificant()) {
							Block.addBlock(participantToUpdate.getSignificantInterventions(), b);
						}

						participantToUpdate.setNoContributions(participantToUpdate.getNoContributions() + 1);

						for (Entry<Word, Integer> entry : u.getWordOccurences().entrySet()) {
							if (entry.getKey().getPOS() != null) {
								if (entry.getKey().getPOS().startsWith("N"))
									participantToUpdate.setNoNouns(participantToUpdate.getNoNouns() + entry.getValue());
								if (entry.getKey().getPOS().startsWith("V"))
									participantToUpdate.setNoVerbs(participantToUpdate.getNoVerbs() + entry.getValue());
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
									participantToUpdate.setRelevanceTop10Topics(
											participantToUpdate.getRelevanceTop10Topics() + relevance);
								}
								if (noSelectedTopics == 10)
									break;
							}
						}
					}
				}
			}
		}

		participantContributions = new double[community.size()][community.size()];

		for (Participant p : community) {
			p.resetIndices();
		}

		for (Conversation d : documents) {
			// determine strength of links
			for (int i = 0; i < d.getBlocks().size(); i++) {
				Utterance u = (Utterance) d.getBlocks().get(i);
				// select contributions in imposed timeframe
				if (u != null && u.isEligible(startDate, endDate)) {
					Participant p1 = u.getParticipant();
					int index1 = community.indexOf(p1);
					if (index1 >= 0) {
						// participantContributions[index1][index1] += d
						// .getBlocks().get(i).getCombinedScore();
						Participant participantToUpdate = community.get(index1);
						participantToUpdate
								.setOverallScore(participantToUpdate.getOverallScore() + u.getCombinedScore());
						participantToUpdate.setPersonalKB(participantToUpdate.getPersonalKB() + u.getPersonalKB());
						participantToUpdate.setSocialKB(participantToUpdate.getSocialKB() + u.getSocialKB());

						for (int j = i + 1; j < d.getBlocks().size(); j++) {
							if (d.getPrunnedBlockDistances()[j][i] != null) {
								Participant p2 = ((Utterance) d.getBlocks().get(j)).getParticipant();
								int index2 = community.indexOf(p2);
								if (index2 >= 0) {
									// model knowledge building effect
									double addedKB = d.getBlocks().get(j).getCombinedScore()
											* d.getPrunnedBlockDistances()[j][i].getCohesion();
									participantContributions[index2][index1] += addedKB;

									addedKB = d.getBlocks().get(i).getCombinedScore()
											* d.getPrunnedBlockDistances()[j][i].getCohesion();
									participantContributions[index1][index2] += addedKB;
								}
							}
						}
					}
				}
			}
			for (Participant p : d.getParticipants()) {
				Participant pToUpdate = community.get(community.indexOf(p));
				pToUpdate.setDegreeInterAnimation(pToUpdate.getDegreeInterAnimation() + p.getDegreeInterAnimation());
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

	public void computeMetrics() {
		selectedIndices = new boolean[ComplexityIndices.NO_COMPLEXITY_INDICES];

		IComplexityFactors[] complexityFactors = { new LengthComplexity(), new SurfaceStatisticsComplexity(),
				new EntropyComplexity(), new POSComplexity(), new PronounsComplexity(), new TreeComplexity(),
				new EntityDensityComplexity(), new ConnectivesComplexity(), new DiscourseComplexity(),
				new SemanticCohesionComplexity(1), new SemanticCohesionComplexity(3),
				new SemanticCohesionComplexity(4) };
		for (IComplexityFactors f : complexityFactors) {
			for (int index : f.getIDs()) {
				selectedIndices[index] = true;
			}
		}

		// determine complexity indices
		for (Participant p : community) {
			// establish minimum criteria
			int noContentWords = 0;
			for (Block b : p.getSignificantInterventions().getBlocks()) {
				if (b != null) {
					for (Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
						noContentWords += entry.getValue();
					}
				}
			}
			p.getSignificantInterventions().setComplexityIndices(new double[ComplexityIndices.NO_COMPLEXITY_INDICES]);

			if (p.getSignificantInterventions().getBlocks().size() >= MIN_NO_CONTRIBUTIONS
					&& noContentWords >= MIN_NO_CONTENT_WORDS) {
				// build cohesion graph for additional indices

				CohesionGraph.buildCohesionGraph(p.getSignificantInterventions());
				for (IComplexityFactors f : complexityFactors) {
					f.computeComplexityFactors(p.getSignificantInterventions());
				}
			}
		}

		ParticipantEvaluation.performSNA(community, participantContributions, true);

		// update surface statistics
		for (AbstractDocument d : documents) {
			Participant p = null;
			for (int i = 0; i < d.getBlocks().size(); i++) {
				if (d.getBlocks().get(i) != null) {
					if (p == null) {
						p = ((Utterance) d.getBlocks().get(i)).getParticipant();
						Participant participantToUpdate = community.get(community.indexOf(p));
						participantToUpdate.setNoNewThreads(participantToUpdate.getNoNewThreads() + 1);
						participantToUpdate.setNewThreadsOverallScore(
								participantToUpdate.getNewThreadsOverallScore() + d.getOverallScore());
						participantToUpdate.setNewThreadsCumulativeInteranimation(
								participantToUpdate.getNewThreadsCumulativeInteranimation()
										+ VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()));
						participantToUpdate
								.setNewThreadsCumulativeSocialKB(participantToUpdate.getNewThreadsCumulativeSocialKB()
										+ VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()));
						participantToUpdate.setAverageNewThreadsLength(participantToUpdate.getAverageNewThreadsLength()
								+ d.getBlocks().get(i).getText().length());
					}
				}
			}
		}

		for (Participant p : community) {
			if (p.getNoNewThreads() != 0)
				p.setAverageNewThreadsLength(p.getAverageNewThreadsLength() / p.getNoNewThreads());
		}
	}

	public void generateParticipantView(final String path) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ParticipantInvolvementView view = new ParticipantInvolvementView("Member", path, community,
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
			if (community.size() > 0) {
				out.write("Participant involvement and interaction\n");
				out.write(
						"Participant name,Annonized name,No. contributions,Qualitative overall score,Overall personal KB,Overall social KB,Cummulative degree of voice inter-animation"
								+ ",Indegree,Outdegree,Betweenness,Closeness,Eccentricity,Coverage of top 10 topics (nouns and verbs only),Number of nouns, Number of verbs"
								+ ",No of new threads started,Average length of started threads,Overall score of initiated threads"
								+ ",Cummulative social knowledge-building for initiated threads,Cummulative inter-animation for initiated threads");
				for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
					if (selectedIndices[i]) {
						out.write("," + ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_NAMES[i]);
					}
				}
				out.write("\n");

				for (int index = 0; index < community.size(); index++) {
					Participant p = community.get(index);
					out.write(
							p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ",Member " + index + ","
									+ p.getNoContributions() + ","
									+ Formatting.formatNumber(p.getOverallScore()) + "," + Formatting.formatNumber(
											p.getPersonalKB())
									+ "," + Formatting.formatNumber(p.getSocialKB()) + ","
									+ Formatting.formatNumber(p.getDegreeInterAnimation()) + ","
									+ Formatting.formatNumber(p.getIndegree()) + ","
									+ Formatting.formatNumber(p.getOutdegree()) + ","
									+ Formatting.formatNumber(p.getBetweenness()) + ","
									+ Formatting.formatNumber(p.getCloseness()) + ","
									+ Formatting.formatNumber(p.getEccentricity()) + ","
									+ Formatting.formatNumber(p.getRelevanceTop10Topics()) + ","
									+ Formatting.formatNumber(p.getNoNouns()) + ","
									+ Formatting.formatNumber(p.getNoVerbs()) + ","
									+ Formatting.formatNumber(p.getNoNewThreads()) + ","
									+ Formatting.formatNumber(p.getAverageNewThreadsLength()) + ","
									+ Formatting.formatNumber(p.getNewThreadsOverallScore()) + ","
									+ Formatting.formatNumber(p.getNewThreadsCumulativeSocialKB()) + ","
									+ Formatting.formatNumber(p.getNewThreadsCumulativeInteranimation()));
					for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
						if (selectedIndices[i]) {
							out.write("," + Formatting
									.formatNumber(p.getSignificantInterventions().getComplexityIndices()[i]));
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
							new File(d.getPath()).getName() + "," + noBlocks + "," + ((Conversation) d).getParticipants().size()
									+ "," + Formatting.formatNumber(d.getOverallScore()) + ","
									+ Formatting.formatNumber(
											VectorAlgebra.sumElements(((Conversation) d).getVoicePMIEvolution()))
									+ "," + Formatting.formatNumber(
											VectorAlgebra.sumElements(((Conversation) d).getSocialKBEvolution()))
									+ "\n");
				}

				// print interaction matrix
				out.write("\nInteraction matrix\n");
				for (Participant p : community)
					out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
				out.write("\n");
				for (int i = 0; i < community.size(); i++) {
					out.write(community.get(i).getName().replaceAll(",", "").replaceAll("\\s+", " "));
					for (int j = 0; j < community.size(); j++)
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

	public static Community loadMultipleConversations(String rootPath, Date startDate, Date endDate) {
		logger.info("Loading all files in " + rootPath);

		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".ser");
			}
		};

		Community collection = new Community(startDate, endDate);
		File dir = new File(rootPath);
		if (!dir.isDirectory())
			return null;
		File[] filesTODO = dir.listFiles(filter);
		for (File f : filesTODO) {
			Conversation c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
			// d.exportIM();
			if (c != null)
				collection.getDocuments().add(c);
		}

		collection.updateParticipantContributions();

		return collection;
	}

	public static void processDocumentCollection(String rootPath, Date startDate, Date endDate) {
		Community dc = Community.loadMultipleConversations(rootPath, startDate, endDate);
		if (dc != null) {
			dc.computeMetrics();
			File f = new File(rootPath);
			dc.export(rootPath + "/" + f.getName() + ".csv");
			dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
			dc.generateConceptView(rootPath + "/" + f.getName() + "_concepts.pdf");
		}
	}

	public static void processAllFolders(String folder, String prefix, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, Date startDate, Date endDate) {
		File dir = new File(folder);
		if (dir.isDirectory()) {
			File[] communityFolder = dir.listFiles();
			for (File f : communityFolder) {
				if (f.isDirectory() && f.getName().startsWith(prefix)) {
					// remove checkpoint file
					// File checkpoint = new File(f + "/checkpoint.xml");
					// if (checkpoint.exists())
					// checkpoint.delete();
					SerialCorpusAssessment.processCorpus(f.getAbsolutePath(), pathToLSA, pathToLDA, lang, usePOSTagging,
							true, null, null, true);
					Community.processDocumentCollection(f.getAbsolutePath(), startDate, endDate);
				}

			}
		}
	}

	public List<Participant> getCommunity() {
		return community;
	}

	public void setCommunity(List<Participant> community) {
		this.community = community;
	}

	public List<Conversation> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Conversation> documents) {
		this.documents = documents;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		// processAllFolders("in/blogs_Nic/1 year", "PW", "config/LSA/tasa_en",
		// "config/LDA/tasa_en", Lang.eng, true, null,
		// null);
		String path = "in/MOOC/forum_posts&comments";
		SerialCorpusAssessment.processCorpus(path, "config/LSA/tasa_lak_en", "config/LDA/tasa_lak_en", Lang.eng, true,
				true, null, null, true);
		Long startDate = 1383235200L;
		// Long startDate = 1383843600L;
		Community.processDocumentCollection(path, new Date(startDate * 1000), null);
	}
}
