/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels;

import data.AbstractDocumentTemplate;
import data.Lang;
import data.discourse.SemanticCohesion;
import data.document.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class TextSimilarity {

    /**
     *
     * @param text1 First text
     * @param text2 Second text
     * @param lang The language of the models
     * @param models The models
     * @param usePOSTagging use or not POS tagging
     * @return
     */
    public static Map<SimilarityType, Double> textSimilarities(String text1, String text2, Lang lang, List<ISemanticModel> models, boolean usePOSTagging) {
        if (text1 == null || text1.isEmpty() || text2 == null || text2.isEmpty() || lang == null || models == null || models.isEmpty()) {
            return null;
        }
        Document docText1 = new Document(null, AbstractDocumentTemplate.getDocumentModel(text1), models, lang, usePOSTagging);
        Document docText2 = new Document(null, AbstractDocumentTemplate.getDocumentModel(text2), models, lang, usePOSTagging);
        SemanticCohesion sc = new SemanticCohesion(docText1, docText2);
        List<SimilarityType> methods = new ArrayList();
        methods.add(SimilarityType.LSA);
        methods.add(SimilarityType.LDA);
        methods.add(SimilarityType.LEACOCK_CHODOROW);
        methods.add(SimilarityType.WU_PALMER);
        methods.add(SimilarityType.PATH_SIM);
        methods.add(SimilarityType.WORD2VEC);
        Map<SimilarityType, Double> similarityScores = new HashMap<>();
        for (SimilarityType method : methods) {
            similarityScores.put(method, sc.getSemanticSimilarities().get(method));
        }
        return similarityScores;
    }

}
