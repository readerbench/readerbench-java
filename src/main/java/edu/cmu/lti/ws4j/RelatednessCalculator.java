package edu.cmu.lti.ws4j;

import java.util.List;

import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.util.Configuration;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.util.DepthFinder;
import edu.cmu.lti.ws4j.util.MatrixCalculator;
import edu.cmu.lti.ws4j.util.PathFinder;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.cmu.lti.ws4j.util.WordSimilarityCalculator;

public abstract class RelatednessCalculator{

	private final static WS4JConfiguration c;
	public final static boolean enableCache;
	public final static boolean enableTrace;

	protected final static String illegalSynset = "Synset is null.";
	protected final static String identicalSynset = "Synsets are identical.";

	protected ILexicalDatabase db;
	protected PathFinder pathFinder;
	protected DepthFinder depthFinder;
	protected Lang lang;

	public RelatednessCalculator(ILexicalDatabase db, Lang lang) {
		this.db = db;
		this.lang = lang;
		pathFinder = new PathFinder(db, lang);
		depthFinder = new DepthFinder(db, lang);
	}

	public final static boolean useRootNode;

	static {
		c = WS4JConfiguration.getInstance();
		enableCache = c.useCache();
		enableTrace = c.useTrace();
		useRootNode = true;
	}

	private WordSimilarityCalculator wordSimilarity = new WordSimilarityCalculator();

	// abstract hook method to be implemented
	public abstract Relatedness calcRelatedness(Concept synset1, Concept synset2);

	public abstract List<POS[]> getPOSPairs();

	// template method
	public Relatedness calcRelatednessOfSynset(Concept synset1, Concept synset2) {
		long t0 = System.currentTimeMillis();
		Relatedness r = calcRelatedness(synset1, synset2);
		long t1 = System.currentTimeMillis();
		r.appendTrace("Process done in = " + (double) (t1 - t0) / 1000D
				+ " sec (cache: "
				+ (Configuration.USE_CACHE ? "enabled" : "disabled") + ").\n");
		return r;
	}

	public double calcRelatednessOfWords(String word1, String word2) {
		return wordSimilarity.calcRelatednessOfWords(word1, word2, this);
	}

	public double[][] getSimilarityMatrix(String[] words1, String[] words2) {
		return MatrixCalculator.getSimilarityMatrix(words1, words2, this);
	}

	public double[][] getNormalizedSimilarityMatrix(String[] words1,
			String[] words2) {
		return MatrixCalculator.getNormalizedSimilarityMatrix(words1, words2,
				this);
	}

	/**
	 * @return the db
	 */
	public ILexicalDatabase getDB() {
		return db;
	}

	public Lang getLang() {
		return lang;
	}
}
