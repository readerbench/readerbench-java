package com.readerbench.coreservices.semanticModels.WordNet;

import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import vu.wntools.wordnet.WordnetData;

import java.util.ArrayList;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class WordOntologyProcessing {

    /**
     * Gets the distance to the root of the hypernym tree. If the word was
     * disambiguated it starts with the senseId that was determined. Otherwise
     * it uses the first sense id returned by WordNet. We go up the hypernym
     * tree always selecting the first hypernym returned by WordNet under the
     * assumption that it is the most likely one.
     */
    public static int getMaxDistanceToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        WordnetData dictionary = OntologySupport.getDictionary(word);
        ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
        if (dictionary.hyperRelations.containsKey(senseId)) {
            dictionary.getMultipleHyperChain(senseId, targetChains);
            return targetChains.stream().mapToInt(ArrayList::size).max().orElse(0);
        }
        return 0;
    }

    //todo - this method was moved from com.readerbench.textualcomplexity.wordComplexity.WordComplexity ??
    public static double getAverageDistanceToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        double avg = OntologySupport.getDictionary(word).getAverageDepthBySynset(senseId);
        if (avg == 1) return 0;
        return avg;
    }

    //todo - this method was moved from com.readerbench.textualcomplexity.wordComplexity.WordComplexity ??
    public static int getPathCountToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        WordnetData dictionary = OntologySupport.getDictionary(word);
        ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
        if (dictionary.hyperRelations.containsKey(senseId)) {
            dictionary.getMultipleHyperChain(senseId, targetChains);
            return targetChains.size();
        }
        return 0;
    }

}
