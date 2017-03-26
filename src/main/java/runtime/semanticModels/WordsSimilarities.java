/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.semanticModels;

import data.Lang;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.similarity.TextSimilarity;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class WordsSimilarities {

    public static final Logger LOGGER = Logger.getLogger("");

    private String path;
    private String inputFile;
    private String outputFile;

    private String lsa;
    private String lda;

    public WordsSimilarities() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getLsa() {
        return lsa;
    }

    public void setLsa(String lsa) {
        this.lsa = lsa;
    }

    public String getLda() {
        return lda;
    }

    public void setLda(String lda) {
        this.lda = lda;
    }

    private String csvHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("word1").append(",").append("POS").append(",");
        sb.append("word2").append(",").append("POS").append(",");
        sb.append("LSA").append(",");
        sb.append("LDA").append(",");
        sb.append("WuPalmer").append(",");
        sb.append("Leacock").append(",");
        sb.append("PathSim").append(",");
        sb.append("\n");
        return sb.toString();
    }

    private void process() {
        Lang lang = Lang.getLang("English");
        List<ISemanticModel> models = new ArrayList<>();
        models.add(LSA.loadLSA(getLsa(), lang));
        models.add(LDA.loadLDA(getLda(), lang));
        try {
            FileInputStream fstream = new FileInputStream(getPath() + "/" + getInputFile());
            FileWriter outputWriter;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
                outputWriter = new FileWriter(getPath() + "/" + getOutputFile());
                outputWriter.write(csvHeader());
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    String[] array = StringUtils.stripEnd(strLine, ", ").split(",", -1);
                    LOGGER.info(Arrays.toString(array));
                    if (array.length > 1) {
                        for (int i = 2; i < array.length; i = i + 2) {
                            Map<SimilarityType, Double> similarityScores = TextSimilarity.textSimilarities(array[0], array[i], lang, models, true);
                            StringBuilder sb = new StringBuilder();
                            sb.append(array[0]).append(",").append(array[1]).append(",");
                            sb.append(array[i]).append(",").append(array[i + 1]).append(",");
                            sb.append(similarityScores.get(SimilarityType.LSA)).append(",");
                            sb.append(similarityScores.get(SimilarityType.LDA)).append(",");
                            sb.append(similarityScores.get(SimilarityType.WU_PALMER)).append(",");
                            sb.append(similarityScores.get(SimilarityType.LEACOCK_CHODOROW)).append(",");
                            sb.append(similarityScores.get(SimilarityType.PATH_SIM)).append(",");
                            sb.append("\n");
                            outputWriter.write(sb.toString());
                            sb.setLength(0);
                        }
                    }
                }
                fstream.close();
            }
            outputWriter.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String args[]) {
        WordsSimilarities ws = new WordsSimilarities();
        ws.setPath("resources/in/ENEA");
        ws.setInputFile("Keyword_pairs.csv");
        ws.setOutputFile("keyword_scores.csv");
        ws.setLsa("resources/config/EN/LSA/ENEA_TASA");
        ws.setLda("resources/config/EN/LDA/ENEA_TASA");
        ws.process();
    }

}
