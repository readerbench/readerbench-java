package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.Collaboration;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.cohesion.DisambiguisationGraphAndLexicalChains;
import services.discourse.cohesion.SentimentAnalysis;
import services.discourse.dialogism.DialogismComputations;
import services.discourse.topicMining.Scoring;
import services.discourse.topicMining.TopicModeling;
import services.nlp.parsing.Parsing;
import services.nlp.parsing.Parsing_EN;
import services.nlp.parsing.Parsing_FR;
import services.nlp.parsing.Parsing_IT;
import services.nlp.parsing.SimpleParsing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.cscl.CSCLIndices;
import data.cscl.CollaborationZone;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.discourse.SemanticCohesion;
import data.discourse.SemanticRelatedness;
import data.discourse.Topic;
import data.document.Document;
import data.lexicalChains.DisambiguationGraph;
import data.lexicalChains.LexicalChain;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author Mihai Dascalu
 */
public abstract class AbstractDocument extends AnalysisElement {
	private static final long serialVersionUID = -6173684658096015060L;
	public static final int MIN_PERCENTAGE_CONTENT_WORDS = 2;

	private String path;
	private String titleText;
	private Sentence title;
	private Vector<Block> blocks;
	// cohesion between a block and the overall document
	private SemanticCohesion[] blockDocDistances;
	// inter-block cohesion values
	private SemanticCohesion[][] blockDistances;
	private SemanticCohesion[][] prunnedBlockDistances;

	// semantic relatdness between a block and the overall document
	private SemanticRelatedness[] blockDocRelatedness;
	// inter-block semantic relatedness values
	private SemanticRelatedness[][] blockRelatedness;
	private SemanticRelatedness[][] prunnedBlockRelatedness;

	private AbstractDocumentTemplate docTmp;
	private String genre;
	// useful for time series analysis - 0 for documents and the difference in
	// - measures the distance between the current & the previous utterance, in
	// ms
	private long[] blockOccurrencePattern;

	private List<LexicalChain> lexicalChains;
	private DisambiguationGraph disambiguationGraph;

	private double[] complexityIndices;

	private List<SemanticChain> voices;
	private transient List<SemanticChain> selectedVoices;

	public AbstractDocument() {
		super();
		this.blocks = new Vector<Block>();
		this.lexicalChains = new LinkedList<LexicalChain>();
	}

	public AbstractDocument(String path, LSA lsa, LDA lda, Lang lang) {
		this();
		this.path = path;
		setLanguage(lang);
		this.disambiguationGraph = new DisambiguationGraph(lang);
		setLSA(lsa);
		setLDA(lda);
	}

	public void rebuildSemanticSpaces(LSA lsa, LDA lda) {
		this.setLSA(lsa);
		this.setLDA(lda);
		for (Block b : getBlocks()) {
			if (b != null) {
				b.setLSA(lsa);
				b.setLDA(lda);
				if (b.getSentences() != null) {
					for (Sentence s : b.getSentences()) {
						s.setLSA(lsa);
						s.setLDA(lda);
						for (Word w : s.getAllWords()) {
							w.setLSA(lsa);
							w.setLDA(lda);
						}
					}
				}
			}
		}
		if (voices != null) {
			for (SemanticChain chain : voices) {
				chain.setLSA(lsa);
				chain.setLDA(lda);
			}
		}
	}

	public void computeAll(String pathToComplexityModel, int[] selectedComplexityFactors) {
		computeDiscourseAnalysis();

		// compute all textual complexity factors
		if (pathToComplexityModel != null && selectedComplexityFactors != null) {
			ComplexityIndices.computeComplexityFactors(this);
		}
	}

	/**
	 * 
	 */
	public void computeDiscourseAnalysis() {
		// build coherence graph
		CohesionGraph.buildCohesionGraph(this);

		// build disambiguisation graph and lexical chains
		DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(this);
		DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(this);
		// System.out.println(d.disambiguationGraph);

		DisambiguisationGraphAndLexicalChains.buildLexicalChains(this);
		// for (LexicalChain chain : lexicalChains) {
		// System.out.println(chain);
		// }

		DisambiguisationGraphAndLexicalChains.computeWordDistances(this);
		// System.out.println(LexicalCohesion.getDocumentCohesion(this));

		// determine semantic chains / voices
		DialogismComputations.determineVoices(this);

		// determine topics
		TopicModeling.determineTopics(this, this);
		// TopicModel.determineTopicsLDA(this);

		Scoring.score(this);
		// assign sentiment values
		SentimentAnalysis.weightSemanticValences(this);

		// determine voice distributions & importance
		DialogismComputations.determineVoiceDistributions(this);

		logger.info("Finished all discourse analysis processes...");
	}

	public void setDocumentTitle(String title, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging) {
		this.titleText = title;
		Annotation document = null;
		String processedText = title.replaceAll("\\s+", " ");

		if (processedText.length() > 0) {
			if (usePOSTagging) {
				// create an empty Annotation just with the given text
				document = new Annotation(processedText.replaceAll("[\\.\\!\\?\n]", ""));
				// run all Annotators on this text

				switch (lang) {
				case fr:
					Parsing_FR.pipeline.annotate(document);
					break;
				case it:
					Parsing_IT.pipeline.annotate(document);
					break;
				default:
					Parsing_EN.pipeline.annotate(document);
					break;
				}

				CoreMap sentence = document.get(SentencesAnnotation.class).get(0);

				// add corresponding block
				setTitle(Parsing.processSentence(new Block(null, 0, "", lsa, lda, lang), 0, sentence));
			} else {
				setTitle(SimpleParsing.processSentence(new Block(null, 0, "", lsa, lda, lang), 0, processedText));
			}
		}
	}

	public static AbstractDocument loadGenericDocument(String pathToDoc, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, boolean cleanInput) {
		// load also LSA vector space and LDA model
		LSA lsa = null;
		LDA lda = null;
		if (pathToLSA != null && pathToLSA.length() > 0 && new File(pathToLSA).isDirectory())
			lsa = LSA.loadLSA(pathToLSA, lang);
		if (pathToLDA != null && pathToLDA.length() > 0 && new File(pathToLDA).isDirectory())
			lda = LDA.loadLDA(pathToLDA, lang);
		return loadGenericDocument(new File(pathToDoc), lsa, lda, lang, usePOSTagging, cleanInput);
	}

	public static AbstractDocument loadGenericDocument(File docFile, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging,
			boolean cleanInput) {
		// parse the XML file
		logger.info("Loading " + docFile.getPath() + " file for processing");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			InputSource input = new InputSource(new FileInputStream(docFile));
			input.setEncoding("UTF-8");
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(input);

			Element doc = dom.getDocumentElement();

			// determine whether the document is a document or a chat
			NodeList nl;
			boolean isDocument = false;
			nl = doc.getElementsByTagName("p");
			if (nl.getLength() > 0)
				isDocument = true;

			boolean isChat = false;
			nl = doc.getElementsByTagName("Utterance");
			if (nl.getLength() > 0)
				isChat = true;

			if (isChat && isDocument) {
				throw new Exception(
						"Input file has an innapropriate structure as it contains tags for both documents and chats!");
			}
			if (!isChat && !isDocument) {
				throw new Exception(
						"Input file has an innapropriate structure as it not contains any tags for documents or chats!");
			}

			if (isDocument) {
				Document d = Document.load(docFile, lsa, lda, lang, usePOSTagging, cleanInput);
				d.computeAll(null, null, true);
				return d;
			}
			if (isChat) {
				Conversation c = Conversation.load(docFile, lsa, lda, lang, usePOSTagging, cleanInput);
				c.computeAll(null, null, true);
				return c;
			}
		} catch (Exception e) {
			logger.error("Error evaluating input file " + docFile.getName() + " - " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public void saveSerializedDocument() {
		logger.info("Saving serialized document");
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(new File(getPath().replace(".xml", ".ser")));
			out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static AbstractDocument loadSerializedDocument(String path) {
		logger.info("Loading serialized document " + path);
		FileInputStream fIn = null;
		ObjectInputStream oIn = null;
		AbstractDocument d = null;
		try {
			fIn = new FileInputStream(new File(path));
			oIn = new ObjectInputStream(fIn);
			d = (AbstractDocument) oIn.readObject();
			oIn.close();
			fIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	@Override
	public String toString() {
		String s = "";
		if (title != null)
			s += title + "\n";
		for (Block b : blocks) {
			if (b != null)
				s += b + "\n";
		}
		return s;
	}

	// Get the list of sentences of a document
	public List<Sentence> getSentencesInDocument() {
		List<Sentence> sentences = new ArrayList<Sentence>();
		for (Block block : this.getBlocks()) {
			if (block != null) {
				sentences.addAll(block.getSentences());
			}
		}
		return sentences;
	}

	public void exportDocument() {
		try {
			logger.info("Writing document export");
			File output = new File(path.replace(".xml", ".csv"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);

			if (titleText != null)
				out.write(titleText.replaceAll(",", "").replaceAll("\\s+", " ") + "\n");
			if (getLSA() != null)
				out.write("LSA space:," + getLSA().getPath() + "\n");
			if (getLDA() != null)
				out.write("LDA model:," + getLDA().getPath() + "\n");

			out.write(
					"\nBlock Index,Ref Block Index,Participant,Date,Score,Personal Knowledge Building,Social Knowledge Building,Initial Text,Processed Text\n");
			for (Block b : blocks) {
				if (b != null) {
					out.write(b.getIndex() + ",");
					if (b.getRefBlock() != null)
						out.write(b.getRefBlock().getIndex() + "");
					out.write(",");
					if (b instanceof Utterance) {
						if (((Utterance) b).getParticipant() != null)
							out.write(((Utterance) b).getParticipant().getName().replaceAll(",", "").replaceAll("\\s+",
									" "));
						out.write(",");
						if (((Utterance) b).getTime() != null)
							out.write(((Utterance) b).getTime() + "");
						out.write(",");
						out.write(b.getOverallScore() + "," + ((Utterance) b).getPersonalKB() + ","
								+ ((Utterance) b).getSocialKB() + "," + b.getText().replaceAll(",", "") + ","
								+ b.getProcessedText() + "\n");
					} else {
						out.write(",," + b.getOverallScore() + ",," + b.getText().replaceAll(",", "") + ","
								+ b.getProcessedText() + "\n");
					}
				}
			}

			// print topics
			out.write("\nTopics - Relevance\n");
			List<Topic> topics = null;

			topics = TopicModeling.getSublist(getTopics(), 100, false, false);
			out.write("Entire document:");
			for (Topic t : topics) {
				out.write("," + t.getWord().getLemma() + " (" + t.getWord().getPOS() + ") - "
						+ Formatting.formatNumber(t.getRelevance()));
			}
			out.write("\n");

			if (this.getLDA() != null) {
				out.write("\nTopics - Clusters\n");
				Map<Integer, List<Topic>> topicClusters = new TreeMap<Integer, List<Topic>>();
				for (Topic t : this.getTopics()) {
					Integer probClass = LDA.findMaxResemblance(t.getWord().getLDAProbDistribution(),
							this.getLDAProbDistribution());
					if (!topicClusters.containsKey(probClass)) {
						topicClusters.put(probClass, new LinkedList<Topic>());
					}
					topicClusters.get(probClass).add(t);
				}
				for (Integer cluster : topicClusters.keySet()) {
					out.write(cluster + ":,");
					for (Topic t : topicClusters.get(cluster))
						out.write(t.getWord().getLemma() + " (" + t.getRelevance() + "),");
					out.write("\n");
				}
			}

			if (this instanceof Conversation) {
				out.write("\nTopics per Participant\n");
				Conversation c = (Conversation) this;
				if (c.getParticipants().size() > 0) {
					for (Participant p : c.getParticipants()) {
						topics = TopicModeling.getSublist(p.getInterventions().getTopics(), 100, false, false);
						out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " ") + ":");
						for (Topic t : topics) {
							out.write("," + t.getWord().getLemma() + " (" + t.getWord().getPOS() + ") - "
									+ Formatting.formatNumber(t.getRelevance()));
						}
						out.write("\n");
					}
				}

				// print participant statistics
				if (c.getParticipants().size() > 0) {
					out.write("\nParticipant involvement and interaction\n");
					out.write("Participant name");
					for (CSCLIndices CSCLindex : CSCLIndices.values())
						out.write(CSCLindex.getDescription());
					for (Participant p : c.getParticipants()) {
						out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
						for (CSCLIndices index : CSCLIndices.values()) {
							out.write("," + p.getIndices().get(index));
						}
					}
					// print interaction matrix
					out.write("Interaction matrix\n");
					for (Participant p : c.getParticipants())
						out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
					out.write("\n");
					Iterator<Participant> it = c.getParticipants().iterator();
					int i = 0;
					while (it.hasNext()) {
						Participant part = it.next();
						out.write(part.getName().replaceAll(",", "").replaceAll("\\s+", " "));
						for (int j = 0; j < c.getParticipants().size(); j++) {
							out.write("," + Formatting.formatNumber(c.getParticipantContributions()[i][j]));
						}
						i++;
						out.write("\n");
					}
				}

				// print collaboration zone statistics
				if (c.getAnnotatedCollabZones().size() > 0) {
					out.write("\nIntense collaboration zones - Annotated\n");
					for (CollaborationZone zone : c.getAnnotatedCollabZones())
						out.write(zone.toStringDetailed() + "\n");
				}

				// print collaboration zone statistics
				if (c.getIntenseCollabZonesSocialKB().size() > 0) {
					out.write("\nIntense collaboration zones - Social Knowledge Building\n");
					for (CollaborationZone zone : c.getIntenseCollabZonesSocialKB())
						out.write(zone.toStringDetailed() + "\n");
				}

				// print collaboration zone statistics
				if (c.getIntenseCollabZonesVoice().size() > 0) {
					out.write("\nIntense collaboration zones - Voice PMI\n");
					for (CollaborationZone zone : c.getIntenseCollabZonesVoice())
						out.write(zone.toStringDetailed() + "\n");
				}

				// print statistics
				double[] results = null;
				if (c.getAnnotatedCollabZones() != null && c.getAnnotatedCollabZones().size() > 0) {
					results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
							c.getIntenseCollabZonesSocialKB());

					out.write("\nOverlap between annotated collaboration zones and Social KB model\n" + "P=,"
							+ results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
									.pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getSocialKBEvolution()));

					results = Collaboration.overlapCollaborationZones(c, c.getAnnotatedCollabZones(),
							c.getIntenseCollabZonesVoice());
					out.write("\nOverlap between annotated collaboration zones and Voice PMI model\n" + "P=,"
							+ results[0] + "\nR=," + results[1] + "\nF1 score=," + results[2] + "\nr=," + VectorAlgebra
									.pearsonCorrelation(c.getAnnotatedCollabEvolution(), c.getVoicePMIEvolution()));
				}
				results = Collaboration.overlapCollaborationZones(c, c.getIntenseCollabZonesSocialKB(),
						c.getIntenseCollabZonesVoice());
				out.write("\nOverlap between Social KB model and Voice PMI model\n" + "P=," + results[0] + "\nR=,"
						+ results[1] + "\nF1 score=," + results[2] + "\nr=,"
						+ VectorAlgebra.pearsonCorrelation(c.getVoicePMIEvolution(), c.getSocialKBEvolution()) + "\n");
			}

			// print lexical chains
			if (lexicalChains.size() > 0) {
				out.write("\nLexical chains\n");
				for (LexicalChain chain : lexicalChains) {
					out.write(chain.toString() + "\n");
				}
			}

			// print cohesion measurements
			out.write("\nCohesion measurements\n");
			out.write("Items,LSA,LDA,Leacock Chodorow,Wu Palmer,Path Similarity,Distance,Overall\n");
			// block - doc
			for (int i = 0; i < blocks.size(); i++) {
				if (blocks.get(i) != null) {
					SemanticCohesion coh = blockDocDistances[i];
					out.write("D - B" + blocks.get(i).getIndex() + "," + coh.print() + "\n");
				}
			}
			// pruned block-block
			for (int i = 0; i < blocks.size() - 1; i++) {
				for (int j = i + 1; j < blocks.size(); j++) {
					if (prunnedBlockDistances[i][j] != null) {
						SemanticCohesion coh = prunnedBlockDistances[i][j];
						out.write("B" + i + "-B" + j + "," + coh.print() + "\n");
					}
				}
			}
			out.write("\n");

			out.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void exportDocumentAdvanced() {
		try {
			logger.info("Writing advanced document export");
			File output = new File(path.replace(".xml", "_adv.csv"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);

			out.write(
					"ID,Block,Text,Score,Cosine Sim LSA,Divergence LDA,Leacok Chodorow,Wu Palmer,Path Sim,Dist,Cohesion\n");
			int globalIndex = 0;
			for (Block b : blocks) {
				if (b != null) {
					for (int index = 0; index < b.getSentences().size(); index++) {
						Sentence u = b.getSentences().get(index);

						out.write(globalIndex++ + ",");
						out.write(b.getIndex() + ",");
						out.write(u.getText().replaceAll(",", "") + ",");
						out.write(Formatting.formatNumber(u.getOverallScore()) + ",");
						if (index > 0) {
							SemanticCohesion coh = b.getSentenceDistances()[index - 1][index];
							out.write(coh.print() + "\n");
						} else {
							out.write("0,0,0,0,0,0,0\n");
						}
					}
				}
			}

			out.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Sentence getTitle() {
		return title;
	}

	public String getTitleText() {
		return titleText;
	}

	public Vector<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(Vector<Block> blocks) {
		this.blocks = blocks;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public void setTitle(Sentence title) {
		this.title = title;
	}

	public SemanticCohesion[][] getBlockDistances() {
		return blockDistances;
	}

	public void setBlockDistances(SemanticCohesion[][] blockDistances) {
		this.blockDistances = blockDistances;
	}

	public SemanticCohesion[][] getPrunnedBlockDistances() {
		return prunnedBlockDistances;
	}

	public void setPrunnedBlockDistances(SemanticCohesion[][] prunnedBlockDistances) {
		this.prunnedBlockDistances = prunnedBlockDistances;
	}

	public SemanticCohesion[] getBlockDocDistances() {
		return blockDocDistances;
	}

	public void setBlockDocDistances(SemanticCohesion[] blockDocDistances) {
		this.blockDocDistances = blockDocDistances;
	}

	public SemanticRelatedness[][] getBlockRelatedness() {
		return blockRelatedness;
	}

	public void setBlockRelatedness(SemanticRelatedness[][] blockRelatedness) {
		this.blockRelatedness = blockRelatedness;
	}

	public SemanticRelatedness[][] getPrunnedBlockRelatedness() {
		return prunnedBlockRelatedness;
	}

	public void setPrunnedBlockRelatedness(SemanticRelatedness[][] prunnedBlockRelatedness) {
		this.prunnedBlockRelatedness = prunnedBlockRelatedness;
	}

	public SemanticRelatedness[] getBlockDocRelatedness() {
		return blockDocRelatedness;
	}

	public void setBlockDocRelatedness(SemanticRelatedness[] blockDocRelatedness) {
		this.blockDocRelatedness = blockDocRelatedness;
	}

	public List<LexicalChain> getLexicalChains() {
		return lexicalChains;
	}

	public void setLexicalChains(List<LexicalChain> lexicalChains) {
		this.lexicalChains = lexicalChains;
	}

	public DisambiguationGraph getDisambiguationGraph() {
		return disambiguationGraph;
	}

	public void setDisambiguationGraph(DisambiguationGraph disambiguationGraph) {
		this.disambiguationGraph = disambiguationGraph;
	}

	public double[] getComplexityIndices() {
		return complexityIndices;
	}

	public void setComplexityIndices(double[] complexityFactors) {
		this.complexityIndices = complexityFactors;
	}

	public List<SemanticChain> getVoices() {
		return voices;
	}

	public void setVoices(List<SemanticChain> voices) {
		this.voices = voices;
	}

	public List<SemanticChain> getSelectedVoices() {
		return selectedVoices;
	}

	public void setSelectedVoices(List<SemanticChain> selectedVoices) {
		this.selectedVoices = selectedVoices;
	}

	public long[] getBlockOccurrencePattern() {
		return blockOccurrencePattern;
	}

	public void setBlockOccurrencePattern(long[] blockOccurrencePattern) {
		this.blockOccurrencePattern = blockOccurrencePattern;
	}

	public String getDescription() {
		String s = this.getTitleText();
		if (this.getLSA() != null && this.getLDA() != null)
			s += " [" + this.getLSA().getPath() + ", " + this.getLDA().getPath() + "]";
		return s;
	}

	public AbstractDocumentTemplate getDocTmp() {
		return docTmp;
	}

	public void setDocTmp(AbstractDocumentTemplate docTmp) {
		this.docTmp = docTmp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getMinWordCoverage() {
		int noWords = 0;
		for (Entry<Word, Integer> entry : getWordOccurences().entrySet())
			noWords += entry.getValue();
		return Math.round(MIN_PERCENTAGE_CONTENT_WORDS / 100f * noWords + 0.5f);
	}

	// get voices with more words than getMinWordCoverage()
	public List<SemanticChain> getSignificantVoices() {
		if (this.getVoices() == null || this.getVoices().size() == 0)
			return null;
		List<SemanticChain> importantVoices = new ArrayList<SemanticChain>();
		for (SemanticChain chain : this.getVoices()) {
			if (chain.getWords().size() >= this.getMinWordCoverage()) {
				importantVoices.add(chain);
			}
		}
		return importantVoices;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
}
