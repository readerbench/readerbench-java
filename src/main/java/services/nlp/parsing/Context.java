package services.nlp.parsing;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;

import java.util.*;
import java.util.regex.Pattern;
import data.Word;

/**
 * @author Florea Anda-Madalina
 *
 */
public class Context {

	public Context() {
	}

	/**
	 * @param tree
	 * @param w
	 * @param isNoun - true if it is noun, false if it is verb
	 * @return  list with all subtrees
	 */
	public List<Tree> findContextTree(Tree tree, Word w, boolean isNoun) {
		List<Tree> contextTree = new ArrayList<Tree>();
		if (isNoun) {
			contextTree = findContextForNoun(tree, w);
		}
		else {
			contextTree = findContextForVerb(tree, w);
		}
		return contextTree;
	}

	/**
	 * @param tree
	 * @param w
	 * @return context trees for the word which is a verb
	 */
	private List<Tree> findContextForVerb(Tree tree, Word w) {
		List<Tree> contextForVerb = new ArrayList<Tree>();
		//all leaves with the given word as a label
		List<Tree> nodeVoiceList = findNodeInTree(tree, w.getText());
		for (Tree nodeVoice: nodeVoiceList) {
			Tree n = nodeVoice;
			//find the parent which is VP or ROOT, if the verb is the sentence's root
			while(!n.label().value().equals("VP") && !n.label().value().equals("ROOT")) {
				n = n.parent(tree);
			}
			contextForVerb.add(n);
		}
		return contextForVerb;
	}

	/**
	 * @param tree
	 * @param w
	 * @return - context trees for the word which is a noun
	 */
	private List<Tree> findContextForNoun(Tree tree, Word w) {
		List<Tree> contextForNoun = new ArrayList<Tree>();
		//all leaves with the given word as a label
		List<Tree> nodeVoiceList = findNodeInTree(tree, w.getText());

		//find the parent which is NP or NNP
		for (Tree nodeVoice: nodeVoiceList) {
			Tree n = nodeVoice;
			//untill a tag NP or NNP is found, or if the noun is the Sentence's root
			while(!n.label().value().equals("NP") && !n.label().value().equals("NNP") && !n.label().value().equals("ROOT")) {
				n = n.parent(tree);
			}
			//take also the siblings
			if (!n.label().value().equals("ROOT"))
				n = n.parent(tree);
			contextForNoun.add(n);
		}
		return contextForNoun;
	}

	/**
	 * @param tree
	 * @param text
	 * @return a list with all leaves which contain the given word
	 */
	public List<Tree> findNodeInTree(Tree tree, String text) {
		List<Tree> nodeVoiceList = new ArrayList<>();
		//tree can contains the given text as a leaf more than once
		for (Tree leaf: tree.getLeaves()) {
			if (leaf.label().value().equals(text)) {
				nodeVoiceList.add(leaf);
			}
		}
		return nodeVoiceList;
	}
	/**
	 * @param g dependencies graph
	 * @param word - voice
	 * @param adv - list with all the adverbs which has the root as a governor
	 * @return the root of the graph
	 */
	public IndexedWord findRoot(SemanticGraph g, Word word, List<IndexedWord> adv) {
		String regex = "(?i)" + word.getText();
		IndexedWord iw = g.getNodeByWordPattern(regex);

		/*
		if list is empty means that the voice concept is the root,
		else, the first element(usually a verb) is the root
		 */
		List<IndexedWord> l = g.getPathToRoot(iw);
		for (IndexedWord x: l) {
			System.out.println(x.word());
		}
		IndexedWord root = iw;
		if (!l.isEmpty()) {
			root = l.get(0);
		}

		/*
		find all the adverbs which has the root node as a governor
		 */
		List<SemanticGraphEdge> E = g.outgoingEdgeList(root);
		for (SemanticGraphEdge edge: E) {
			GrammaticalRelation relation = edge.getRelation();
			if (relation.getShortName().equals("advmod")) {
				adv.add(edge.getDependent());
			}
		}
		return root;
	}

	/**
	 * @param w - voice concept
	 * @param root - sentence root, found from dependencies
	 * @param tree
	 * @param adverbs
	 * @return context tree
	 */
	public Tree findSubTree(Word w, IndexedWord root, Tree tree, ArrayList<IndexedWord> adverbs) {
		//if the voice is sentence's root, the context is all sentence
		if (w.getText().equals(root.word())) {
			return tree;
		}

		Map<Tree, Integer> nodeLevels = computeLevels(tree);
//		Tree nodeW = findNodeInTree(tree, w.getText());
//		Tree nodeR = findNodeInTree(tree, root.word());
		Tree nodeW = tree;
		Tree nodeR =tree;

		//the subtree which has common ancestor as a root
		Tree commonAncestor = lowestCommonAncestor(tree, nodeW, nodeR, nodeLevels);

		return commonAncestor;
	}

	/**
	 * @param tree - sentence tree
	 * @param nodeW - voice
	 * @param nodeR - root
	 * @param nodeLevels
	 * @return context tree
	 */
	private Tree lowestCommonAncestor(Tree tree, Tree nodeW, Tree nodeR, Map<Tree, Integer> nodeLevels) {
		//save what subtrees were went through
		Tree nodeLeft = nodeW;
		Tree nodeRight = nodeR;

		while(nodeW != nodeR) {
			//the node which has a bigger level will become his parent
			if (nodeLevels.get(nodeW) > nodeLevels.get(nodeR)) {
				nodeLeft = nodeW;
				nodeW = nodeW.parent(tree);
			}
			else {
				nodeRight = nodeR;
				nodeR = nodeR.parent(tree);
			}
		}

		//remove those children which had not been gone through, to restrict the context
		Tree ancestor = nodeW.deepCopy();
		List<Tree> otherChildren = new ArrayList<>();
		for (Tree child:ancestor.getChildrenAsList()) {
			if (!child.equals(nodeLeft) && !child.equals(nodeRight)) {
				otherChildren.add(child);
			}
		}

		ancestor.remove(otherChildren);

		return ancestor;
	}

	private Map<Tree,Integer> computeLevels(Tree tree) {
		//run BFS to find every node's level
		Queue<Tree> nodes = new LinkedList<>();
		Queue<Integer> levels = new LinkedList<>();

		Map<Tree, Integer> nodeLevels =  new HashMap<>();

		//put the root in queue with level 0
		nodes.add(tree);
		levels.add(0);

		while(!nodes.isEmpty()) {
			//the current node with his level
			Tree n = nodes.poll();
			Integer level = levels.poll();
			nodeLevels.put(n, level);

			//put children in queue with level + 1
			for (Tree child: n.getChildrenAsList()) {
				nodes.add(child);
				levels.add(level + 1);
			}
		}
		return nodeLevels;
	}
}
