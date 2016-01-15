package services.nlp.parsing;

import java.util.StringTokenizer;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;
import data.sentiment.SentimentEntity;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;

public class SimpleParsing {
	public static Block processBlock(AbstractDocument d, int blockIndex, String paragraph) {
		Block b = new Block(d, blockIndex, paragraph, d.getLSA(), d.getLDA(), d.getLanguage());
		// parse the text using a simple String Tokenizer
		StringTokenizer st = new StringTokenizer(b.getText(), ".!?");
		int utteranceCounter = 0;

		while (st.hasMoreTokens()) {
			String content = st.nextToken().trim();
			if (content.length() > 0) {
				Sentence u = processSentence(b, utteranceCounter++, content);
				// add utterance to block
				b.getSentences().add(u);
				b.setProcessedText(b.getProcessedText() + u.getProcessedText() + ". ");
			}
		}
		b.finalProcessing();
		return b;
	}

	public static Sentence processSentence(Block b, int utteranceIndex, String sentence) {
		// basic parsing
		Lang lang = b.getLanguage();
		Sentence s = new Sentence(b, utteranceIndex, sentence.toString(), b.getLSA(), b.getLDA(), lang);

		Word w = null;
		StringTokenizer st = new StringTokenizer(s.getText(), " ,:;'-");
		while (st.hasMoreTokens()) {
			String wordText = st.nextToken().toLowerCase();
			String stem = Stemmer.stemWord(wordText, lang);
			String lemma = StaticLemmatizer.lemmaStatic(wordText, lang);
			if (b != null) {
				w = new Word(b.getIndex(), s.getIndex(), wordText, lemma, stem, null, null, s.getLSA(), s.getLDA(),
						lang);
			} else {
				w = new Word(0, s.getIndex(), wordText, lemma, stem, null, null, s.getLSA(), s.getLDA(), lang);
			}
			s.getAllWords().add(w);

			// add content words
			if (!StopWords.isStopWord(w.getText(), lang) && !StopWords.isStopWord(w.getLemma(), lang)
					&& (Dictionary.isDictionaryWord(w.getText(), lang)
							|| Dictionary.isDictionaryWord(w.getLemma(), lang))
					&& wordText.length() > 2) {
				s.getWords().add(w);
				if (s.getWordOccurences().containsKey(w)) {
					s.getWordOccurences().put(w, s.getWordOccurences().get(w) + 1);
				} else {
					s.getWordOccurences().put(w, 1);
				}
			}
		}

		if (s.getLanguage().equals(Lang.eng)) {
			SentimentEntity se = new SentimentEntity();
			s.setSentimentEntity(se);
		}

		s.finalProcessing(b, null);
		return s;
	}
}
