package edu.cmu.lti.ws4j.demo;

import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNetDB;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SimilarityCalculationDemo_IT {

	private static ILexicalDatabase db = new NictWordNetDB();
	private static RelatednessCalculator[] rcs = {
			new HirstStOnge(db, Lang.it), new LeacockChodorow(db, Lang.it),
			new Lesk(db, Lang.it), new WuPalmer(db, Lang.it),
			new Resnik(db, Lang.it), new JiangConrath(db, Lang.it),
			new Lin(db, Lang.it), new Path(db, Lang.it) };

	private static void run(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		System.out.println(word1 + " - " + word2);
		for (RelatednessCalculator rc : rcs) {
			try {
				double s = rc.calcRelatednessOfWords(word1, word2);
				System.out.println("\t" + rc.getClass().getName() + "\t" + s);
			} catch (Exception e) {
				System.out.println(rc.getClass().getName() + ": "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();
		// run( "act#v","moderate#v" );
		run("madre#n", "famiglia#n");
		System.out.println("----------------------------");
		run("madre#n", "papà#n");
		System.out.println("----------------------------");
		run("arancia#n", "limone#n");
		System.out.println("----------------------------");
		long t1 = System.currentTimeMillis();
		System.out.println("Done in " + (t1 - t0) + " msec.");
	}
}