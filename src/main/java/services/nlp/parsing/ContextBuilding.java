package services.nlp.parsing;

import java.util.LinkedList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.trees.Tree;

public class ContextBuilding {
	private static final String[] IMPORTANT_POS = { "NN", "VB", "RB", "JJ" };
	private static final int MIN_CONTEXT_SIZE = 2;
	private static final int MAX_CONTEXT_SIZE = 5;
	private static List<String> context;
	private static List<List<String>> relevantContexts;

	private static boolean isImportant(String pos) {
		for (String p : IMPORTANT_POS)
			if (p.equals(pos))
				return true;
		return false;
	}

	private static boolean isImportant(Tree tree, Lang lang) {
		if (tree.isPreTerminal()) {
			int index = tree.nodeString().indexOf(" ");
			String pos = "";
			if (index >= 0)
				pos = tree.nodeString().substring(0, index);
			else
				pos = tree.nodeString();
			if (lang.equals(Lang.fr))
				pos = Parsing.convertFrPenn(pos);
			// trim POS
			if (pos.length() > 2)
				pos = pos.substring(0, 2);
			// consider only important POS for adding to the context
			if (isImportant(pos))
				return true;
		}
		return false;
	}

	public static void buildContext(Tree tree, Lang lang) {
		//reinitialize context for current utterance
		context = new LinkedList<String>();
		relevantContexts = new LinkedList<List<String>>();
		buildContexts(tree, new LinkedList<Tree>(), lang);
		
		//print relevant contexts
		for (List<String> context:relevantContexts)
			System.out.println(context);
		System.out.println();
	}

	private static void chooseStrategy(Tree tree, List<Tree> peers, Lang lang) {
		// go deep
		List<Tree> kids = tree.getChildrenAsList();
		for (int i = 0; i < kids.size(); i++) {
			List<Tree> peersKid = new LinkedList<Tree>();
			for (int j = i + 1; j < kids.size(); j++)
				peersKid.add(kids.get(j));
			// add all known peers
			peersKid.addAll(peers);

			buildContexts(kids.get(i), peersKid, lang);
		}

		// select first peer
		if (peers.size() > 0) {
			Tree peer = peers.remove(0);
			// ignore peer
			// buildContexts(tree, peers, lang);

			// selecting that specific peer
			buildContexts(peer, peers, lang);
		}
	}

	private static boolean equalContext(List<String> context1,
			List<String> context2) {
		if (context1.size() != context2.size())
			return false;
		for (int i = 0; i < context1.size(); i++)
			if (!context1.get(i).equals(context2.get(i)))
				return false;
		return true;
	}

	private static boolean alreadyContainsContext() {
		for (List<String> context1 : relevantContexts)
			if (equalContext(context1, context))
				return true;
		return false;
	}

	private static void buildContexts(Tree tree, List<Tree> peers, Lang lang) {
		if (!tree.isLeaf()) {
			if (isImportant(tree, lang) && context.size() < MAX_CONTEXT_SIZE) {
				context.add(tree.pennString().trim());
				chooseStrategy(tree, peers, lang);
				context.remove(tree.pennString().trim());
			}
			chooseStrategy(tree, peers, lang);
		} else {
			if (context.size() >= MIN_CONTEXT_SIZE && !alreadyContainsContext()) {
				List<String> copyContext = new LinkedList<String>();
				for (String word : context)
					copyContext.add(word);
				relevantContexts.add(copyContext);
			}
		}
	}
}
