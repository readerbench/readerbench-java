package services.nlp.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import services.commons.TextPreprocessing;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.lemmatizer.StaticLemmatizerPOS;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import DAO.AbstractDocument;
import DAO.AbstractDocumentTemplate.BlockTemplate;
import DAO.cscl.Conversation;
import DAO.cscl.Participant;
import DAO.cscl.Utterance;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.sentiment.SentimentEntity;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import pojo.SentimentValence;

/**
 * 
 * @author Mihai Dascalu
 */
public abstract class Parsing {
	static Logger logger = Logger.getLogger(Parsing.class);

	public static String convertFrPenn(String pos) {
		// rename French POS according to the Pen TreeBank POSs
		// http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		if (pos.startsWith("N"))
			return "NN";
		if (pos.startsWith("V"))
			return "VB";
		if (pos.startsWith("CL"))
			return "PR";
		if (pos.startsWith("C"))
			return "CC";
		if (pos.startsWith("D"))
			return "IN";
		if (pos.startsWith("ADV"))
			return "RB";
		if (pos.startsWith("A"))
			return "JJ";
		return pos;
	}

	public static String convertEsPenn(String pos) {
		// rename Spanish POS -
		// http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-es.html according
		// to the Pen TreeBank POSs
		// http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		if (pos.startsWith("d"))
			return "DT";
		if (pos.startsWith("n"))
			return "NN";
		if (pos.startsWith("v"))
			return "VB";
		if (pos.startsWith("p"))
			return "PR";
		if (pos.startsWith("cc"))
			return "CC";
		if (pos.startsWith("cs") || pos.startsWith("s"))
			return "IN";
		if (pos.startsWith("i"))
			return "UH";
		if (pos.startsWith("r"))
			return "RB";
		if (pos.startsWith("a"))
			return "JJ";
		return pos;
	}

	public static void parseDoc(AbstractDocument d, boolean usePOSTagging, boolean cleanInput) {
		try {
			if (!d.getDocTmp().getBlocks().isEmpty()) {
				java.util.Iterator<BlockTemplate> it = d.getDocTmp().getBlocks().iterator();
				while (it.hasNext()) {
					BlockTemplate blockTmp = it.next();
					Participant activeSpeaker = null;
					// extract name (if applicable)
					if (d instanceof Conversation) {
						Conversation c = (Conversation) d;
						if (!blockTmp.getSpeaker().isEmpty()) {
							activeSpeaker = new Participant(blockTmp.getSpeaker(), d);
							boolean contains = false;
							for (Participant p : c.getParticipants())
								if (p.equals(activeSpeaker)) {
									activeSpeaker = p;
									contains = true;
								}
							if (!contains)
								c.getParticipants().add(activeSpeaker);
						}
					}

					Date time = null;
					// extract date (if applicable)
					if (null != blockTmp.getTime()) {
						try {
							DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
							time = df.parse(blockTmp.getTime());
						} catch (ParseException e) {
							DateFormat df2 = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm aaa");
							try {
								time = df2.parse(blockTmp.getTime());
							} catch (ParseException e2) {
								DateFormat df3 = new SimpleDateFormat("kk.mm.ss");
								try {
									time = df3.parse(blockTmp.getTime());
								} catch (ParseException e3) {
									DateFormat df4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									try {
										time = df4.parse(blockTmp.getTime());
									} catch (ParseException e4) {
										DateFormat df5 = new SimpleDateFormat("dd MMMMMMMM yyyy HH:mm", Locale.FRANCE);
										try {
											time = df5.parse(blockTmp.getTime());
										} catch (ParseException e5) {
											DateFormat df6 = new SimpleDateFormat("HH:mm:ss");
											try {
												time = df6.parse(blockTmp.getTime());
											} catch (ParseException e6) {
												try {
													Long longTime = Long.parseLong(blockTmp.getTime());
													time = new Date(longTime * 1000);
												} catch (NumberFormatException e7) {
													logger.error("Unparsable date: " + blockTmp.getTime());
												}
											}
										}
									}
								}
							}
						}
					}

					// extract block text
					String text = cleanInput ? TextPreprocessing.cleanText(blockTmp.getContent(), d.getLanguage())
							: blockTmp.getContent().toLowerCase().replaceAll("\\s+", " ").trim();

					// get block ID
					int id = 0;
					try {
						id = Double.valueOf(blockTmp.getId()).intValue();
					} catch (Exception e) {
						id = 0;
					}
					// get ref ID
					int ref = 0;
					try {
						ref = Double.valueOf(blockTmp.getRefId()).intValue();
					} catch (Exception e) {
						ref = 0;
					}

					boolean followedByVerbalization = false;
					// mark if the block has a verbalization afterwards
					if (null != blockTmp.getVerbId()) {
						try {
							followedByVerbalization = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					Block b = null;
					Utterance u = null;
					Annotation document = null;

					if (usePOSTagging) {
						// create an empty Annotation just with the given text
						document = new Annotation(text);
						// run all Annotators on this text
						switch (d.getLanguage()) {
						case fr:
							Parsing_FR.pipeline.annotate(document);
							break;
						case it:
							Parsing_IT.pipeline.annotate(document);
							break;
						case es:
							Parsing_ES.pipeline.annotate(document);
							break;
						default:
							Parsing_EN.pipeline.annotate(document);
							break;
						}

						// add corresponding block
						b = processBlock(d, id, text, document.get(SentencesAnnotation.class));
					} else {
						b = SimpleParsing.processBlock(d, id, text);
					}
					if (d instanceof Conversation) {
						u = new Utterance(b, activeSpeaker, time);
						u.setFollowedByVerbalization(followedByVerbalization);
						Block.addBlock(d, u);
					} else {
						b.setFollowedByVerbalization(followedByVerbalization);
						Block.addBlock(d, b);
					}

					// add explicit reference, if the case
					if (ref > 0) {
						for (Block refB : d.getBlocks()) {
							if (refB != null && refB.getIndex() == ref) {
								if (u != null)
									u.setRefBlock(refB);
								else
									b.setRefBlock(refB);
								break;
							}
						}
					}

					if (usePOSTagging && d.getLanguage().equals(Lang.eng)) {
						// Build the co-reference link graph
						// Each chain stores a set of mentions that link to each
						// other,
						// along with a method for getting the most
						// representative mention
						// Both sentence and token offsets start at 1!
						b.setAnnotation(document);
						b.setCorefs(document.get(CorefChainAnnotation.class));

						// for (CorefChain chain : d.getCorefs().values()) {
						// if (chain.getCorefMentions().size() > 1) {
						// for (CorefMention cm : chain.getCorefMentions())
						// System.out.println(cm + "|" + cm.sentNum);
						// System.out.println(chain);
						// }
						// }
					}
				}
			}
			// determine overall word occurrences
			d.determineWordOccurences(d.getBlocks());

			if (d.getLDA() != null)
				d.setLDAProbDistribution(d.getLDA().getProbDistribution(d));
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static Block processBlock(AbstractDocument d, int blockIndex, String content, List<CoreMap> sentences) {
		// uses Stanford Core NLP
		Block b = new Block(d, blockIndex, content, d.getLSA(), d.getLDA(), d.getLanguage());

		// set Stanford sentences
		b.setStanfordSentences(sentences);
		int utteranceCounter = 0;

		for (CoreMap sentence : sentences) {
			if (sentence.toString().trim().length() > 1) {
				Sentence s = processSentence(b, utteranceCounter++, sentence);
				// add utterance to block
				b.getSentences().add(s);
				b.setProcessedText(b.getProcessedText() + s.getProcessedText() + ". ");
				
				// add sentiment entity to the block
			}
		}

		b.finalProcessing();
		return b;
	}

	public static Sentence processSentence(Block b, int utteranceIndex, CoreMap sentence) {
		// uses Stanford Core NLP
		Sentence s = new Sentence(b, utteranceIndex, sentence.toString().trim(), b.getLSA(), b.getLDA(),
				b.getLanguage());

		Tree tree = null;
		if (s.getLanguage().equals(Lang.eng) || s.getLanguage().equals(Lang.fr) || s.getLanguage().equals(Lang.es)) {
			// this is the parse tree of the current sentence
			tree = sentence.get(TreeAnnotation.class);
			s.setPOSTreeDepth(tree.depth());
			s.setPOSTreeSize(tree.size());
			s.setParseTree(tree);
			// TreePrint tp = new TreePrint("penn");
			// tp.printTree(tree);
			tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
			if (s.getLanguage().equals(Lang.eng)) {
				
				SentimentEntity se = new SentimentEntity();
				// iterate through SentimentValence.valenceMap to add all sentiments
				Iterator it = DAO.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        DAO.sentiment.SentimentValence daoSe = (DAO.sentiment.SentimentValence)pair.getValue();
			        //System.out.println(pair.getKey() + " = " + pair.getValue());
			        
			        // iterate through all words and get that sentiments' value
			        double wordSentimentSum = 0.0;
			        for (Word w : s.getWords()) {
						Double wordSentimentScore = w.getSentiment().get(daoSe);
						if (wordSentimentScore != null) {
							wordSentimentSum += wordSentimentScore;
						}
					}
			        
			        // add sentiment entity to the sentence
			        se.add(new DAO.sentiment.SentimentValence(
			        		daoSe.getId(),
			        		daoSe.getName(),
			        		daoSe.getIndexLabel(),
			        		daoSe.getRage()
			        		), wordSentimentSum);
			        it.remove(); // avoids a ConcurrentModificationException
			    }
				
				int score = RNNCoreAnnotations.getPredictedClass(tree);
				s.setSentimentEntity(new SentimentEntity());
			}
		}

		// ContextBuilding.buildContext(tree, s.getLanguage());

		// build the Stanford dependency graph of the current sentence
		if (s.getLanguage().equals(Lang.eng)) {
			s.setDependencies(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}

		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.get(OriginalTextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			String ne = token.get(NamedEntityTagAnnotation.class);
			Tree tokenTree = token.get(SentimentAnnotatedTree.class);
			int score = 2;
			if (tokenTree != null)
				score = RNNCoreAnnotations.getPredictedClass(tokenTree);

			if (!word.matches("[,:;'\\.\\!\\?\\-]")) {
				Word w = null;
				switch (s.getLanguage()) {
				case fr:
					pos = convertFrPenn(pos);
					w = new Word(s.getContainer().getIndex(), s.getIndex(), word,
							StaticLemmatizerPOS.lemmaStatic(word, pos, Lang.fr),
							Stemmer.stemWord(word.toLowerCase(), s.getLanguage()), pos, ne, s.getLSA(), s.getLDA(),
							s.getLanguage());
					break;
				case it:
					// trim POS
					if (pos != null && pos.length() > 2)
						pos = pos.substring(0, 2);
					w = new Word(s.getContainer().getIndex(), s.getIndex(), word,
							StaticLemmatizerPOS.lemmaStatic(word, pos, Lang.it),
							Stemmer.stemWord(word.toLowerCase(), s.getLanguage()), pos, ne, s.getLSA(), s.getLDA(),
							s.getLanguage());
					break;
				case es:
					pos = convertEsPenn(pos);
					String stem = Stemmer.stemWord(word.toLowerCase(), Lang.es);
					w = new Word(s.getContainer().getIndex(), s.getIndex(), word,
							StaticLemmatizer.lemmaStatic(word, Lang.es), stem, pos, ne, s.getLSA(), s.getLDA(),
							s.getLanguage());
					break;
				default:
					// trim POS
					if (pos != null && pos.length() > 2)
						pos = pos.substring(0, 2);
					String lemma = Morphology.lemmaStatic(word, pos, true);
					w = new Word(s.getContainer().getIndex(), s.getIndex(), word, lemma,
							Stemmer.stemWord(word.toLowerCase(), s.getLanguage()), pos, ne, s.getLSA(), s.getLDA(),
							new SentimentEntity(), s.getLanguage());
					break;
				}

				s.getAllWords().add(w);
				if (w.getText().length() > 1 && !StopWords.isStopWord(w.getText(), s.getLanguage())
						&& !StopWords.isStopWord(w.getLemma(), s.getLanguage())
						&& (Dictionary.isDictionaryWord(w.getText(), s.getLanguage())
								|| Dictionary.isDictionaryWord(w.getLemma(), s.getLanguage()))) {
					if (w.getPOS().equals("NN") || w.getPOS().equals("VB") || w.getPOS().equals("JJ")
							|| w.getPOS().equals("RB")) {
						s.getWords().add(w);
						if (s.getWordOccurences().containsKey(w)) {
							s.getWordOccurences().put(w, s.getWordOccurences().get(w) + 1);
						} else {
							s.getWordOccurences().put(w, 1);
						}
					}
				}

			}
		}

		s.finalProcessing();
		return s;
	}
}
