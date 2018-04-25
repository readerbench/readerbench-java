/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.processingservice;

import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param annotators
     * @return
     */
    public static Map<SimilarityType, Double> textSimilarities(String text1, String text2, Lang lang, List<ISemanticModel> models, List<Annotators> annotators) {
        if (text1 == null || text1.isEmpty() || text2 == null || text2.isEmpty() || lang == null || models == null || models.isEmpty()) {
            return null;
        }

        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
        Document docText1 = pipeline.createDocumentFromTemplate(AbstractDocumentTemplate.getDocumentModel(text1));
        Document docText2 = pipeline.createDocumentFromTemplate(AbstractDocumentTemplate.getDocumentModel(text2));

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
