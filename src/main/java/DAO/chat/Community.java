package DAO.chat;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import services.discourse.topicMining.TopicCoverage;
import services.discourse.topicMining.TopicCoverage.TopicClass;
import services.discourse.topicMining.TopicModeling;
import view.widgets.chat.ParticipantInvolvementView;
import view.widgets.document.corpora.PaperConceptView;

public class Community extends AnalysisElement {
	private static final long serialVersionUID = 2836361816092262953L;
	private static final int MIN_NO_CONTRIBUTIONS = 3;
	private static final int MIN_NO_CONTENT_WORDS = 50;

	static Logger logger = Logger.getLogger(Community.class);

	private List<Participant> community;
	private List<AbstractDocument> documents;
	private double[][] participantContributions;
	private TopicClass specificityClass; // if specificity == null ignore
	private boolean[] selectedIndices;

	public Community(TopicClass specificityClass) {
		super(null, 0, null, null, null);
		this.specificityClass = specificityClass;
		community = new LinkedList<Participant>();
		documents = new LinkedList<AbstractDocument>();
	}

	private void updateParticipantContributions() {
		// determine the contributions and also indegree / outdegree for each
		// node
		participantContributions = new double[community.size()][community.size()];

		for (Participant p : community) {
			p.resetIndices();
		}

		// determine specificity for each block
		for (AbstractDocument d : documents) {
			for (Block b : d.getBlocks()) {
				if (b != null) {
					b.setSpecificity(TopicCoverage.coverage(b, specificityClass));
				}
			}
		}

		for (AbstractDocument d : documents) {
			// determine strength of links
			for (int i = 0; i < d.getBlocks().size(); i++) {
				if (d.getBlocks().get(i) != null) {
					Participant p1 = ((Utterance) d.getBlocks().get(i)).getParticipant();
					int index1 = community.indexOf(p1);
					if (index1 >= 0) {
						// participantContributions[index1][index1] += d
						// .getBlocks().get(i).getCombinedScore();
						Participant participantToUpdate = community.get(index1);
						participantToUpdate.setOverallScore(
								participantToUpdate.getOverallScore() + d.getBlocks().get(i).getCombinedScore());
						participantToUpdate.setPersonalKB(
								participantToUpdate.getPersonalKB() + ((Utterance) d.getBlocks().get(i)).getPersonalKB()
										* d.getBlocks().get(i).getSpecificity());
						participantToUpdate.setSocialKB(
								participantToUpdate.getSocialKB() + ((Utterance) d.getBlocks().get(i)).getSocialKB()
										* d.getBlocks().get(i).getSpecificity());

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
		}

		// update degree of inter-animation
		for (AbstractDocument d : documents) {
			for (Participant p : ((Chat) d).getParticipants()) {
				Participant pToUpdate = community.get(community.indexOf(p));
				pToUpdate.setDegreeInterAnimation(pToUpdate.getDegreeInterAnimation() + p.getDegreeInterAnimation());
			}
		}
	}

	public void addDocument(Chat c) {
		if (c != null) {
			logger.info("Adding document " + c.getPath());
			documents.add(c);
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
				participantToUpdate
						.setNoContributions(participantToUpdate.getNoContributions() + p.getNoContributions());
				participantToUpdate.setRelevanceTop10Topics(
						participantToUpdate.getRelevanceTop10Topics() + p.getRelevanceTop10Topics());
				participantToUpdate.setNoNouns(participantToUpdate.getNoNouns() + p.getNoNouns());
				participantToUpdate.setNoVerbs(participantToUpdate.getNoVerbs() + p.getNoVerbs());

				// update corresponding global significant utterances
				for (Block b : p.getInterventions().getBlocks()) {
					if (b != null) {
						Block.addBlock(participantToUpdate.getInterventions(), b);
						if (b.isSignificant()) {
							Block.addBlock(participantToUpdate.getSignificantInterventions(), b);
						}
					}
				}
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
										+ VectorAlgebra.sumElements(((Chat) d).getVoicePMIEvolution()));
						participantToUpdate
								.setNewThreadsCumulativeSocialKB(participantToUpdate.getNewThreadsCumulativeSocialKB()
										+ VectorAlgebra.sumElements(((Chat) d).getSocialKBEvolution()));
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
							new File(d.getPath()).getName() + "," + noBlocks + "," + ((Chat) d).getParticipants().size()
									+ "," + Formatting.formatNumber(d.getOverallScore()) + ","
									+ Formatting.formatNumber(
											VectorAlgebra.sumElements(((Chat) d).getVoicePMIEvolution()))
									+ "," + Formatting.formatNumber(
											VectorAlgebra.sumElements(((Chat) d).getSocialKBEvolution()))
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

	public static Community loadMultipleConversations(String rootPath, TopicClass specificityClass) {
		logger.info("Loading all files in " + rootPath);

		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".ser");
			}
		};

		Community collection = new Community(specificityClass);
		File dir = new File(rootPath);
		if (!dir.isDirectory())
			return null;
		File[] filesTODO = dir.listFiles(filter);
		for (File f : filesTODO) {
			Chat d = (Chat) Chat.loadSerializedDocument(f.getPath());
			// d.exportIM();
			collection.addDocument(d);
		}

		collection.updateParticipantContributions();

		return collection;
	}

	public static void processDocumentCollection(String rootPath, TopicClass specificityClass) {
		Community dc = Community.loadMultipleConversations(rootPath, specificityClass);
		if (dc != null) {
			dc.computeMetrics();
			File f = new File(rootPath);
			if (dc.getSpecificityClass() != null) {
				dc.export(rootPath + "/" + f.getName() + "_" + dc.getSpecificityClass().toString().toLowerCase()
						+ ".csv");
			} else {
				dc.export(rootPath + "/" + f.getName() + ".csv");
			}

			dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
			dc.generateConceptView(rootPath + "/" + f.getName() + "_concepts.pdf");
		}
	}

	public List<Participant> getCommunity() {
		return community;
	}

	public void setCommunity(List<Participant> community) {
		this.community = community;
	}

	public List<AbstractDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<AbstractDocument> documents) {
		this.documents = documents;
	}

	public TopicClass getSpecificityClass() {
		return specificityClass;
	}

	public void setSpecificityClass(TopicClass specificityClass) {
		this.specificityClass = specificityClass;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		processDocumentCollection("in/blogs_Nic/1 year/KB45", null);

		// processDocumentCollection("in/forum_Nic", null);

		// processDocumentCollection("in/forum_Nic",
		// TopicClass.ACADEMIC_ADMINISTRATION);

		// processDocumentCollection("in/forum_Nic",
		// TopicClass.EDUCATIONAL_SCIENCES);
	}
}
