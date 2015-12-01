package services.nlp.parsing;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing_EN {
	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_EN());

	public static void main(String[] args) {
		BasicConfigurator.configure();

		AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(
				"What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance). Human machine interface for ABC computer applications."
						+ " A survey of user opinion of computer system response time."
						+ " The EPS user interface management system. "
						+ "System and human system engineering testing of EPS. "
						+ "Relation of user perceived response time to error measurement.");

		AbstractDocument d = new Document(null,
				// "The green and brown fox didn't jump over the lazy dog's
				// head",
				docTmp, null, null, Lang.eng, true, false);
		System.out.println(d);

		// for (Map.Entry<Integer, CorefChain> entry : d.getBlocks().get(0)
		// .getCorefs().entrySet()) {
		// CorefChain c = entry.getValue();
		//
		// // this is because it prints out a lot of self references which
		// // aren't that useful
		// if (c.getMentionsInTextualOrder().size() <= 1)
		// continue;
		//
		// CorefMention cm = c.getRepresentativeMention();
		// String clust = "";
		// List<CoreLabel> tks = d.getBlocks().get(0).getAnnotation()
		// .get(SentencesAnnotation.class).get(cm.sentNum - 1)
		// .get(TokensAnnotation.class);
		// for (int i = cm.startIndex - 1; i < cm.endIndex - 1; i++)
		// clust += tks.get(i).get(TextAnnotation.class) + " ";
		// clust = clust.trim();
		// System.out.println("representative mention: \"" + clust
		// + "\" is mentioned by:");
		//
		// for (CorefMention m : c.getMentionsInTextualOrder()) {
		// String clust2 = "";
		// tks = d.getBlocks().get(0).getAnnotation()
		// .get(SentencesAnnotation.class).get(m.sentNum - 1)
		// .get(TokensAnnotation.class);
		// for (int i = m.startIndex - 1; i < m.endIndex - 1; i++)
		// clust2 += tks.get(i).get(TextAnnotation.class) + " ";
		// clust2 = clust2.trim();
		// // don't need the self mention
		// // if (clust.equals(clust2))
		// // continue;
		//
		// System.out.println("\t" + clust2);
		// }
		// }
	}
}

/**
 * @return
 */

class ParsingParams_EN extends Properties {
	private static final long serialVersionUID = -161579346328207322L;

	public ParsingParams_EN() {
		super();
		this.put("tokenize.options", "normalizeCurrency=false");
		this.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-caseless-left3words-distsim.tagger");
		this.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.caseless.ser.gz");
		// this.put("depparse.model",
		// "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
		this.put("ner.model",
				"edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.muc.7class.caseless.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.conll.4class.caseless.distsim.crf.ser.gz");

		this.put("dcoref.sievePasses",
				"MarkRole, DiscourseMatch, ExactStringMatch, RelaxedExactStringMatch, PreciseConstructs, StrictHeadMatch1, StrictHeadMatch2, StrictHeadMatch3, StrictHeadMatch4, RelaxedHeadMatch, PronounMatch");
		this.put("dcoref.score", "false");
		this.put("dcoref.postprocessing", "true");
		this.put("dcoref.maxdist", "-1");
		this.put("dcoref.use.big.gender.number", "false");
		this.put("dcoref.replicate.conll", "false");
		this.put("numThreads", "8");

		this.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, gender, sentiment");
		// this.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse,
		// dcoref, gender, sentiment");
	}
}
