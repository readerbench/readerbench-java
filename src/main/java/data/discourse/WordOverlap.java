package data.discourse;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;
import data.document.Document;
import data.Lang;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import services.nlp.parsing.Parsing_EN;

public class WordOverlap {
	public static final float WORD_OVERLAP_ALPHA = 0.3f;

	public List<Document> documents;
	public HashMap<String, Double> documentScores;
	public HashMap<AbstractDocument, Double> documentOverlapScores;
	public HashMap<AbstractDocument, Double> documentSemanticScores;
	public HashMap<AbstractDocument, Double> documentAggregatedScores;

	public HashMap<String, Double> semanticCohesionScores;

	public WordOverlap(List<Document> documents) {
		this.documents = documents;
		this.documentScores = new HashMap<String, Double>();
		this.semanticCohesionScores = new HashMap<String, Double>();
		this.documentOverlapScores = new HashMap<AbstractDocument, Double>();
		this.documentSemanticScores = new HashMap<AbstractDocument, Double>();
		this.documentAggregatedScores = new HashMap<AbstractDocument, Double>();

	}

	public List<Document> computeWordOverlaps() {
		// the max number of word occurrences
		double maxScore = 1.0d;

		for (Document doc : documents) {
			// if(! (doc instanceof Document))
			// continue;

			// STEP 1 : word overlap distance
			// for each keyword create a number of occurrence equal to 0
			HashMap<String, Integer> topicOccurency = new HashMap<String, Integer>();
			List<Word> wordList = ((Document) doc).getInitialTopics();
			for (Word topic : wordList) {
				topicOccurency.put(topic.getText(), new Integer(0));
			}

			// compute the number of occurrences for each word
			for (Block block : doc.getBlocks()) {
				if (block == null)
					continue;

				for (Sentence sentence : block.getSentences()) {
					for (Word word : sentence.getAllWords()) {
						if (!topicOccurency.containsKey(word.getText().toLowerCase()))
							continue;
						Integer count = topicOccurency.get(word.getText().toLowerCase());
						count = new Integer(count + 1);
						topicOccurency.put(word.getText().toLowerCase(), count);
					}
				}
			}
			double documentScore = getScore(topicOccurency);
			if (documentScore > maxScore)
				maxScore = documentScore;
			documentScores.put(((Document) doc).getFullDescription(), documentScore);
		}

		// normalize score based on the max number of occurencies
		Iterator<String> keySet = documentScores.keySet().iterator();
		while (keySet.hasNext()) {
			String key = keySet.next();
			Double score = documentScores.get(key);
			score = score / maxScore;
			documentScores.put(key, score);
		}

		// STEP 2: SemanticCohesion score
		for (AbstractDocument doc : documents) {
			if (doc.getBlocks().size() == 0) {
				semanticCohesionScores.put(((Document) doc).getFullDescription(), 0.0d);
				continue;
			}
			Block docAbstract = null;
			for (Block b : doc.getBlocks()) {
				if (b != null) {
					docAbstract = b;
					break;
				}
			}
			if (docAbstract == null) {
				semanticCohesionScores.put(((Document) doc).getFullDescription(), 0.0d);
				continue;
			}

			String keywordSentenceStr = "";
			List<Word> wordList = ((Document) doc).getInitialTopics();
			for (Word topic : wordList) {
				keywordSentenceStr += (keywordSentenceStr.length() > 0) ? " " : "";
				keywordSentenceStr += topic.getText();
			}

			if (keywordSentenceStr.trim().length() == 0) {
				semanticCohesionScores.put(((Document) doc).getFullDescription(), 0.0d);
				continue;
			}

			Annotation keywordDocument = new Annotation(keywordSentenceStr);
			Parsing_EN.getInstance().getPipeline().annotate(keywordDocument);
			CoreMap keywordSentence = keywordDocument.get(SentencesAnnotation.class).get(0);

			Sentence docKeywords = Parsing_EN.getInstance()
					.processSentence(new Block(null, 0, "", doc.getLSA(), doc.getLDA(), Lang.eng), 0, keywordSentence);

			SemanticCohesion semanticCohesion = new SemanticCohesion(docAbstract, docKeywords);
			double cohesionVal = (float) semanticCohesion.getCohesion();
			semanticCohesionScores.put(((Document) doc).getFullDescription(), cohesionVal);
		}

		// STEP 3: combine scores
		for (AbstractDocument doc : documents) {
			Double wordOverlapScore = documentScores.get(((Document) doc).getFullDescription());
			Double semanticCohesionScore = semanticCohesionScores.get(((Document) doc).getFullDescription());
			Double aggregatedScore = WORD_OVERLAP_ALPHA * wordOverlapScore
					+ (1 - WORD_OVERLAP_ALPHA) * semanticCohesionScore;
			// update the score
			documentScores.put(((Document) doc).getFullDescription(), aggregatedScore);

			documentOverlapScores.put(doc, wordOverlapScore);
			documentSemanticScores.put(doc, semanticCohesionScore);
			documentAggregatedScores.put(doc, aggregatedScore);
		}

		// sort the documents based on their score
		Collections.sort(documents, new Comparator<AbstractDocument>() {
			public int compare(AbstractDocument d1, AbstractDocument d2) {
				double score1 = documentScores.get(((Document) d1).getFullDescription());
				double score2 = documentScores.get(((Document) d2).getFullDescription());
				return -Double.compare(score1, score2);
			}
		});
		// System.out.print(">> ");
		for (AbstractDocument doc : documents) {
			Document d = (Document) doc;
			d.setKeywordAbstractOverlap(documentScores.get(d.getFullDescription()));
			// System.out.print(documentScores.get(((Document)doc).getFullDescription())
			// + ":" +
			// doc.getTitleText() + " ");
		}
		return documents;
	}

	private float getScore(HashMap<String, Integer> topicOccurency) {
		float totalCount = 0.0f;
		Iterator<String> keyIt = topicOccurency.keySet().iterator();
		while (keyIt.hasNext()) {
			float occurency = (float) topicOccurency.get(keyIt.next());
			totalCount += occurency;
		}
		return totalCount;
	}

	public HashMap<AbstractDocument, Double> getDocumentOverlapScores() {
		return documentOverlapScores;
	}

	public HashMap<AbstractDocument, Double> getDocumentSemanticScores() {
		return documentSemanticScores;
	}

	public HashMap<AbstractDocument, Double> getDocumentAggregatedScores() {
		return documentAggregatedScores;
	}
}
