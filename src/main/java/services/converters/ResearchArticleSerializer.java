/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters;

import data.AbstractDocument;
import data.article.ResearchArticle;
import data.Lang;
import java.io.File;
import java.io.FilenameFilter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import view.widgets.ReaderBenchView;
import services.semanticModels.SimilarityType;

public class ResearchArticleSerializer {

    public static void main(String[] args) {
        String outputFolder = "resources/in/LAK_corpus/parsed-documents";
        serializeDocuments(outputFolder);
    }

    private static void serializeDocuments(String dirName) {
        File[] files;
        files = new File(dirName).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        for (File f : files) {
            try {
                addSingleDocument(f.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addSingleDocument(String filePath) {
        List<String> lsaSpaces = ReaderBenchView.LSA_SPACES.get(Lang.en);
        List<String> ldsSpaces = ReaderBenchView.LDA_SPACES.get(Lang.en);

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, lsaSpaces.get(0));
        modelPaths.put(SimilarityType.LDA, ldsSpaces.get(0));

        ResearchArticle d = ResearchArticle.load(filePath, modelPaths, Lang.en, false, true);
        d.computeAll(false);
        d.save(AbstractDocument.SaveType.SERIALIZED);
    }
}
