package services.nlp.parsing;

import java.util.StringTokenizer;

import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import DAO.Block;
import DAO.AbstractDocument;
import DAO.Sentence;
import DAO.Word;

public class SimpleParsing {
	public static Block processBlock(AbstractDocument d, int blockIndex,
			String paragraph) {
		Block b = new Block(d, blockIndex, paragraph, d.getLSA(), d.getLDA(),
				d.getLanguage());
		// parse the text using a simple String Tokenizer
		StringTokenizer st = new StringTokenizer(b.getText(), ".!?");
		int utteranceCounter = 0;

		while (st.hasMoreTokens()) {
			String content = st.nextToken().trim();
			if (content.length() > 0) {
				Sentence u = processSentence(b, utteranceCounter++, content);
				// add utterance to block
				b.getSentences().add(u);
				b.setProcessedText(b.getProcessedText() + u.getProcessedText()
						+ ". ");
			}
		}
		b.finalProcessing();
		return b;
	}

	public static Sentence processSentence(Block b, int utteranceIndex,
			String sentence) {
		// basic parsing
		Sentence s = new Sentence(b, utteranceIndex, sentence.toString(),
				b.getLSA(), b.getLDA(), b.getLanguage());
		Word w = null;
		StringTokenizer st = new StringTokenizer(s.getText(), " ,:;'-");
		while (st.hasMoreTokens()) {
			String wordText = st.nextToken().toLowerCase();
			String stem = Stemmer.stemWord(wordText, s.getLanguage());
			String lemma = StaticLemmatizer.lemmaStatic(wordText,
					s.getLanguage());
			if (b != null) {
				w = new Word(s.getContainer().getIndex(), s.getIndex(),
						wordText, lemma, stem, null, null, s.getLSA(),
						s.getLDA(), s.getLanguage());
			} else {
				w = new Word(0, s.getIndex(), wordText, lemma, stem, null,
						null, s.getLSA(), s.getLDA(), s.getLanguage());
			}
			s.getAllWords().add(w);

			if (!StopWords.isStopWord(w.getText(), s.getLanguage())
					&& !StopWords.isStopWord(w.getLemma(), s.getLanguage())
					&& (Dictionary.isDictionaryWord(w.getText(),
							s.getLanguage()) || Dictionary
							.isDictionaryWord(w.getLemma(), s.getLanguage()))
					&& wordText.length() > 2) {
				s.getWords().add(w);
				if (s.getWordOccurences().containsKey(w)) {
					s.getWordOccurences().put(w,
							s.getWordOccurences().get(w) + 1);
				} else {
					s.getWordOccurences().put(w, 1);
				}
			}
		}

		s.finalProcessing();
		return s;
	}
}
