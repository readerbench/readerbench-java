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

public class SimilarityCalculationDemo_FR {

	private static ILexicalDatabase db = new NictWordNetDB();
	private static RelatednessCalculator[] rcs = {
			new HirstStOnge(db, Lang.fr), new LeacockChodorow(db, Lang.fr),
			new Lesk(db, Lang.fr), new WuPalmer(db, Lang.fr),
			new Resnik(db, Lang.fr), new JiangConrath(db, Lang.fr),
			new Lin(db, Lang.fr), new Path(db, Lang.fr) };

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
		run("famille#n", "unité#n");
		System.out.println("----------------------------");
		run("stylo#n", "maison#n");
		System.out.println("----------------------------");
		run("maison#n", "famille#n");
		System.out.println("----------------------------");
		run("chaud#a", "froid#a");
		long t1 = System.currentTimeMillis();
		System.out.println("Done in " + (t1 - t0) + " msec.");
	}
}