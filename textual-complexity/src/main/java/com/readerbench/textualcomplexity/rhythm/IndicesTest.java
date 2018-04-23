/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm;

import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.rhythm.indices.LanguageRhythmicCoefficient;
import com.readerbench.textualcomplexity.rhythm.indices.LanguageRhythmicIndexSM;
import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.coreservices.semanticModels.LSA.LSA;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class IndicesTest {
    public static void main(String[] args) {
            //ReaderBenchServer.initializeDB();
            // load models
            LSA lsa = LSA.loadLSA("resources/config/EN/LSA/COCA_newspaper", Lang.en);
            LDA lda = LDA.loadLDA("resources/config/EN/LDA/COCA_newspaper", Lang.en);
            
            List<ISemanticModel> models = new ArrayList<>();
            models.add(lsa);
            models.add(lda);
            
            String pathToConv = "resources/in/CSCL/sample chat.xml";
            Conversation c = Conversation.load(new File(pathToConv), models, Lang.en, true);

            String pathToDoc;
//            pathToDoc = "resources/in/Docs/sample doc1.xml";
//            pathToDoc = "resources/in/Docs/Aaron22.01.01.xml";
            pathToDoc = "C:\\Users\\admin_licenta\\Desktop\\ReaderBench (EN)\\texts\\bohemian.xml";
            Document d = Document.load(new File(pathToDoc), models, Lang.en, true);
            
//            LanguageRhythm lr = new LanguageRhythm();
//            lr.compute(d);
                    
            LanguageRhythmicIndexSM lri = new LanguageRhythmicIndexSM();
//            lri.compute(d);
            
            LanguageRhythmicCoefficient lrc = new LanguageRhythmicCoefficient();
            lrc.compute(d);
//            pr.compute(d);
//            pr.evaluateParticipantsRhythmicity(c);
    }
}
