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

public class SimilarityCalculationDemo_RO {

	private static ILexicalDatabase db = new NictWordNetDB();
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db, Lang.ro), new LeacockChodorow(db, Lang.ro),
			new Lesk(db, Lang.ro), new WuPalmer(db, Lang.ro), new Resnik(db, Lang.ro), new JiangConrath(db, Lang.ro),
			new Lin(db, Lang.ro), new Path(db, Lang.ro) };

	private static void run(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		System.out.println(word1 + " - " + word2);
		for (RelatednessCalculator rc : rcs) {
			try {
				double s = rc.calcRelatednessOfWords(word1, word2);
				System.out.println("\t" + rc.getClass().getName() + "\t" + s);
			} catch (Exception e) {
				System.out.println(rc.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();
		run("deal#n", "pat#n");
		System.out.println("----------------------------");
		run("monopol#n", "capitalism#n");
		System.out.println("----------------------------");
		run("carte#n", "bilet#n");
		System.out.println("----------------------------");
		run("cald#a", "prietenos#a");
		long t1 = System.currentTimeMillis();
		System.out.println("Done in " + (t1 - t0) + " msec.");
	}
}