/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.nlp.parsing;

import data.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Stefan
 */
public class Parsing_LA extends Parsing {

	static Logger logger = Logger.getLogger(Parsing_LA.class);

	private static Parsing_LA instance = null;
	private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_LA());

	private Parsing_LA() {
		lang = Lang.la;
	}

	public static Parsing_LA getInstance() {
		if (instance == null) {
			instance = new Parsing_LA();
		}
		return instance;
	}

	@Override
	public StanfordCoreNLP getPipeline() {
		return pipeline;
	}

	class ParsingParams_LA extends Properties {

		private static final long serialVersionUID = -1561330268167277821L;

		public ParsingParams_LA() {
			super();
		}
	}

}
