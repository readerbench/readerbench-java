package services.nlp.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Block;
import data.Sentence;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.sentiment.SentimentEntity;
import data.Lang;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import services.commons.TextPreprocessing;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.lemmatizer.StaticLemmatizerPOS;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;

/**
 *
 * @author Mihai Dascalu
 */
public abstract class Parsing {

	static Logger logger = Logger.getLogger(Parsing.class);

	protected Lang lang;

	public static Parsing getParser(Lang lang) {
		switch (lang) {
		case fr:
			return Parsing_FR.getInstance();
		case it:
			return Parsing_IT.getInstance();
		case es:
			return Parsing_ES.getInstance();
		case nl:
			return Parsing_NL.getInstance();
		case ro:
			return Parsing_RO.getInstance();
		case eng:
			return Parsing_EN.getInstance();
		case la:
			return Parsing_LA.getInstance();
		default:
			return null;
		}
	}

	public String convertToPenn(String pos) {
		if (pos != null && pos.length() > 2) {
			return pos.substring(0, 2);
		}
		return pos;
	}

	public abstract StanfordCoreNLP getPipeline();

	private Utterance getUtterance(Conversation c, BlockTemplate blockTmp, Block b) {
		Participant activeSpeaker = null;
		if (!blockTmp.getSpeaker().isEmpty()) {
			activeSpeaker = new Participant(blockTmp.getSpeaker(), c);
			boolean contains = false;
			for (Participant p : c.getParticipants()) {
				if (p.equals(activeSpeaker)) {
					activeSpeaker = p;
					contains = true;
				}
			}
			if (!contains) {
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

		Utterance u = new Utterance(b, activeSpeaker, time);
		return u;
	}

	public void parseDoc(AbstractDocumentTemplate adt, AbstractDocument d, boolean usePOSTagging, boolean cleanInput) {
		try {
			if (!adt.getBlocks().isEmpty()) {
				for (BlockTemplate blockTmp : adt.getBlocks()) {
					// extract name (if applicable)

					// extract block text
					String text = cleanInput ? TextPreprocessing.cleanText(blockTmp.getContent(), lang)
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

					Block b;
					Annotation document = null;

					if (usePOSTagging) {
						// create an empty Annotation just with the given text
						document = new Annotation(text);
						// run all Annotators on this text
						getPipeline().annotate(document);
						// add corresponding block
						b = processBlock(d, id, text, document.get(SentencesAnnotation.class));
					} else {
						b = SimpleParsing.processBlock(d, id, text);
					}
					if (d instanceof Conversation) {
						b = getUtterance((Conversation) d, blockTmp, b);
					}
					b.setFollowedByVerbalization(followedByVerbalization);
					Block.addBlock(d, b);

					// add explicit reference, if the case
					if (ref > 0) {
						for (Block refB : d.getBlocks()) {
							if (refB != null && refB.getIndex() == ref) {
								b.setRefBlock(refB);
								break;
							}
						}
					}

					if (usePOSTagging && lang.equals(Lang.eng)) {
						// Build the co-reference link graph
						// Each chain stores a set of mentions that link to each
						// other, along with a method for getting the most
						// representative mention. Both sentence and token
						// offsets start at 1!
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

			if (d.getLDA() != null) {
				d.setLDAProbDistribution(d.getLDA().getProbDistribution(d));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public Block processBlock(AbstractDocument d, int blockIndex, String content, List<CoreMap> sentences) {
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
			}
		}

		b.finalProcessing();
		return b;
	}

	public Sentence processSentence(Block b, int utteranceIndex, CoreMap sentence) {
		// uses Stanford Core NLP
		Sentence s = new Sentence(b, utteranceIndex, sentence.toString().trim(), b.getLSA(), b.getLDA(), lang);

		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.get(OriginalTextAnnotation.class);
			String pos = Parsing.getParser(lang).convertToPenn(token.get(PartOfSpeechAnnotation.class));
			String ne = token.get(NamedEntityTagAnnotation.class);

			if (!word.matches("[,:;'\\.\\!\\?\\-]")) {
				Word w = new Word(b.getIndex(), s.getIndex(), word, StaticLemmatizerPOS.lemmaStatic(word, pos, lang),
						Stemmer.stemWord(word.toLowerCase(), lang), Parsing.getParser(lang).convertToPenn(pos), ne,
						s.getLSA(), s.getLDA(), lang);

				s.getAllWords().add(w);
				if (w.getText().length() > 1 && !StopWords.isStopWord(w.getText(), lang)
						&& !StopWords.isStopWord(w.getLemma(), lang) && (Dictionary.isDictionaryWord(w.getText(), lang)
								|| Dictionary.isDictionaryWord(w.getLemma(), lang))) {
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

		Tree tree = null;
		if (lang.equals(Lang.eng) || lang.equals(Lang.fr) || lang.equals(Lang.es)) {
			// this is the parse tree of the current sentence
			tree = sentence.get(TreeAnnotation.class);
			s.setPOSTreeDepth(tree.depth());
			s.setPOSTreeSize(tree.size());
			s.setParseTree(tree);
			// TreePrint tp = new TreePrint("penn");
			// tp.printTree(tree);
			if (lang.equals(Lang.eng) || lang.equals(Lang.fr)) {
				s.setDependencies(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));

				// add relevant word associations if the case
				SemanticGraph dependencies = s.getDependencies();
				if (dependencies != null) {
					for (SemanticGraphEdge edge : dependencies.edgeListSorted()) {
						String dependent = edge.getDependent().word().toLowerCase();
						Word w1 = Word.getWordFromConcept(dependent, lang);
						w1.setLemma(StaticLemmatizer.lemmaStatic(dependent, lang));

						String governor = edge.getGovernor().word().toLowerCase();
						Word w2 = Word.getWordFromConcept(governor, lang);
						w2.setLemma(StaticLemmatizer.lemmaStatic(governor, lang));
						String association = w1.getLemma() + Word.WORD_ASSOCIATION + w2.getLemma();
						Word wordAssociation = new Word(association, association, association, null, null, s.getLSA(),
								s.getLDA(), lang);
						// add correspondingly the word association if the LSA
						// space contains it
						if (s.getLSA() != null && s.getLSA().getWords().containsKey(wordAssociation)) {
							if (s.getWordOccurences().containsKey(wordAssociation)) {
								s.getWordOccurences().put(wordAssociation,
										s.getWordOccurences().get(wordAssociation) + 1);
							} else {
								s.getWordOccurences().put(wordAssociation, 1);
							}
						}
					}
				}
			}
			if (lang.equals(Lang.eng)) {
				tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
				SentimentEntity se = new SentimentEntity();
				// Stanford Valence
				int score = RNNCoreAnnotations.getPredictedClass(tree);
				se.add(new data.sentiment.SentimentValence(10000, "Stanford", "STANFORD", false), score);
				s.setSentimentEntity(se);
			}

		}

		s.finalProcessing(b, sentence);
		return s;
	}
}
