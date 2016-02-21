package services.nlp.parsing;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing_FR extends Parsing {

	private static Parsing_FR instance = null;

	private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_FR());

	private Parsing_FR() {
		lang = Lang.fr;
	}

	public static Parsing_FR getInstance() {
		if (instance == null) {
			instance = new Parsing_FR();
		}
		return instance;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		AbstractDocumentTemplate docTmp = getDocumentModel();
		AbstractDocument d = new Document(null, docTmp, null, null, Lang.fr, true, false);
		System.out.println(d);
	}

	/**
	 * @return
	 */
	protected static AbstractDocumentTemplate getDocumentModel() {
		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
		BlockTemplate block = docTmp.new BlockTemplate();
		block.setId(0);
		block.setContent("Ce soir-là, la famille de Matilda "
				+ "dînait rapidement comme d'habitude devant la télévision.\nTout est bon.");
		docTmp.getBlocks().add(block);
		return docTmp;
	}

	@Override
	public String convertToPenn(String pos) {
		// rename French POS according to the Pen TreeBank POSs
		// http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		if (pos.startsWith("N")) {
			return "NN";
		}
		if (pos.startsWith("V")) {
			return "VB";
		}
		if (pos.startsWith("CL")) {
			return "PR";
		}
		if (pos.startsWith("C")) {
			return "CC";
		}
		if (pos.startsWith("D")) {
			return "IN";
		}
		if (pos.startsWith("ADV")) {
			return "RB";
		}
		if (pos.startsWith("A")) {
			return "JJ";
		}
		return pos;
	}

	@Override
	public StanfordCoreNLP getPipeline() {
		return pipeline;
	}
}

class ParsingParams_FR extends Properties {

	private static final long serialVersionUID = -161579346328207322L;

	public ParsingParams_FR() {
		this.put("tokenize.language", "fr");
		this.put("pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");
		this.put("parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
		this.put("annotators", "tokenize, ssplit, pos, parse");

		/*
		 * this.put("pos.model", "resources/config/POSmodels/french.tagger");
		 * this.put("parse.model",
		 * "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
		 * this.put("parse.flags", ""); this.put("parse.buildgraphs", "false");
		 * this.put("annotators", "tokenize, ssplit, pos, parse");
		 * this.put("numThreads", "8");
		 */
	}
}
