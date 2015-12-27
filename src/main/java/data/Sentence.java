/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import data.discourse.SemanticCohesion;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.lemmatizer.StaticLemmatizerPOS;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.parsing.Parsing;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 * 
 * @author Mihai Dascalu
 */
public class Sentence extends AnalysisElement implements Comparable<Sentence> {
	private static final long serialVersionUID = 6612571737695007151L;

	private List<Word> words;
	private List<Word> allWords;
	private int POSTreeDepth;
	private int POSTreeSize;
	private transient Tree parseTree;
	private transient SemanticGraph dependencies;
	private SemanticCohesion titleSimilarity;

	public Sentence(Block b, int index, String text, LSA lsa, LDA lda, Lang lang) {
		super(b, index, text.replaceAll("\\s", " ").trim(), lsa, lda, lang);
		this.words = new LinkedList<Word>();
		this.allWords = new LinkedList<Word>();
	}

	public void finalProcessing(boolean fullParsing, Block b, CoreMap sentence) {
		if (fullParsing) {
			// build the Stanford dependency graph of the current sentence
			if (getLanguage().equals(Lang.eng)) {
				setDependencies(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
			}

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(OriginalTextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				String ne = token.get(NamedEntityTagAnnotation.class);

				if (!word.matches("[,:;'\\.\\!\\?\\-]")) {
					Word w = null;
					switch (getLanguage()) {
					case fr:
						pos = Parsing.convertFrPenn(pos);
						w = new Word(getContainer().getIndex(), getIndex(), word,
								StaticLemmatizerPOS.lemmaStatic(word, pos, Lang.fr),
								Stemmer.stemWord(word.toLowerCase(), getLanguage()), pos, ne, getLSA(), getLDA(),
								getLanguage());
						break;
					case it:
						// trim POS
						if (pos != null && pos.length() > 2)
							pos = pos.substring(0, 2);
						w = new Word(getContainer().getIndex(), getIndex(), word,
								StaticLemmatizerPOS.lemmaStatic(word, pos, Lang.it),
								Stemmer.stemWord(word.toLowerCase(), getLanguage()), pos, ne, getLSA(), getLDA(),
								getLanguage());
						break;
					case es:
						pos = Parsing.convertEsPenn(pos);
						String stem = Stemmer.stemWord(word.toLowerCase(), Lang.es);
						w = new Word(getContainer().getIndex(), getIndex(), word,
								StaticLemmatizer.lemmaStatic(word, Lang.es), stem, pos, ne, getLSA(), getLDA(),
								getLanguage());
						break;
					default:
						// trim POS
						if (pos != null && pos.length() > 2)
							pos = pos.substring(0, 2);
						String lemma = Morphology.lemmaStatic(word, pos, true);
						w = new Word(getContainer().getIndex(), getIndex(), word, lemma,
								Stemmer.stemWord(word.toLowerCase(), getLanguage()), pos, ne, getLSA(), getLDA(),
								getLanguage());
						// new SentimentEntity(), getLanguage());
						break;
					}

					getAllWords().add(w);
					if (w.getText().length() > 1 && !StopWords.isStopWord(w.getText(), getLanguage())
							&& !StopWords.isStopWord(w.getLemma(), getLanguage())
							&& (Dictionary.isDictionaryWord(w.getText(), getLanguage())
									|| Dictionary.isDictionaryWord(w.getLemma(), getLanguage()))) {
						if (w.getPOS().equals("NN") || w.getPOS().equals("VB") || w.getPOS().equals("JJ")
								|| w.getPOS().equals("RB")) {
							getWords().add(w);
							if (getWordOccurences().containsKey(w)) {
								getWordOccurences().put(w, getWordOccurences().get(w) + 1);
							} else {
								getWordOccurences().put(w, 1);
							}
						}
					}

				}
			}

			Tree tree = null;
			if (getLanguage().equals(Lang.eng) || getLanguage().equals(Lang.fr) || getLanguage().equals(Lang.es)) {
				// this is the parse tree of the current sentence
				tree = sentence.get(TreeAnnotation.class);
				setPOSTreeDepth(tree.depth());
				setPOSTreeSize(tree.size());
				setParseTree(tree);
				// TreePrint tp = new TreePrint("penn");
				// tp.printTree(tree);
				tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
				if (getLanguage().equals(Lang.eng)) {
					SentimentEntity se = new SentimentEntity();
					// iterate through SentimentValence.valenceMap to add all
					// sentiments
					// logger.info("There are " +
					// SentimentValence.getAllValences().size() + " sentiments
					// that should be mapped.");
					for (SentimentValence daoSe : SentimentValence.getAllValences()) {
						// System.out.println(pair.getKey() + " = " +
						// pair.getValue());

						// iterate through all words and get that sentiments'
						// value
						// double wordSentimentSum = 0.0;
						// double noWordWeights = 0;
						// logger.info("There are " + getWords().size() + "
						// words in this sentence.");
						double value = getWords().stream().mapToDouble(w -> {
							SentimentEntity e = w.getSentiment();
							if (e == null)
								return 0.;
							Double v = e.get(daoSe);
							return (v == null ? 0. : v);
						}).sum() / getWords().size();
						/*
						 * for (Word w : getWords()) { logger.info("Word " + w +
						 * " sentiments: " + w.getSentiment()); Double
						 * wordSentimentScore = w.getSentiment().get(daoSe); if
						 * (wordSentimentScore != null) { wordSentimentSum +=
						 * wordSentimentScore; noWordWeights++; } }
						 */
						// add sentiment entity to the sentence
						// logger.info("Adding sentiment " + daoSe.getName() + "
						// for sentence " + this.getIndex());
						se.add(daoSe, value);
					}

					// Stanford Valence
					int score = RNNCoreAnnotations.getPredictedClass(tree);
					se.add(new data.sentiment.SentimentValence(10000, "Stanford", "STANFORD", false), score);

					setSentimentEntity(se);

					// add relevant word associations if the case
					SemanticGraph dependencies = this.getDependencies();
					if (dependencies != null) {
						for (SemanticGraphEdge edge : dependencies.edgeListSorted()) {
							String dependent = edge.getDependent().word().toLowerCase();
							Word w1 = Word.getWordFromConcept(dependent, getLanguage());
							w1.setLemma(StaticLemmatizer.lemmaStatic(dependent, getLanguage()));

							String governor = edge.getGovernor().word().toLowerCase();
							Word w2 = Word.getWordFromConcept(governor, getLanguage());
							w2.setLemma(StaticLemmatizer.lemmaStatic(governor, getLanguage()));
							String association = w1.getLemma() + Word.WORD_ASSOCIATION + w2.getLemma();
							Word wordAssociation = new Word(association, association, association, null, null, getLSA(),
									getLDA(), getLanguage());
							// add correspondingly the word association if the
							// LSA space contains it
							if (getLSA().getWords().containsKey(wordAssociation)) {
								System.out.println("BAU" + wordAssociation);
								if (getWordOccurences().containsKey(wordAssociation)) {
									getWordOccurences().put(wordAssociation,
											getWordOccurences().get(wordAssociation) + 1);
								} else {
									getWordOccurences().put(wordAssociation, 1);
								}
							}
						}
					}
				}
			}
		} else {
			Word w = null;
			StringTokenizer st = new StringTokenizer(getText(), " ,:;'-");
			while (st.hasMoreTokens()) {
				String wordText = st.nextToken().toLowerCase();
				String stem = Stemmer.stemWord(wordText, getLanguage());
				String lemma = StaticLemmatizer.lemmaStatic(wordText, getLanguage());
				if (b != null) {
					w = new Word(getContainer().getIndex(), getIndex(), wordText, lemma, stem, null, null, getLSA(),
							getLDA(), getLanguage());
				} else {
					w = new Word(0, getIndex(), wordText, lemma, stem, null, null, getLSA(), getLDA(), getLanguage());
				}
				getAllWords().add(w);

				// add content words
				if (!StopWords.isStopWord(w.getText(), getLanguage())
						&& !StopWords.isStopWord(w.getLemma(), getLanguage())
						&& (Dictionary.isDictionaryWord(w.getText(), getLanguage())
								|| Dictionary.isDictionaryWord(w.getLemma(), getLanguage()))
						&& wordText.length() > 2) {
					getWords().add(w);
					if (getWordOccurences().containsKey(w)) {
						getWordOccurences().put(w, getWordOccurences().get(w) + 1);
					} else {
						getWordOccurences().put(w, 1);
					}
				}
			}
		}

		// logger.info("There are " + getSentimentEntity().getAll().size() + "
		// sentiments added to my sentence.");

		// write the processedText
		String processedText = "";
		for (Word word : getWords()) {
			processedText += word.getLemma() + " ";
		}
		setProcessedText(processedText.trim());

		// determine LSA utterance vector
		determineSemanticDimensions();
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public int getPOSTreeDepth() {
		return POSTreeDepth;
	}

	public void setPOSTreeDepth(int pOSTreeDepth) {
		POSTreeDepth = pOSTreeDepth;
	}

	public int getPOSTreeSize() {
		return POSTreeSize;
	}

	public void setPOSTreeSize(int pOSTreeSize) {
		POSTreeSize = pOSTreeSize;
	}

	public Tree getParseTree() {
		return parseTree;
	}

	public void setParseTree(Tree parseTree) {
		this.parseTree = parseTree;
	}

	public SemanticGraph getDependencies() {
		return dependencies;
	}

	public void setDependencies(SemanticGraph dependencies) {
		this.dependencies = dependencies;
	}

	public List<Word> getAllWords() {
		return allWords;
	}

	public void setAllWords(List<Word> allWords) {
		this.allWords = allWords;
	}

	public SemanticCohesion getTitleSimilarity() {
		return titleSimilarity;
	}

	public void setTitleSimilarity(SemanticCohesion titleSimilarity) {
		this.titleSimilarity = titleSimilarity;
	}

	@Override
	public String toString() {
		String s = "";
		// if (text.indexOf(" ", 40) > 0) {
		// s = "[" + text.substring(0, text.indexOf(" ", 40)) + "...]\n";
		// } else {
		// s = "[" + text + "]\n";
		// }
		for (Word w : allWords) {
			if (words.contains(w))
				s += w.toString() + "* ";
			else
				s += w.toString() + " ";
		}
		s += "[" + getOverallScore() + "]";
		return s;
	}

	@Override
	public int compareTo(Sentence o) {
		return (int) (Math.signum(o.getOverallScore() - this.getOverallScore()));
	}
}
