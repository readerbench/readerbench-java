/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.lemmatizer.StaticLemmatizerPOS;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.parsing.Parsing;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import DAO.discourse.SemanticCohesion;
import DAO.sentiment.SentimentEntity;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

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
		
		for (Word w : getWords()) {
			
		}
		
		if (fullParsing) {
	
			// ContextBuilding.buildContext(tree, s.getLanguage());
	
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
								//new SentimentEntity(), getLanguage());
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
					// iterate through SentimentValence.valenceMap to add all sentiments
					Iterator it = DAO.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
					logger.info("There are " + DAO.sentiment.SentimentValence.getValenceMap().size() + " sentiments that should be mapped.");
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        DAO.sentiment.SentimentValence daoSe = (DAO.sentiment.SentimentValence)pair.getValue();
				        //System.out.println(pair.getKey() + " = " + pair.getValue());
				        
				        // iterate through all words and get that sentiments' value
				        double wordSentimentSum = 0.0;
				        double noWordWeights = 0;
				        logger.info("There are " + getWords().size() + " words in this sentence.");
				        for (Word w : getWords()) {
				        	logger.info("Word " + w + " sentiments: " + w.getSentiment());
							Double wordSentimentScore = w.getSentiment().get(daoSe);
							if (wordSentimentScore != null) {
								wordSentimentSum += wordSentimentScore;
								noWordWeights++;
							}
						}
				        
				        // add sentiment entity to the sentence
				        logger.info("Adding sentiment " + daoSe.getName() + " for sentence " + this.getIndex());
				        se.add(daoSe, (noWordWeights > 0 ? wordSentimentSum / noWordWeights : 0.0));
				    }
				    
				    // Stanford Valence
				    int score = RNNCoreAnnotations.getPredictedClass(tree);
				    se.add(new DAO.sentiment.SentimentValence(
			        		10000,
			        		"Stanford",
			        		"STANFORD",
			        		false
			        		), score);
				    
				    setSentimentEntity(se);
				}
			}
		}
		else {
			Word w = null;
			StringTokenizer st = new StringTokenizer(getText(), " ,:;'-");
			while (st.hasMoreTokens()) {
				String wordText = st.nextToken().toLowerCase();
				String stem = Stemmer.stemWord(wordText, getLanguage());
				String lemma = StaticLemmatizer.lemmaStatic(wordText,
						getLanguage());
				if (b != null) {
					w = new Word(getContainer().getIndex(), getIndex(),
							wordText, lemma, stem, null, null, getLSA(),
							getLDA(), getLanguage());
				} else {
					w = new Word(0, getIndex(), wordText, lemma, stem, null,
							null, getLSA(), getLDA(), getLanguage());
				}
				getAllWords().add(w);

				if (!StopWords.isStopWord(w.getText(), getLanguage())
						&& !StopWords.isStopWord(w.getLemma(), getLanguage())
						&& (Dictionary.isDictionaryWord(w.getText(),
								getLanguage()) || Dictionary
								.isDictionaryWord(w.getLemma(), getLanguage()))
						&& wordText.length() > 2) {
					getWords().add(w);
					if (getWordOccurences().containsKey(w)) {
						getWordOccurences().put(w,
								getWordOccurences().get(w) + 1);
					} else {
						getWordOccurences().put(w, 1);
					}
				}
			}
		}
		
		logger.info("There are " + getSentimentEntity().getAll().size() + " sentiments added to my sentence.");
		
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
