/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm;

import services.complexity.rhythm.indices.LanguageRhythmicIndexSM;
import services.complexity.rhythm.indices.LanguageRhythmicCoefficient;
import data.Lang;
import data.cscl.Conversation;
import data.document.Document;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class IndicesTest {
    public static void main(String[] args) {
            ReaderBenchServer.initializeDB();
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
