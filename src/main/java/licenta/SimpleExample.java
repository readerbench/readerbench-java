package licenta;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.io.Files;

import data.Lang;
import data.Word;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import services.nlp.parsing.Context;

/** A simple corenlp example ripped directly from the Stanford CoreNLP website using text from wikinews. */
public class SimpleExample {

    public static void main(String[] args) throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos,parse, lemma,depparse, ner, mention, coref, sentiment");
        props.put("coref.md.type", "dependency");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text from the file..
        File inputFile = new File("src/main/java/licenta/sample-content.txt");
        String text = Files.toString(inputFile, Charset.forName("UTF-8"));

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            double valence = RNNCoreAnnotations.getPredictedClass(tree) - 2;
            tree.pennPrint();

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
            System.out.println("dependency graph:\n" + dependencies.toList());

            Context ctx = new Context();

//            String[] sentenceWords = sentence.split("[\\s\\.,]+");
            String[] sentenceWords = text.split("[\\p{Punct}\\s]+");
            Word w =  Word.getWordFromConcept("wants", Lang.en);
            System.out.println(w.toString());
            List<Tree> subTrees = ctx.findContextTree(tree,w, false);
            String sentenceOrContext = "";
            for (Tree t: subTrees) {
                t.pennPrint();
                valence = RNNCoreAnnotations.getPredictedClass(t) - 2;
                System.out.println(valence);
                for (int wordIndex = 0; wordIndex < sentenceWords.length; wordIndex++) {
                    String wordInSentence = sentenceWords[wordIndex];
                    System.out.println(wordInSentence);
                    //the word is the label of a node in the tree
                    if (ctx.findNodeInTree(t, wordInSentence).size() > 0) {
                        sentenceOrContext += wordInSentence + " ";
                    }
                }
            }

            System.out.println(sentenceOrContext);

        }


    }

}