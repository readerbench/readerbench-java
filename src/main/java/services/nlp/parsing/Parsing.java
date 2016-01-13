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
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import services.commons.TextPreprocessing;

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
			default:
				return Parsing_EN.getInstance();
		}
	}

	public String convertToPenn(String pos) {
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

	public void parseDoc(
			AbstractDocumentTemplate adt, 
			AbstractDocument d,
			boolean usePOSTagging, boolean cleanInput) {
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
						b = getUtterance((Conversation)d, blockTmp, b);
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
		Sentence s = new Sentence(b, utteranceIndex, sentence.toString().trim(), b.getLSA(), b.getLDA(),
				b.getLanguage());

		s.finalProcessing(true, b, sentence);
		return s;
	}
}
