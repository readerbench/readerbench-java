/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.converters;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.data.article.ResearchArticle;
import com.readerbench.services.semanticModels.SimilarityType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class ResearchArticleSerializer {

    public static final Map<Lang, List<String>> LSA_SPACES = new HashMap();
    public static final Map<Lang, List<String>> LDA_SPACES = new HashMap();
    public static final Map<Lang, List<String>> WORD2VEC_SPACES = new HashMap();

    static {
        identifyModels(LSA_SPACES, "LSA");
        identifyModels(LDA_SPACES, "LDA");
        identifyModels(WORD2VEC_SPACES, "word2vec");
    }

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
        List<String> lsaSpaces = LSA_SPACES.get(Lang.en);
        List<String> ldsSpaces = LDA_SPACES.get(Lang.en);

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, "resources/config/EN/LSA/TASA");
        //modelPaths.put(SimilarityType.LDA, ldsSpaces.get(0));

        ResearchArticle d = ResearchArticle.load(filePath, modelPaths, Lang.en, false, true);
        d.computeAll(false, false);
        d.save(AbstractDocument.SaveType.SERIALIZED);
    }

    private static void identifyModels(Map<Lang, List<String>> models, String type) {
        for (Lang lang : Lang.values()) {
            List<String> paths = new ArrayList<>();
            File path = new File("resources/config/" + lang.toString().toUpperCase() + "/" + type);
            if (path.exists() && path.isDirectory()) {
                for (File folder : path.listFiles((File current, String name) -> new File(current, name).isDirectory())) {
                    paths.add(folder.getPath().replace("\\", "/"));
                }
            }
            paths.add("");
            models.put(lang, paths);
        }
    }
}
